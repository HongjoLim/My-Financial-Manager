package com.hongjolim.mfmanager.tools;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

public class BigDecimalCalculator {

    public static double roundValue(double value, String countryCode){

        Locale locale = null;

        switch(countryCode){
            //country is set Canada
            case "Canada":
                locale = Locale.CANADA;
                break;
            case "US":
                locale = Locale.US;
                break;
            case "China":
                locale = Locale.CHINA;
                break;
            case "France":
                locale = Locale.FRANCE;
                break;
            case "Germany":
                locale = Locale.GERMANY;
                break;
            case "Japan":
                locale = Locale.JAPAN;
                break;
            case "Korea":
                locale = Locale.KOREA;
                break;
            default:
                locale = Locale.CANADA;
        }

        Currency currency = Currency.getInstance(locale);

        return Double.parseDouble(BigDecimal.valueOf(value).setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP).toString());
    }

    public static BigDecimal subtract(String value1, String value2){
        BigDecimal big1 = new BigDecimal(value1);
        BigDecimal big2 = new BigDecimal(value2);

        return big1.subtract(big2);

    }

    public static BigDecimal add(String value1, String value2){

        BigDecimal big1 = new BigDecimal(value1);
        BigDecimal big2 = new BigDecimal(value2);

        return big1.add(big2);

    }
}
