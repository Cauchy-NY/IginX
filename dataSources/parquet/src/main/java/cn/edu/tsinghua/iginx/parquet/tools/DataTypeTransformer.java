package cn.edu.tsinghua.iginx.parquet.tools;

import cn.edu.tsinghua.iginx.thrift.DataType;

public class DataTypeTransformer {

  public static DataType fromParquetDataType(String dataType) {
    switch (dataType) {
      case "BOOLEAN":
        return DataType.BOOLEAN;
      case "INTEGER":
        return DataType.INTEGER;
      case "BIGINT":
        return DataType.LONG;
      case "FLOAT":
        return DataType.FLOAT;
      case "DOUBLE":
        return DataType.DOUBLE;
      case "VARCHAR":
      default:
        return DataType.BINARY;
    }
  }

  public static DataType fromDuckDBDataType(String dataType) {
    switch (dataType) {
      case "BOOLEAN":
        return DataType.BOOLEAN;
      case "INT32":
        return DataType.INTEGER;
      case "INT64":
        return DataType.LONG;
      case "FLOAT":
        return DataType.FLOAT;
      case "DOUBLE":
        return DataType.DOUBLE;
      case "BYTE_ARRAY":
      default:
        return DataType.BINARY;
    }
  }

  public static DataType fromStringDataType(String dataType) {
    switch (dataType) {
      case "BOOLEAN":
        return DataType.BOOLEAN;
      case "INTEGER":
        return DataType.INTEGER;
      case "LONG":
        return DataType.LONG;
      case "FLOAT":
        return DataType.FLOAT;
      case "DOUBLE":
        return DataType.DOUBLE;
      case "BINARY":
      default:
        return DataType.BINARY;
    }
  }

  public static String toParquetDataType(DataType dataType) {
    switch (dataType) {
      case BOOLEAN:
        return "BOOLEAN";
      case INTEGER:
        return "INTEGER";
      case LONG:
        return "BIGINT";
      case FLOAT:
        return "FLOAT";
      case DOUBLE:
        return "DOUBLE";
      case BINARY:
      default:
        return "VARCHAR";
    }
  }

  public static int getDataSize(DataType type) {
    switch (type) {
      case BOOLEAN:
        return 1;
      case FLOAT:
      case INTEGER:
        return 4;
      case LONG:
      case DOUBLE:
        return 8;
      default:
        return 0;
    }
  }
}
