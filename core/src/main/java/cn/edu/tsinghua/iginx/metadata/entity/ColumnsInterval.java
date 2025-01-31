/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.edu.tsinghua.iginx.metadata.entity;

import static cn.edu.tsinghua.iginx.engine.logical.utils.PathUtils.addSuffix;

import cn.edu.tsinghua.iginx.utils.StringUtils;
import java.util.Objects;

public final class ColumnsInterval implements Comparable<ColumnsInterval> {

  private String startColumn;

  private String endColumn;

  private String schemaPrefix = null;

  public ColumnsInterval(String startColumn, String endColumn) {
    this.startColumn = startColumn;
    this.endColumn = endColumn;
  }

  public ColumnsInterval(String startColumn, String endColumn, String schemaPrefix) {
    this(startColumn, endColumn);
    this.schemaPrefix = schemaPrefix;
  }

  private boolean isValid(String prefix) {
    return prefix != null
        && !prefix.contains("..")
        && (prefix.isEmpty() || prefix.charAt(0) != '.');
  }

  public ColumnsInterval(String column) {
    if (StringUtils.isContainRegex(column)
        || !isValid(column)
        || StringUtils.isContainSpecialChar(column)) {
      throw new IllegalArgumentException("not support the regex in prefix");
    }
    ColumnsInterval columnsInterval = addSuffix(new ColumnsInterval(column, column));
    this.startColumn = columnsInterval.startColumn;
    this.endColumn = columnsInterval.endColumn;
  }

  private String realColumn(String column) {
    if (column != null && schemaPrefix != null) {
      return schemaPrefix + "." + column;
    }
    return column;
  }

  public String getStartColumn() {
    return startColumn;
  }

  public void setStartColumn(String startColumn) {
    this.startColumn = startColumn;
  }

  public String getEndColumn() {
    return endColumn;
  }

  public void setEndColumn(String endColumn) {
    this.endColumn = endColumn;
  }

  @Override
  public String toString() {
    return startColumn + "-" + endColumn;
  }

  public String getSchemaPrefix() {
    return schemaPrefix;
  }

  public void setSchemaPrefix(String schemaPrefix) {
    this.schemaPrefix = schemaPrefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ColumnsInterval that = (ColumnsInterval) o;
    return Objects.equals(startColumn, that.getStartColumn())
        && Objects.equals(endColumn, that.getEndColumn());
  }

  @Override
  public int hashCode() {
    return Objects.hash(startColumn, endColumn);
  }

  public boolean isContain(String columnName) {
    // judge if is the dummy node && it will have specific prefix
    String startColumn = realColumn(this.startColumn);
    String endColumn = realColumn(this.endColumn);

    return (startColumn == null
            || (columnName != null && StringUtils.compare(columnName, startColumn, true) >= 0))
        && (endColumn == null
            || (columnName != null && StringUtils.compare(columnName, endColumn, false) < 0));
  }

  public boolean isCompletelyBefore(String columnName) {
    // judge if is the dummy node && it will have specific prefix
    String endColumn = realColumn(this.endColumn);

    return endColumn != null && columnName != null && endColumn.compareTo(columnName) <= 0;
  }

  public boolean isIntersect(ColumnsInterval columnsInterval) {
    // judge if is the dummy node && it will have specific prefix
    String startColumn = realColumn(this.startColumn);
    String endColumn = realColumn(this.endColumn);

    return (columnsInterval.getStartColumn() == null
            || endColumn == null
            || StringUtils.compare(columnsInterval.getStartColumn(), endColumn, false) < 0)
        && (columnsInterval.getEndColumn() == null
            || startColumn == null
            || StringUtils.compare(columnsInterval.getEndColumn(), startColumn, true) >= 0);
  }

  public ColumnsInterval getIntersect(ColumnsInterval columnsInterval) {
    if (!isIntersect(columnsInterval)) {
      return null;
    }
    // judge if is the dummy node && it will have specific prefix
    String startColumn = realColumn(this.startColumn);
    String endColumn = realColumn(this.endColumn);

    String start =
        startColumn == null
            ? columnsInterval.getStartColumn()
            : columnsInterval.getStartColumn() == null
                ? startColumn
                : StringUtils.compare(columnsInterval.getStartColumn(), startColumn, true) < 0
                    ? startColumn
                    : columnsInterval.getStartColumn();
    String end =
        endColumn == null
            ? columnsInterval.getEndColumn()
            : columnsInterval.getEndColumn() == null
                ? endColumn
                : StringUtils.compare(columnsInterval.getEndColumn(), endColumn, false) < 0
                    ? columnsInterval.getEndColumn()
                    : endColumn;
    return new ColumnsInterval(start, end);
  }

  public boolean isCompletelyAfter(ColumnsInterval columnsInterval) {
    // judge if is the dummy node && it will have specific prefix
    String startColumn = realColumn(this.startColumn);

    return columnsInterval.getEndColumn() != null
        && startColumn != null
        && StringUtils.compare(columnsInterval.getEndColumn(), startColumn, true) < 0;
  }

  public boolean isAfter(String colName) {
    // judge if is the dummy node && it will have specific prefix
    String startColumn = realColumn(this.startColumn);

    return startColumn != null && StringUtils.compare(colName, startColumn, true) < 0;
  }

  @Override
  public int compareTo(ColumnsInterval o) {
    // judge if is the dummy node && it will have specific prefix
    String startColumn = realColumn(this.startColumn);
    String endColumn = realColumn(this.endColumn);

    int value = compareTo(startColumn, o.getStartColumn(), true);
    if (value != 0) {
      return value;
    }
    return compareTo(endColumn, o.getEndColumn(), false);
  }

  private static int compareTo(String s1, String s2, boolean isStart) {
    if (s1 == null && s2 == null) {
      return 0;
    }
    if (s1 == null) {
      return isStart ? -1 : 1;
    }
    if (s2 == null) {
      return isStart ? 1 : -1;
    }
    return s1.compareTo(s2);
  }
}
