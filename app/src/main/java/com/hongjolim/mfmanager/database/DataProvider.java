package com.hongjolim.mfmanager.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DataProvider extends ContentProvider {

    private static final String AUTHORITY = "com.hongjolim.mfmanager.dataprovider";
    private static final String BASE_PATH_USER = "USER";
    private static final String BASE_PATH_ACCOUNT = "ACCOUNTS";
    private static final String BASE_PATH_EX_CATEGORY = "EX_CATEGORY";
    private static final String BASE_PATH_IN_CATEGORY = "IN_CATEGORY";
    private static final String BASE_PATH_TRANSACTION = "TRANSACTION";
    private static final String BASE_PATH_CREDIT = "CREDIT";

    public static final Uri USER_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH_USER);
    public static final Uri ACCOUNTS_URI = Uri.parse("content://" + AUTHORITY + "/"+BASE_PATH_ACCOUNT);
    public static final Uri EX_CATEGORY_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH_EX_CATEGORY);
    public static final Uri IN_CATEGORY_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH_IN_CATEGORY);
    public static final Uri TRANSACTION_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH_TRANSACTION);
    public static final Uri CREDIT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH_CREDIT);

    public static final int ACCOUNT_DATA = 1;
    public static final int ACCOUNT_DATA_ID = 2;

    public static final int EX_CATEGORY_DATA = 3;
    public static final int EX_CATEGORY_DATA_ID = 4;

    public static final int IN_CATEGORY_DATA = 5;
    public static final int IN_CATEGORY_DATA_ID = 6;

    public static final int TRANSACTION_DATA = 7;
    public static final int TRANSACTION_DATA_ID = 8;

    public static final int CREDIT_DATA = 9;
    public static final int CREDIT_DATA_ID = 10;

    public static final int USER_DATA = 11;

    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH_ACCOUNT, ACCOUNT_DATA);
        uriMatcher.addURI(AUTHORITY, BASE_PATH_ACCOUNT + "/#", ACCOUNT_DATA_ID);

        uriMatcher.addURI(AUTHORITY, BASE_PATH_EX_CATEGORY, EX_CATEGORY_DATA);
        uriMatcher.addURI(AUTHORITY, BASE_PATH_EX_CATEGORY + "/#", EX_CATEGORY_DATA_ID);

        uriMatcher.addURI(AUTHORITY, BASE_PATH_IN_CATEGORY, IN_CATEGORY_DATA);
        uriMatcher.addURI(AUTHORITY, BASE_PATH_IN_CATEGORY + "/#", IN_CATEGORY_DATA_ID);

        uriMatcher.addURI(AUTHORITY, BASE_PATH_TRANSACTION, TRANSACTION_DATA);
        uriMatcher.addURI(AUTHORITY, BASE_PATH_TRANSACTION + "/#", TRANSACTION_DATA_ID);

        uriMatcher.addURI(AUTHORITY, BASE_PATH_CREDIT, CREDIT_DATA);
        uriMatcher.addURI(AUTHORITY, BASE_PATH_CREDIT+"/#", CREDIT_DATA_ID);

        uriMatcher.addURI(AUTHORITY, BASE_PATH_USER, USER_DATA);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBHelper helper = new DBHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {

        if(uriMatcher.match(uri)==ACCOUNT_DATA_ID||uriMatcher.match(uri)==ACCOUNT_DATA){
            if (uriMatcher.match(uri) == ACCOUNT_DATA_ID) {
                selection = AccountsTable.COL1 + "=" + uri.getLastPathSegment();
            }
            return database.query(AccountsTable.TABLE_NAME, AccountsTable.ALL_COLS,
                    selection, selectionArgs, null, null, orderBy);
        }else if(uriMatcher.match(uri)==EX_CATEGORY_DATA_ID||uriMatcher.match(uri)==EX_CATEGORY_DATA) {
            if (uriMatcher.match(uri) == EX_CATEGORY_DATA_ID) {
                selection = ExCategoryTable.COL1 + "=" + uri.getLastPathSegment();
            }
            return database.query(ExCategoryTable.TABLE_NAME, ExCategoryTable.ALL_COLS,
                    selection, selectionArgs, null, null, orderBy);
        }else if(uriMatcher.match(uri)==IN_CATEGORY_DATA_ID||uriMatcher.match(uri)==IN_CATEGORY_DATA) {
            if (uriMatcher.match(uri) == IN_CATEGORY_DATA_ID) {
                selection = InCategoryTable.COL1 + "=" + uri.getLastPathSegment();
            }
            return database.query(InCategoryTable.TABLE_NAME, InCategoryTable.ALL_COLS,
                    selection, selectionArgs, null, null, orderBy);
        }else if(uriMatcher.match(uri)==TRANSACTION_DATA||uriMatcher.match(uri)==TRANSACTION_DATA_ID) {
            if (uriMatcher.match(uri) == TRANSACTION_DATA_ID) {
                selection = TransactionTable.COL1 + "=" + uri.getLastPathSegment();
            }
            return database.query(TransactionTable.TABLE_NAME, TransactionTable.ALL_COLS, selection,
                    selectionArgs, null, null, orderBy);
        }else if(uriMatcher.match(uri)==CREDIT_DATA||uriMatcher.match(uri)==CREDIT_DATA_ID){
            if(uriMatcher.match(uri)==CREDIT_DATA_ID){
                selection=CreditTable.COL1 +"="+uri.getLastPathSegment();
            }
            return database.query(CreditTable.TABLE_NAME, CreditTable.ALL_COLS, selection, selectionArgs, null,
                    null, orderBy);
        }else if(uriMatcher.match(uri)==USER_DATA){
            return database.query(UserTable.TABLE_NAME, UserTable.ALL_COLS,
                    null, null, null, null, null);
        }
        return null;

    }

    @Override
    public String getType(Uri uri) {

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if(uriMatcher.match(uri)==EX_CATEGORY_DATA||uriMatcher.match(uri)==EX_CATEGORY_DATA_ID){
            long id = database.insert(ExCategoryTable.TABLE_NAME, null, values);
            return Uri.parse(BASE_PATH_EX_CATEGORY+"/"+id);
        }else if(uriMatcher.match(uri)==IN_CATEGORY_DATA||uriMatcher.match(uri)==IN_CATEGORY_DATA_ID){
            long id = database.insert(InCategoryTable.TABLE_NAME, null, values);
            return Uri.parse(BASE_PATH_IN_CATEGORY+"/"+id);
        }else if(uriMatcher.match(uri)==ACCOUNT_DATA_ID||uriMatcher.match(uri)==ACCOUNT_DATA){
            long id = database.insert(AccountsTable.TABLE_NAME, null, values);
            return Uri.parse(BASE_PATH_ACCOUNT + "/" + id);
        }else if(uriMatcher.match(uri)==TRANSACTION_DATA_ID||uriMatcher.match(uri)==TRANSACTION_DATA) {
            long id = database.insert(TransactionTable.TABLE_NAME, null, values);
            return Uri.parse(BASE_PATH_TRANSACTION + "/" + id);
        }else if(uriMatcher.match(uri)==CREDIT_DATA_ID||uriMatcher.match(uri)==CREDIT_DATA){
            long id = database.insert(CreditTable.TABLE_NAME, null, values);
            return Uri.parse(BASE_PATH_CREDIT+"/"+id);
        }else if(uriMatcher.match(uri)==USER_DATA){
            long id = database.insert(UserTable.TABLE_NAME, null, values);
            return Uri.parse(BASE_PATH_CREDIT+"/"+id);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){

        if(uri.equals(ACCOUNTS_URI)){
            return database.delete(AccountsTable.TABLE_NAME, selection, selectionArgs);
        }else if(uri.equals(TRANSACTION_URI)){
            return database.delete(TransactionTable.TABLE_NAME, selection, selectionArgs);
        }else if(uri.equals(EX_CATEGORY_URI)){
            return database.delete(ExCategoryTable.TABLE_NAME, selection, selectionArgs);
        }else if(uri.equals(IN_CATEGORY_URI)){
            return database.delete(InCategoryTable.TABLE_NAME, selection, selectionArgs);
        }else if(uri.equals(CREDIT_URI)) {
            return database.delete(CreditTable.TABLE_NAME, selection, selectionArgs);
        }else if(uri.equals(USER_URI)){
            return database.delete(UserTable.TABLE_NAME, null, null);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String s,
                      @Nullable String[] strings) {

        if (uriMatcher.match(uri)==ACCOUNT_DATA||uriMatcher.match(uri)==ACCOUNT_DATA_ID) {
            return database.update(AccountsTable.TABLE_NAME, values, s, strings);
        }else if(uriMatcher.match(uri)==TRANSACTION_DATA||uriMatcher.match(uri)==TRANSACTION_DATA_ID){
            return database.update(TransactionTable.TABLE_NAME, values, s, strings);
        }else if(uriMatcher.match(uri)==EX_CATEGORY_DATA_ID||uriMatcher.match(uri)==EX_CATEGORY_DATA){
            return database.update(ExCategoryTable.TABLE_NAME, values, s, strings);
        }else if(uriMatcher.match(uri)==IN_CATEGORY_DATA_ID||uriMatcher.match(uri)==IN_CATEGORY_DATA){
            return database.update(InCategoryTable.TABLE_NAME, values, s, strings);
        }else if(uriMatcher.match(uri)==CREDIT_DATA_ID||uriMatcher.match(uri)==CREDIT_DATA){
            return database.update(CreditTable.TABLE_NAME, values, s, strings);
        }else if(uriMatcher.match(uri)==USER_DATA){
            return database.update(UserTable.TABLE_NAME, values, s, strings);
        }
        return 0;
    }
}
