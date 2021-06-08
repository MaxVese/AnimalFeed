package ru.sem.animalfeed.utils;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtilsA {

    public static DateTimeFormatter formatD = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static DateTimeFormatter formatDFull = DateTimeFormatter.ofPattern("EEE, d MMM\nв HH:mm");
    public static DateTimeFormatter formatD2 = DateTimeFormatter.ofPattern("EEE\nв HH:mm");
    public static DateTimeFormatter formatT =  DateTimeFormatter.ofPattern("HH:mm");
    public static DateTimeFormatter formatDT =  DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static DateTimeFormatter formatSql =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    /*
    Это константа для обозначения периода следующего кормления
    Должна быть Calendar.DATE, но для тестов Calendar.MINUTE
     */
    public static final int nextFieldValue = Calendar.DAY_OF_MONTH;

    public static Date getDateFromString(String dtString){
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        Date dt;
        try {
            dt = format.parse(dtString);
        } catch (ParseException e) {
            e.printStackTrace();
            dt= Calendar.getInstance().getTime();
        }
        return dt;
    }

    public static String getCurrentDateString(){
        return formatD.format(LocalDateTime.now());
    }

    public static String getCurrentTimeString(){
        return formatT.format(LocalDateTime.now());
    }

    public static String getStringDateTime(LocalDateTime date){
        return formatD.format(date)+" "+formatT.format(date);
    }

    /*public static String toSqlDate(String dt){
        Date date = getDateFromString(dt);
        return formatSql.format(date);
    }

    public static String sqlDateToString(String dt){
        Date date = null;
        try {
            date = formatSql.parse(dt);
        } catch (ParseException e) {
            e.printStackTrace();
            date= Calendar.getInstance().getTime();
        }
        return getStringDateTime(date);
    }

    public static String getSqlFromDate(String fromDate){
        try {
            return formatSql.format(
                    formatD.parse(fromDate.substring(2, 12)+" 00:00"));
        } catch (ParseException e) {
            e.printStackTrace();
            return formatSql.format(new Date());
        }
    }

    public static String getSqlToDate(String fromDate){
        try {
            return formatSql.format(
                    formatD.parse(fromDate.substring(3, 13)+" 00:00"));
        } catch (ParseException e) {
            e.printStackTrace();
            return formatSql.format(new Date());
        }
    }*/

    public static boolean isLocalDateInTheSameWeek(LocalDate date1, LocalDate date2) {
        LocalDate sundayBeforeDate1 = date1.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate saturdayAfterDate1 = date1.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        return  ((date2.isEqual(sundayBeforeDate1) || date2.isAfter(sundayBeforeDate1))
                && (date2.isEqual(saturdayAfterDate1) || date2.isBefore(saturdayAfterDate1)));
    }

    public static LocalDateTime calculateNextFeedMorningOrEvening(LocalDateTime lastFeed, LocalTime morning, LocalTime evening){
        return calculateNextFeedMorningOrEvening(lastFeed, morning, evening, 1);
    }

    public static LocalDateTime calculateNextFeedMorningOrEvening(LocalDateTime lastFeed, LocalTime morning, LocalTime evening, int interval){
        LocalDateTime morningTime = lastFeed.with(morning);
        LocalDateTime eveningTime = lastFeed.with(evening);
        if(lastFeed.isBefore(morningTime)){
            //утреннго кормления еще не было
            return morningTime;
        }else{
            if(lastFeed.isBefore(eveningTime)){
                //вечернего кормления еще не было
                return eveningTime;
            }else{
                return morningTime.plusDays(interval);
            }
        }
    }

    public static String getYearString(int age){
        if(age==1) return "Год";
        if(age>=2 && age <=4) return "Года";
        return "Лет";
    }

    public static Period calculateTime(LocalDateTime birthDay) {
        /*Duration showSeconds = Duration.between(birthDay, LocalDateTime.now());
        Long timeSeconds = showSeconds.getSeconds();
        long days = timeSeconds / 86400; // 24*60*60
        long hours = timeSeconds / 3600;
        long minutes = (timeSeconds % 3600) / 60;
        long seconds = (timeSeconds % 3600) % 60;

        System.out.println("Days: " + days);
        System.out.println("Hours: " + hours);
        System.out.println("Minutes: " + minutes);
        System.out.println("Seconds: " + seconds);*/
        return Period.between(birthDay.toLocalDate(), LocalDate.now());
        /*System.out.println("You are " + p.getYears() + " years, " + p.getMonths() +
                " months and " + p.getDays() +
                " days old.");*/
    }
}
