/*
 * DateUtils.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.underworldlabs.util;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>This class was modified and included as a part of the Package
 * <code>org.executequery.util</code> in the application Execute Query.
 *
 * Original authorship belongs to The Apache Software Foundation.
 * <p>
 * Copyright (c) 1999 The Apache Software Foundation.
 *
 * Takis Diakoumis 2002
 */

/**
 *
 * @author   Takis Diakoumis
 */
public class DateUtils {
    
    private Calendar calendar;
    private TimeZone timezone;
    private Locale locale;
    
    private SimpleDateFormat dateFormat;
    
    private static final String DASH = "-";
    private static final String COLON = ":";
    private static final String SPACE = " ";
    private static final String ZERO = "0";
    
    public DateUtils() {

        timezone = TimeZone.getTimeZone(System.getProperty("user.country"));
        locale = new Locale(System.getProperty("user.language"), System.getProperty("user.timezone"));
        calendar = Calendar.getInstance();
    }
    
    public DateUtils(String format) {
        this();
        dateFormat = new SimpleDateFormat(format);
    }
    
    public void reset() {
        calendar = Calendar.getInstance(timezone, locale);
    }

    public long getTimeInMillis() {
        return calendar.getTimeInMillis();
    }
    
    public String getFormattedDate() {
        return dateFormat.format(calendar.getTime());
    }
    
    public void resetTimeZone(String timezoneString, String language, String country) {
        timezone = TimeZone.getTimeZone(timezoneString);
        locale = new Locale(language, country);
    }
    
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }
    
    public String getMonth() {
        int m = getMonthInt();
        String[] months = new String [] { "Jan", "Feb", "Mar",
                                          "Apr", "May", "Jun",
                                          "Jul", "Aug", "Sep",
                                          "Oct", "Nov", "Dec" };
        if (m > 12) {
            return "Unknown to Man";
        }
        
        return months[m - 1];
    }
    
    public String getDay() {
        int x = getDayOfWeek();
        String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday",
                                        "Thursday", "Friday", "Saturday"};
        
        if (x > 7) {
            return "Unknown to Man";
        }
        
        return days[x - 1];
    }
    
    public int getMonthInt() {
        return 1 + calendar.get(Calendar.MONTH);
    }
    
    public String getDate() {
        String year = Integer.toString(getYear());
        return getDayOfMonth() + DASH + getMonth() + DASH + year.substring(2);
    }
    
    public String getDate(char delimeter) {
        String year = Integer.toString(getYear());
        return getDayOfMonth() + delimeter + getMonth() +
               delimeter + year.substring(2);
    }
    
    public String getDateInt(String delimeter) {
        String year = Integer.toString(getYear());
        return getDayOfMonth() + delimeter + getMonthInt() +
               delimeter + year.substring(2);
    }
    
    public String getTime() {
        return getHour() + COLON + getMinute();
    }
    
    public String getLongTime() {
        return getHour() + COLON + getMinute() + COLON + getSecond();
    }
    
    public String getDateTime() {
        return getDate() + SPACE + getTime();
    }
    
    public String getLongDateTime() {
        return getDayOfMonth() + DASH + getMonth() + DASH +
        getYear() + SPACE + getLongTime();
    }
    
    public int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    public int getDayOfWeek() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    
    public int getWeekOfMonth() {
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }
    
    public int getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    
    public String getSecond() {
        int tempSecond = calendar.get(Calendar.SECOND);
        return tempSecond < 10 ? ZERO + tempSecond : Integer.toString(tempSecond);
    }
    
    public String getMinute() {
        int tempMinute = calendar.get(Calendar.MINUTE);
        return tempMinute < 10 ? ZERO + tempMinute : Integer.toString(tempMinute);
    }
    
    public int getMinuteForCalc() {
        return calendar.get(Calendar.MINUTE);
    }
    
}







