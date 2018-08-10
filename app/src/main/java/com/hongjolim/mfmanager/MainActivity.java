package com.hongjolim.mfmanager;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.hongjolim.mfmanager.database.AccountsTable;
import com.hongjolim.mfmanager.database.DataProvider;
import com.hongjolim.mfmanager.database.DataSource;
import com.hongjolim.mfmanager.database.ExCategoryTable;
import com.hongjolim.mfmanager.database.InCategoryTable;
import com.hongjolim.mfmanager.database.TransactionTable;
import com.hongjolim.mfmanager.model.Account;
import com.hongjolim.mfmanager.model.ExCategory;
import com.hongjolim.mfmanager.model.InCategory;
import com.hongjolim.mfmanager.tools.BigDecimalCalculator;
import com.hongjolim.mfmanager.tools.CurrencyFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>{

    private DataSource mDataSource;

    //static final fields for sharedPreferences
    private static final String CHECK_IF = "IS_FROM_MAIN_FAB";

    //the name of the shared preferences set
    private static final String FIRST_START = "FIRST_START";

    //the key to get value for default currency setting from sharedpreference
    public static final String CURRENCY_KEY = "CURRENCY";

    private static final int REQUEST_CODE = 1;
    private static final int IS_FROM_MAIN_FAB = 1001;
    private Cursor cursor;

    private BigDecimal totalMonthlyIn, totalMonthlyEx;

    private SharedPreferences prefs;
    String currencyCode;

    AdView mAdview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, "ca-app-pub-1465820537677658/8541972644");

        mAdview = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);


        mDataSource = new DataSource(this);
        mDataSource.open();

        //get Shared preferences to check whether this app is launching for the first time or not
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstStart = prefs.getBoolean(FIRST_START, true);

        currencyCode = prefs.getString(CURRENCY_KEY, "Canada");

        if(firstStart){
            setDefaultCurrencySetting();
            setDefaultExCategory();
            setDefaultInCategory();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setContentForMain();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(listener);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getLoaderManager().initLoader(0, null, this);
    }

    private void setDefaultCurrencySetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CharSequence [] items = getResources().getStringArray(R.array.currency);
        builder.setTitle("Choose Currency").
                setItems(items, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("CURRENCY", items[position].toString()).apply();

                        //calls the default cash balance setting method
                        showDefaultAccountSettingDialog();
                    }
                }).setPositiveButton("OK", null).setCancelable(false).create().show();

    }

    //let the user to set the default cash balance, if nothing entered, the balance is set to 0
    private void showDefaultAccountSettingDialog(){

        final View view = getLayoutInflater().inflate(R.layout.set_default_account, null);

        final EditText defaultAmount = view.findViewById(R.id.default_balance);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Default Account Setting");
        builder.setMessage("Please set your Cash balance. If not provided, it will be set to 0");
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                defaultAmount.setText(String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));

                double amount_double = 0.0;

                try {
                    amount_double = Double.parseDouble(defaultAmount.getText().toString());
                }catch(Exception e){
                    amount_double = BigDecimalCalculator.roundValue(0.0, currencyCode);
                }

                String amount = String.valueOf(BigDecimalCalculator.roundValue(amount_double, currencyCode));

                setDefaultAccount("Cash", "default", amount);
                dialogInterface.dismiss();

                SharedPreferences.Editor editor = prefs.edit();
                //if the cash amount is set, set the shared preferences value to false
                editor.putBoolean(FIRST_START, false);
                editor.apply();

            }
        }).setCancelable(false).create().show();
    }

    private void setBudgetSummaryPieChart(BigDecimal totalMonthlyEx) {

        /*
        execute raw query to get data for expenses for category:
        query = SELECT category_id, SUM(CAST(amount AS NUMBER(10))) FROM Transaction
        WHERE category_id IS NOT NULL GROUP BY category_id ORDER BY category_id
        */

        String[] columns = {TransactionTable.COL2, TransactionTable.COL5, "SUM(CAST("+TransactionTable.COL3+" AS REAL))"};
        String[] selectionArgs = {String.valueOf(TransactionTable.TRANS_TYPE1)};
        cursor = mDataSource.query(TransactionTable.TABLE_NAME, columns, TransactionTable.COL8+"=? AND "+
                TransactionTable.COL2+"<=date('now', 'start of month', '+1 month', '-1 day') AND "+
                        TransactionTable.COL2+" >= date('now', 'start of month')",
                selectionArgs, TransactionTable.COL5, null, TransactionTable.COL5);

        ArrayList<ExCategory> exCategories = mDataSource.getAllExCategories();

        try{
            while(cursor.moveToNext()){
                for(int i = 0; i<exCategories.size(); i++){
                    if(cursor.getInt(cursor.getColumnIndex(TransactionTable.COL5))==exCategories.get(i).get_id()){
                        //3rd column is the value of sum of amount spent monthly
                        exCategories.get(i).setTotalMonthlySpent(cursor.getFloat(cursor.getColumnIndex(columns[2])));
                    }
                }
            }
        }finally{
            if(cursor!=null&&!cursor.isClosed())
                cursor.close();
        }

        //to get the cursor to retrieve all the names of Ex categories
        cursor = getContentResolver().query(DataProvider.EX_CATEGORY_URI, ExCategoryTable.ALL_COLS,
        null, null, ExCategoryTable.COL1);

        //initialize variables for pie chart
        PieChart budgetSummary = findViewById(R.id.budget_piechart);
        budgetSummary.setUsePercentValues(false);
        budgetSummary.getDescription().setEnabled(false);
        budgetSummary.setExtraOffsets(5, 0, 5, 5);

        budgetSummary.setDragDecelerationFrictionCoef(0.95f);

        //set whether or not there will be a hole in the center of the pie chart
        budgetSummary.setDrawHoleEnabled(false);;

        ArrayList<PieEntry> yValues = new ArrayList<>();

        //the size of both array lists(the numbers of categoryNames, categoryIds are NOT identical)
        for(int i = 0; i<exCategories.size(); i++) {
            ExCategory exCategory = exCategories.get(i);

            /*finds an exCategory that has been spent at all
             * if it has not been spent, it will not be in the chart
             */

            if(exCategory.getTotalMonthlySpent()!=0.0f) {
                yValues.add(new PieEntry(exCategory.getTotalMonthlySpent(), exCategory.getName()));
            }
        }

        TextView noExpense = findViewById(R.id.show_no_expense);
        if(yValues.size()>=1){
            //if there is more than 1 expense corresponding any expense category, erase the text saying that there is no expense
            noExpense.setVisibility(GONE);
        }else{
            //if there is no expense spent this month so far, erase the pie chart and show the text saying that there is no expense
            budgetSummary.setVisibility(GONE);
        }

        PieDataSet dataSet = new PieDataSet(yValues, null);
        dataSet.setSliceSpace(1.0f);
        dataSet.setSelectionShift(10f);

        //set colors for each part of the pie chart
        dataSet.setColors(Color.rgb(244,67,54), Color.rgb(255,193,7), Color.rgb(3,169,244),
                Color.rgb(76,175,80), Color.rgb(121,85,72));

        PieData data = new PieData((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(getResources().getColor(R.color.lightPrimary));

        budgetSummary.setData(data);

        TextView budgetTotal = findViewById(R.id.budget_total_amount);
        BigDecimal exBudgetTotal = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));
        for(int i = 0; i<exCategories.size(); i++){
            exBudgetTotal = BigDecimalCalculator.add(exBudgetTotal.toString(), exCategories.get(i).getAmount());
            budgetTotal.setText(CurrencyFormatter.format(this, exBudgetTotal.toString()));
        }
        TextView budgetSpent = findViewById(R.id.budget_total_spent);

        if(totalMonthlyEx.doubleValue()>0) {
            budgetSpent.setText(String.format("-%s", CurrencyFormatter.format(this, totalMonthlyEx.toString())));
        }else {
            budgetSpent.setText(CurrencyFormatter.format(this, totalMonthlyEx.toString()));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_CODE&&resultCode==RESULT_OK){
            reLoad();
        }
    }

    private void reLoad(){
        getLoaderManager().restartLoader(0, null, this);
        setContentForMain();
    }

    @SuppressLint("SetTextI18n")
    private void setContentForMain() {

        CardView netEarningMonthlyCardView = findViewById(R.id.netEarnings_monthly_cardView);

        TextView total7daysExpense = findViewById(R.id.expense_total_7days);
        TextView total7daysIncome = findViewById(R.id.income_total_7days);
        TextView totalMonthlyExpense = findViewById(R.id.expense_total_monthly);
        TextView totalMonthlyIncome = findViewById(R.id.income_total_monthly);
        TextView netEarning7days = findViewById(R.id.netEarnings_total_7days);
        TextView netEarningMonthly = findViewById(R.id.netEarnings_total_monthly);

        //build the selection statement to find expenses and income for the 'last 7 days' in the transaction table
        String selection1 = TransactionTable.COL8 + "=" + String.valueOf(TransactionTable.TRANS_TYPE1) + " AND " +
                TransactionTable.COL2+"<="+"date('now') AND "+
                TransactionTable.COL2 + ">=" + "date('now', '-7 days')";
        String selection2 = TransactionTable.COL8 + "=" + String.valueOf(TransactionTable.TRANS_TYPE2) + " AND " +
                TransactionTable.COL2+"<="+"date('now') AND "+
                TransactionTable.COL2 + ">=" + "date('now', '-7 days')";

        //get the cursor to track expenses for the last 7 days
        cursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection1, null, null);

        /* since the amount column does not use numeric types, it has to be added using customized class
        BigDecimalCalculator to get exact currency value */
        BigDecimal total7daysEx = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0.0, currencyCode)));
        try {
            while (cursor.moveToNext()) {
                total7daysEx = BigDecimalCalculator.add(total7daysEx.toString(),
                        cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        //get the cursor to track income for the last 7 days
        cursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection2, null, null);

        /* since the amount column does not use numeric types, it has to be added using customized class
        BigDecimalCalculator to get exact currency value */
        BigDecimal total7daysIn = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));
        try {
            while (cursor.moveToNext()) {
                total7daysIn = BigDecimalCalculator.add(total7daysIn.toString(),
                        cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        total7daysExpense.setText(CurrencyFormatter.format(this, total7daysEx.toString()));
        total7daysIncome.setText(CurrencyFormatter.format(this, total7daysIn.toString()));

        BigDecimal bigNetEarning7days = BigDecimalCalculator.subtract(total7daysIn.toString(),
                total7daysEx.toString());

        //if net Earning is greater than 0, set color 'green', equals 0, 'black(default)', less than, set 'red'
        if(bigNetEarning7days.doubleValue()>0) {
            netEarning7days.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }else if(bigNetEarning7days.doubleValue()<0) {
            netEarning7days.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        netEarning7days.setText(CurrencyFormatter.format(this, bigNetEarning7days.toString()));

        BigDecimal bigNetEarningMonthly = getMonthlyNetEarnings(0);

        totalMonthlyExpense.setText(CurrencyFormatter.format(this, totalMonthlyEx.toString()));
        totalMonthlyIncome.setText(CurrencyFormatter.format(this, totalMonthlyIn.toString()));


        if(bigNetEarningMonthly.doubleValue()>0) {
            netEarningMonthly.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        else if(bigNetEarningMonthly.doubleValue()<0){
            netEarningMonthly.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        netEarningMonthly.setText(CurrencyFormatter.format(this, bigNetEarningMonthly.toString()));

        setAssetsPieChart();

        //set earning percentage for this month
        setEarningProgress(totalMonthlyIn);

        //set budget summary pie chart in content main layout
        setBudgetSummaryPieChart(totalMonthlyEx);

        netEarningMonthlyCardView.setOnClickListener(listener);

    }

    private void setAssetsPieChart(){

        PieChart assetsPieChart = findViewById(R.id.assets_piechart);

        //sets the center void
        assetsPieChart.setDrawHoleEnabled(true);

        ArrayList<PieEntry> yValues = new ArrayList<>();

        //get all the accounts from the DataSource class
        ArrayList<Account> accounts = mDataSource.getAllAccounts();

        //this variable is going to be used for the total amount of assets
        BigDecimal assetTotal = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));

        //loop to get total assets as well as to get each entry for each account
        for(int i = 0; i<accounts.size(); i++){

            //get the total amount of current balances of all the accounts
            //if an account's current balance is less than 0, than do not add it to the total assets
            if(Float.parseFloat(accounts.get(i).getCurrent_balance())>0.0f) {
                //add the current balance of an account to the total asset
                assetTotal = BigDecimalCalculator.add(assetTotal.toString(), accounts.get(i).getCurrent_balance());
            }

            //if the current amount of an account is no more than 0, do not add it to the pie entry
            if(Float.parseFloat(accounts.get(i).getCurrent_balance())>0.0f){
                //the PieEntry Class takes float, String as parameters for its constructor
                yValues.add(new PieEntry(Float.parseFloat(accounts.get(i).getCurrent_balance()),
                        accounts.get(i).getName()));
            }
        }

        //put the total balance at the center of the chart
        assetsPieChart.setCenterText(String.format("Total\n%s", assetTotal.toString()));
        //set the size of the center text which in this chart represents the total amount
        assetsPieChart.setCenterTextColor(Color.rgb(63,81,181));
        assetsPieChart.setCenterTextSize(12);
        //set the margin of the chart
        assetsPieChart.setExtraOffsets(0, 5, 0, 0);
        //going to use percent values instead of original values
        assetsPieChart.setUsePercentValues(true);
        //erase the description of the chart that would appear right side of the card view
        assetsPieChart.getDescription().setEnabled(false);
        //set the size of entry label text
        assetsPieChart.setEntryLabelTextSize(10);
        assetsPieChart.setEntryLabelColor(Color.rgb(62,39,35));
        
        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(1.0f);
        dataSet.setSelectionShift(10f);

        //set colors for each part of the pie chart
        dataSet.setColors(Color.rgb(205,220,57), Color.rgb(121,85,72), Color.rgb(63,81,181), Color.rgb(3,169,244),
                Color.rgb(255,193,7), Color.rgb(244,67,54));

        PieData data = new PieData((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.rgb(62,39,35));

        //set the value formatter that sets the value string with '%' at the end
        data.setValueFormatter(new PercentFormatter());

        assetsPieChart.setData(data);
        assetsPieChart.setHoleRadius(35);
        assetsPieChart.setTransparentCircleRadius(0);

    }

    //this method is to get net Earnings for 1 specified month
    private BigDecimal getMonthlyNetEarnings(int offSet){

        String selection1="";
        String selection2="";

        /**
         * build the selection statement to find expenses and income for specified period(1 month)
         * for example, if offset is 0, that means this method is going to return net earnings for this month
         */

        if(offSet==0) {
            selection1 = TransactionTable.COL8 + "=" + String.valueOf(TransactionTable.TRANS_TYPE1) + " AND " +
                    TransactionTable.COL2 + "<=" + "date('now', 'start of month', '+1 month', '-1 day') AND " +
                    TransactionTable.COL2 + ">=" + "date('now', 'start of month')";
            selection2 = TransactionTable.COL8 + "=" + String.valueOf(TransactionTable.TRANS_TYPE2) + " AND " +
                    TransactionTable.COL2 + "<=" + "date('now', 'start of month', '+1 month', '-1 day') AND " +
                    TransactionTable.COL2 + ">=" + "date('now', 'start of month')";
        }else{
            selection1 = TransactionTable.COL8 + "=" + String.valueOf(TransactionTable.TRANS_TYPE1) + " AND " +
                TransactionTable.COL2 + "<=" + "date('now', 'start of month', '-" + (offSet-1) + " month', '-1 day') AND " +
                TransactionTable.COL2 + ">=" + "date('now', 'start of month', '-" + offSet + " month')";
            selection2 = TransactionTable.COL8 + "=" + String.valueOf(TransactionTable.TRANS_TYPE2) + " AND " +
                    TransactionTable.COL2 + "<=" + "date('now', 'start of month', '-" + (offSet-1) + " month', '-1 day') AND " +
                    TransactionTable.COL2 + ">=" + "date('now', 'start of month', '-" + offSet + " month')";
        }

        //get the cursor to track expenses for a specified month
        cursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection1, null, null);

        /* since the amount column does not use numeric types, it has to be added using customized class
        BigDecimalCalculator to get exact currency value */
        totalMonthlyEx = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));
        try {
            while (cursor.moveToNext()) {
                totalMonthlyEx = BigDecimalCalculator.add(totalMonthlyEx.toString(),
                        cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        //get the cursor to track income for the last 7 days
        cursor = getContentResolver().query(DataProvider.TRANSACTION_URI, TransactionTable.ALL_COLS,
                selection2, null, null);

        /* since the amount column does not use numeric types, it has to be added using customized class
        BigDecimalCalculator to get exact currency value */
        totalMonthlyIn = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));
        try {
            while (cursor.moveToNext()) {
                totalMonthlyIn = BigDecimalCalculator.add(totalMonthlyIn.toString(),
                        cursor.getString(cursor.getColumnIndex(TransactionTable.COL3)));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return BigDecimalCalculator.subtract(totalMonthlyIn.toString(),
                totalMonthlyEx.toString());
    }

    @SuppressLint("SetTextI18n")
    private void setEarningProgress(BigDecimal totalMonthlyIn) {

        CardView earningProgressView = findViewById(R.id.earning_progress_view);
        earningProgressView.setOnClickListener(listener);

        TextView expectedEarning = findViewById(R.id.earning_expectation);
        TextView earningStatus = findViewById(R.id.earning_status);

        ArrayList<InCategory> inCategories = mDataSource.getAllInCategories();

        BigDecimal totalExpectedEarning = new BigDecimal(String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));
        for(int i = 0; i<inCategories.size(); i++){
            InCategory inCategory = inCategories.get(i);
            /**
             *  add all the amount of each inCategory (the amount is in a string value)
             *  so the customized class BigDecimalCalculator is needed here to get the exact sum of the amount
             */
            totalExpectedEarning = BigDecimalCalculator.add(totalExpectedEarning.toString(), inCategory.getAmount());
        }

        expectedEarning.setText(CurrencyFormatter.format(this, totalExpectedEarning.toString()));

        BigDecimal earningPercent = new BigDecimal("0.00");
        //calculate the (exact earning) / (expected earning) using BigDecimal Class rounding half up

        TextView show_no_earnings = findViewById(R.id.show_no_earnings);
        ProgressBar earningProgressBar = findViewById(R.id.earning_progress_bar);

        try {
            earningProgressBar.setVisibility(VISIBLE);
            earningStatus.setVisibility(VISIBLE);
            show_no_earnings.setVisibility(GONE);
            earningPercent = totalMonthlyIn.divide(totalExpectedEarning, 3, RoundingMode.HALF_UP).
                    multiply(new BigDecimal("100"));
        }catch(ArithmeticException e){
            earningProgressBar.setVisibility(INVISIBLE);
            earningStatus.setVisibility(INVISIBLE);
            show_no_earnings.setVisibility(VISIBLE);
        }

        //set the big decimal number to have 2 digits after decimal point
        earningStatus.setText(String.format("%s%%", earningPercent.setScale(2)));

        ObjectAnimator anim = ObjectAnimator.ofInt(earningProgressBar, "progress", 0, earningPercent.intValue());

        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(2000);
        anim.start();
    }

    //when the app is launching for the first time, this method sets the info of default account
    private void setDefaultAccount(String name, String type, String balance) {

            ContentValues values = new ContentValues();
            values.put(AccountsTable.COL2, name);
            values.put(AccountsTable.COL3, type);
            values.put(AccountsTable.COL4, balance);
            values.put(AccountsTable.COL5, balance);
            getContentResolver().insert(DataProvider.ACCOUNTS_URI, values);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDataSource.open();
        reLoad();
    }

    private void setDefaultExCategory() {

        ContentValues values = new ContentValues();
        values.put(ExCategoryTable.COL2, "Others");
        values.put(ExCategoryTable.COL3, String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));
        getContentResolver().insert(DataProvider.EX_CATEGORY_URI, values);

    }

    private void setDefaultInCategory() {

        ContentValues values = new ContentValues();
        values.put(InCategoryTable.COL2, "Others");
        values.put(InCategoryTable.COL3, String.valueOf(BigDecimalCalculator.roundValue(0, currencyCode)));
        getContentResolver().insert(DataProvider.IN_CATEGORY_URI, values);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_main:
                Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                break;
            case R.id.nav_expense:
                Intent expenseIntent = new Intent(MainActivity.this, ShowingExpensesActivity.class);
                startActivity(expenseIntent);
                break;
            case R.id.nav_income:
                Intent incomeIntent = new Intent(MainActivity.this, ShowingIncomeActivity.class);
                startActivity(incomeIntent);
                break;
            case R.id.nav_accounts:
                Intent accountIntent = new Intent(MainActivity.this, ShowingAccountsActivity.class);
                startActivity(accountIntent);
                break;
            case R.id.nav_credit:
                Intent creditIntent = new Intent(MainActivity.this, ShowingCreditActivity.class);
                startActivity(creditIntent);
                break;
            case R.id.nav_budget:
                Intent budgetIntent = new Intent(MainActivity.this, BudgetActivity.class);
                startActivity(budgetIntent);
                break;
            case R.id.nav_income_category:
                Intent incomeCategoryIntent = new Intent(MainActivity.this, IncomeCategoryActivity.class);
                startActivity(incomeCategoryIntent);
                break;
            case R.id.nav_transfer:
                Intent transferActivity = new Intent(MainActivity.this, TransferActivity.class);
                startActivity(transferActivity);
                break;
            case R.id.nav_settings:
                Intent prefsActivity = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(prefsActivity);
                break;
            case R.id.nav_exit:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Do you want to exit?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, DataProvider.TRANSACTION_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {}

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    View.OnClickListener listener = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            switch(view.getId()){
                case R.id.fab:
                    Intent addIntent = new Intent(MainActivity.this, AddingExpenseActivity.class);
                    addIntent.putExtra(CHECK_IF, IS_FROM_MAIN_FAB);
                    startActivityForResult(addIntent, REQUEST_CODE);
                    break;
                case R.id.netEarnings_monthly_cardView:
                    MonthlyNetEarningsFragment fragment = new MonthlyNetEarningsFragment();
                    Bundle b = new Bundle();
                    double[] chart_yValues = new double[6];

                    for(int i = 0; i<6; i++) {
                        chart_yValues[i] = getMonthlyNetEarnings(i).doubleValue();
                    }

                    b.putDoubleArray("Y_VALUES", chart_yValues);
                    fragment.setArguments(b);
                    fragment.setCancelable(true);
                    fragment.show(getFragmentManager(), "CHART");
                    break;
                case R.id.earning_progress_view:
                    Intent incomeIntent = new Intent(MainActivity.this, ShowingIncomeActivity.class);
                    startActivity(incomeIntent);
                    break;
            }
        }
    };
}

