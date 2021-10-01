package com.example.csvimporter.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampStringConverter {
    public static Timestamp stringToTimestamp(String dateAsString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dt = sdf.parse(dateAsString);
            Timestamp ti = new Timestamp(dt.getTime());
            return ti;
        } catch (ParseException e) {
            return null;
        }
    }

    public static Timestamp convertToTimestamp(String value) {
        Date dt = parseDate(new SimpleDateFormat("MM/dd/yy"), value);
        if(dt!=null)
            return new Timestamp(dt.getTime());

        dt = parseDate(new SimpleDateFormat("MM/dd/yyyy"), value);
        if(dt!=null)
            return new Timestamp(dt.getTime());
        return null;
    }

    private static Date parseDate(SimpleDateFormat sdf, String stringValue) {
        try {
            return sdf.parse(stringValue);
        } catch (ParseException e) {
            return null;
        }
    }
}
