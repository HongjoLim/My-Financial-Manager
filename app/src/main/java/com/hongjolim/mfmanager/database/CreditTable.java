package com.hongjolim.mfmanager.database;

public class CreditTable {

    public static final String TABLE_NAME = "CREDIT";
    public static final String COL1 = "_id";
    public static final String COL2 = "name";
    public static final String COL3 = "pay_date";
    //the id of the account that the money show be withdrawn from for payment every month
    public static final String COL4 = "accounts_id";
    public static final String COL5 = "debt";
    public static final String COL6 = "cycleStart";
    public static final String COL7 = "cycleEnd";

    public static final String[] ALL_COLS = {COL1, COL2, COL3, COL4, COL5, COL6, COL7};

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
            COL1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
            COL2+" TEXT, "+
            COL3+" INTEGER, "+
            COL4+" INTEGER, "+
            COL5+" TEXT, "+
            COL6+" INTEGER, "+
            COL7+" INTEGER)";

    public static final String SQL_DELETE = "DELETE TABLE IF EXISTS "+TABLE_NAME;


}
