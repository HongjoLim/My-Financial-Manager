package com.hongjolim.mfmanager.database;
/**
 * Created by HONGJO on 3/13/2018.
 */

public class AccountsTable {

    public final static String TABLE_NAME = "ACCOUNTS";
    public final static String COL1 = "_id";
    public final static String COL2 = "name";
    public final static String COL3 = "type";
    public final static String COL4 = "starting_balance";
    public final static String COL5 = "current_balance";

    public final static String[] ALL_COLS = {COL1, COL2, COL3, COL4, COL5};

    public final static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+ "("+
            COL1+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
            COL2+" TEXT NOT NULL, "+
            COL3+" TEXT, "+
            COL4+" TEXT, "+
            COL5+" TEXT);";

    public final static String SQL_DELETE = "DROP TABLE IF EXISTS "+TABLE_NAME+";";

}
