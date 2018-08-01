package com.hongjolim.mfmanager.tools;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/*this class is to convert date String on the screen and convert it to ISO format so that it can be
stored in SQLite base
*/
public class DateFormatConverter {

    //strings for months
    public static final String[] MONTH_STRINGS =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


    //takes original date string(MMMM/dd/yyyy) and convert it to the specific format as: (yyyy-MM-dd)
    public static String convertDateToISO(String dateString){

        String[] dateStrings = dateString.split(" ");
        int year = Integer.parseInt(dateStrings[2]);
        String monthString = dateStrings[0];
        int day = Integer.parseInt(dateStrings[1]);

        int month=0;

        //set the integer value for corresponding month string
        if(monthString.equals(MONTH_STRINGS[0])){
            month = 0;
        }else if(monthString.equals(MONTH_STRINGS[1])){
            month = 1;
        }else if(monthString.equals(MONTH_STRINGS[2])){
            month = 2;
        }else if(monthString.equals(MONTH_STRINGS[3])){
            month = 3;
        }else if(monthString.equals(MONTH_STRINGS[4])){
            month = 4;
        }else if(monthString.equals(MONTH_STRINGS[5])){
            month = 5;
        }else if(monthString.equals(MONTH_STRINGS[6])){
            month = 6;
        }else if(monthString.equals(MONTH_STRINGS[7])){
            month = 7;
        }else if(monthString.equals(MONTH_STRINGS[8])){
            month = 8;
        }else if(monthString.equals(MONTH_STRINGS[9])){
            month = 9;
        }else if(monthString.equals(MONTH_STRINGS[10])){
            month = 10;
        }else if(monthString.equals(MONTH_STRINGS[11])){
            month = 11;
        }

        //depends on the android version of the user's device
        //if the version of the device is higher than or equal to 26,
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            return DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.of(year, month+1, day));

            //if the version is lower than 26,
        }else{

            //get an instance of calendar object
            Calendar calendar = Calendar.getInstance();

            //set the date to the specific date
            calendar.set(year, month, day);

            //set the format
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.format(calendar.getTime());
        }
    }

    public static String convertDateToCustom(String dateString){

        String[] dateStrings = dateString.split("-");
        int year = Integer.parseInt(dateStrings[0]);
        int month = Integer.parseInt(dateStrings[1]);
        int day = Integer.parseInt(dateStrings[2]);

        //declare monthString to store
        String monthString = "";

        //set the month as String
        switch(month){
            case 1:
                monthString = MONTH_STRINGS[0];
                break;
            case 2:
                monthString = MONTH_STRINGS[1];
                break;
            case 3:
                monthString = MONTH_STRINGS[2];
                break;
            case 4:
                monthString = MONTH_STRINGS[3];
                break;
            case 5:
                monthString = MONTH_STRINGS[4];
                break;
            case 6:
                monthString = MONTH_STRINGS[5];
                break;
            case 7:
                monthString = MONTH_STRINGS[6];
                break;
            case 8:
                monthString = MONTH_STRINGS[7];
                break;
            case 9:
                monthString = MONTH_STRINGS[8];
                break;
            case 10:
                monthString = MONTH_STRINGS[9];
                break;
            case 11:
                monthString = MONTH_STRINGS[10];
                break;
            case 12:
                monthString = MONTH_STRINGS[11];
                break;

        }

        return monthString+" "+day+" "+year;
    }
}
