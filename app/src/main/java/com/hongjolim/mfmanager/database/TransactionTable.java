package com.hongjolim.mfmanager.database;

/**
 * Created by HONGJO on 3/14/2018.
 */

public class TransactionTable {

    public static final String TABLE_NAME = "TRANSACTIONS";
    public static final String COL1 = "_id";
    public static final String COL2 = "date";
    public static final String COL3 = "amount";
    public static final String COL4 = "description";
    public static final String COL5 = "category_id";
    public static final String COL6 = "account_id";
    public static final String COL7 = "credit_id";
    public static final String COL8 = "trans_type";

    //expense
    public static final int TRANS_TYPE1 = 1;
    //income
    public static final int TRANS_TYPE2 = 2;
    //transfer_minus
    public static final int TRANS_TYPE3 = 3;
    //transfer_plus
    public static final int TRANS_TYPE4 = 4;
    //adjustment_minus
    public static final int TRANS_TYPE5 = 5;
    //adjustment_plus
    public static final int TRANS_TYPE6 = 6;

    public static final String[] ALL_COLS = {COL1, COL2, COL3, COL4, COL5, COL6, COL7, COL8};

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+
            COL1+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
            COL2+" TEXT NOT NULL, "+
            COL3+" TEXT NOT NULL, "+
            COL4+" TEXT, "+
            COL5+" INTEGER, "+
            COL6+" INTEGER, "+
            COL7+" INTEGER, "+
            COL8+" INTEGER);";

    public static final String SQL_DELETE = "DROP TABLE IF EXISTS "+TABLE_NAME+";";


}
