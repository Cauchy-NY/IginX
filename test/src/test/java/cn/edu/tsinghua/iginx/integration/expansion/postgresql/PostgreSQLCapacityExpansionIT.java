package cn.edu.tsinghua.iginx.integration.expansion.postgresql;

import static cn.edu.tsinghua.iginx.thrift.StorageEngineType.postgresql;

import cn.edu.tsinghua.iginx.integration.expansion.BaseCapacityExpansionIT;
import cn.edu.tsinghua.iginx.integration.expansion.constant.Constant;
import cn.edu.tsinghua.iginx.integration.expansion.utils.SQLTestTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLCapacityExpansionIT extends BaseCapacityExpansionIT {

  private static final Logger logger = LoggerFactory.getLogger(PostgreSQLCapacityExpansionIT.class);

  public PostgreSQLCapacityExpansionIT() {
    super(postgresql, "username:postgres, password:postgres");
    Constant.oriPort = 5432;
    Constant.expPort = 5433;
    Constant.readOnlyPort = 5434;
  }

  /** 执行一个简单查询1000次，测试是否会使连接池耗尽，来验证PG的dummy查询是否正确释放连接 */
  @Override
  protected void testQuerySpecialHistoryData() {
    String statement = "select * from ln;";
    String expect =
        "ResultSets:\n"
            + "+----+--------------+---------------+\n"
            + "| key|ln.wf02.status|ln.wf02.version|\n"
            + "+----+--------------+---------------+\n"
            + "| 100|          true|             v1|\n"
            + "| 400|         false|             v4|\n"
            + "| 800|          null|             v8|\n"
            + "|1600|          null|            v48|\n"
            + "+----+--------------+---------------+\n"
            + "Total line number = 4\n";

    int times = 1000;
    for (int i = 0; i < times; i++) {
      SQLTestTools.executeAndCompare(session, statement, expect);
    }
  }
}
