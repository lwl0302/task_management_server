package com.mrray.desens.task.constant;

/**
 * Created by ln on 2017/9/29.
 */
public class Constant {

    public static final String TASK_TYPE_EXTRCT = "extract";

    public static final String TASK_TYPE_LOAD = "load";

    public static final String TASK_TYPE_DESENS = "desens";

    public static final int TASK_STATUS_NEW = 0;

    public static final int TASK_STATUS_RUNNING = 1;

    public static final int TASK_STATUS_PAUSE = 2;

    public static final int TASK_STATUS_CANCLE = 3;

    public static final int TASK_STATUS_FINISH = 4;

    public static final int TASK_STATUS_WAITING = 5;

    public static final int TASK_STATUS_FIAL_EXTRACT = -1;

    public static final int TASK_STATUS_FIAL_DESENS = -2;

    public static final int TASK_STATUS_FIAL_LOAD = -3;

    public static final int TASK_STATUS_FIAL_UNKNOWN = -4;

    public static final boolean TASK_IS_DELETED = true;

    public static final boolean TASK_NOT_DELETED = false;

    public static final String MICRO_SERVER_RESULT_SUCCESS = "success";

    public static final String DATABASE_TYPE_MYSQL = "mysql";

    public static final String DATABASE_TYPE_ORACLE = "oracle";

    public static final String DATABASE_TYPE_MSSQL = "mssql";

    public static final String DATABASE_TYPE_POSTGRESQL = "postgresql";

    public static final String DATABASE_TYPE_GREENPLUM = "greenplum";

    public static final String DATABASE_TYPE_HIVE = "hive";

    public static final String DATABASE_TYPE_DB2 = "DB2";


}
