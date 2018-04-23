package com.mrray.desens.task.utils;

import com.mrray.desens.task.constant.Constant;
import com.mrray.desens.task.entity.vo.BaseResourceInfoVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import java.sql.*;
import java.util.List;

/**
 * Created by ln on 2017/10/9.
 */
public class DatabaseUtil {

    private static Log logger = LogFactory.getLog(DatabaseUtil.class);

    public static Connection getJdbcConnection(BaseResourceInfoVo databaseInfo) {
        String dbType = databaseInfo.getDbType();
        Connection con = null;

        String url = null;
        if (Constant.DATABASE_TYPE_MYSQL.equalsIgnoreCase(databaseInfo.getDbType())) {
            url = "jdbc:mysql://" + databaseInfo.getIp() + ":" + databaseInfo.getPort() + "/" + databaseInfo.getDatabaseName();
        } else if (Constant.DATABASE_TYPE_GREENPLUM.equalsIgnoreCase(databaseInfo.getDbType()) || Constant.DATABASE_TYPE_POSTGRESQL.equalsIgnoreCase(databaseInfo.getDbType())) {
            url = "jdbc:postgresql://" + databaseInfo.getIp() + ":" + databaseInfo.getPort() + "/" + databaseInfo.getDatabaseName();
        } else if (Constant.DATABASE_TYPE_ORACLE.equalsIgnoreCase(databaseInfo.getDbType())) {
            url = "jdbc:oracle:thin:@" + databaseInfo.getIp() + ":" + databaseInfo.getPort() + ":" + databaseInfo.getDatabaseName();
        } else if (Constant.DATABASE_TYPE_MSSQL.equalsIgnoreCase(databaseInfo.getDbType())) {
            url = "jdbc:jtds:sqlserver://" + databaseInfo.getIp() + ":" + databaseInfo.getPort() + ";DatabaseName=" + databaseInfo.getDatabaseName();
        } else if (Constant.DATABASE_TYPE_HIVE.equalsIgnoreCase(databaseInfo.getDbType())) {
            url = "jdbc:hive2://" + databaseInfo.getIp() + ":" + databaseInfo.getPort() + "/" + databaseInfo.getDatabaseName();
        } else if (Constant.DATABASE_TYPE_DB2.equalsIgnoreCase(databaseInfo.getDbType())) {
            //暂时未适配装载
        }

        if (!StringUtils.isEmpty(url)) {
            try {
                con = DriverManager.getConnection(url, databaseInfo.getUsername(), databaseInfo.getPassword());
            } catch (SQLException e) {
                con = null;
            }

        }
        return con;
    }

    public static void closeJdbc(Connection con, Statement stmt, ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                logger.error(e);
            }
        }

    }

    public static void dropTablesAsyn(BaseResourceInfoVo databaseInfo, List<String> tables) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = getJdbcConnection(databaseInfo);
                if (conn == null) {
                    return;
                }
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    if (!CollectionUtils.isEmpty(tables)) {
                        for (String table : tables) {
                            stmt.execute(String.format("DROP TABLE IF EXISTS `%s`", table));
                        }
                    }
                } catch (SQLException e) {
                    logger.error(e);
                } finally {
                    closeJdbc(conn, stmt, null);
                }
            }
        }).start();
    }


}
