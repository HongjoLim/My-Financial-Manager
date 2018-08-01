package com.hongjolim.mfmanager.database;

/**
 * Created by HONGJO on 3/20/2018.
 */

public class InCategoryTable {

    public static final String TABLE_NAME = "IN_CATEGORY";

    public static final String COL1 = "_id";
    public static final String COL2 = "name";
    public static final String COL3 = "amount";

    public static final String[] ALL_COLS = {COL1, COL2, COL3};

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
            COL1+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
            COL2+" TEXT NOT NULL, "+ COL3+" TEXT NOT NULL);";

    public static final String SQL_DELETE = "DROP TABLE IF EXISTS "+TABLE_NAME+";";


}
