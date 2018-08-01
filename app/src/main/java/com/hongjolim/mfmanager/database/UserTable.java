package com.hongjolim.mfmanager.database;

public class UserTable {

    public static final String TABLE_NAME = "USER";

    public static final String COL1 = "EMAIL";
    public static final String COL2 = "PASSWORD";
    public static final String COL3 = "SECURITY_QUESTION_NUM";
    public static final String COL4 = "SECURITY_ANSWER";

    public static final String[] ALL_COLS = {COL1, COL2, COL3, COL4};

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(" +
            COL1+" TEXT, "+
            COL2+" TEXT, "+
            COL3+" INT, "+
            COL4+" TEXT)";

    public static final String SQL_DELETE = "DELETE TABLE IF EXISTS "+TABLE_NAME;
}
