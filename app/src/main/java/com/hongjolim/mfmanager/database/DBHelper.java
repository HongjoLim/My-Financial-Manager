package com.hongjolim.mfmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Created by HONGJO on 3/13/2018.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "MFM";
    private static final int DB_VER = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(UserTable.SQL_CREATE);
        db.execSQL(AccountsTable.SQL_CREATE);
        db.execSQL(TransactionTable.SQL_CREATE);
        db.execSQL(ExCategoryTable.SQL_CREATE);
        db.execSQL(InCategoryTable.SQL_CREATE);
        db.execSQL(CreditTable.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        db.execSQL(UserTable.SQL_DELETE);
        db.execSQL(AccountsTable.SQL_DELETE);
        db.execSQL(TransactionTable.SQL_DELETE);
        db.execSQL(ExCategoryTable.SQL_DELETE);
        db.execSQL(InCategoryTable.SQL_DELETE);
        db.execSQL(CreditTable.SQL_DELETE);
        onCreate(db);
    }


}
