package com.statsmind.commons;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DateUtils {

    private static java.util.Date EARLIEST;
    private static java.util.Date LATEST;

    static {
        try {
            EARLIEST = new SimpleDateFormat("yyyy-MM-dd").parse("1850-01-01");
            LATEST = new SimpleDateFormat("yyyy-MM-dd").parse("2099-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static String format(Date date) {
        if (date == null) {
            return null;
        }

        return format(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String format(java.util.Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static String format(java.util.Date date) {
        if (date == null) {
            return null;
        }

        return format(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }

        return format(date, "yyyy-MM-dd");
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return format(timestamp, "yyyy-MM-dd");
    }

    public static String fixTime(String time, String regex) {
        if (StringUtils.isBlank(time) || !Pattern.matches(".*\\d.*", time)) {
            return null;
        }
//        date = date.replaceAll("[\\u4e00-\\u9fa5]","-");
        time = time.replaceAll(regex, "-");
        time = time.replaceAll("_+", "-");
        time = time.replaceAll("/+", "-");
        time = time.replaceAll("-+", "-");

        if (time.indexOf("-") == 0) {
            time = time.replaceFirst("-", "");
        }
        if (time.lastIndexOf("-") == time.length() - 1) {
            time = time.substring(0, time.length() - 1);
        }

        return time;
    }

    public static Date parseDate(String date) throws ParseException {
        String tDate = fixTime(date, "[^0-9_\\-/]+");
        if (tDate == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatePattern.getDatePattern(tDate));

        java.util.Date theDate = simpleDateFormat.parse(tDate);
        if (theDate == null) {
            return null;
        }

        if (theDate.before(EARLIEST) || theDate.after(LATEST)) {
            return null;
        }

        return new Date(theDate.getTime());
    }


    public static Timestamp parseDateTime(String dateTime) throws ParseException {

        String tDateTime = fixTime(dateTime, "[^0-9_\\-/:]+ ");

        if (tDateTime == null) {
            return null;
        }

        if (tDateTime.matches("^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$")) {
            tDateTime = tDateTime + " 00:00:00";
        } else if (tDateTime.matches("^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}$")) {
            tDateTime = tDateTime + ":00:00";
        } else if (tDateTime.matches("^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}$")) {
            tDateTime = tDateTime + ":00";
        } else if (tDateTime.matches("^[0-9]{4}-[0-9]{1,2}$")) {
            tDateTime = tDateTime + "-01 00:00:00";
        } else if (tDateTime.matches("^[0-9]{4}$")) {
            tDateTime = tDateTime + "-01-01 00:00:00";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date theDate = simpleDateFormat.parse(tDateTime);
        if (theDate == null) {
            return null;
        }


        if (theDate.before(EARLIEST) || theDate.after(LATEST)) {
            return null;
        }

        return new Timestamp(theDate.getTime());
    }

    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp afterTimeMillis(long timeMillis) {
        return new Timestamp(System.currentTimeMillis() + timeMillis);
    }

    public static Timestamp now(long seconds) {
        return new Timestamp(System.currentTimeMillis() + seconds * 1000L);
    }

    public static boolean isBefore(Timestamp t1, Timestamp t2) {
        return t1.before(t2);
    }

    public static boolean isAfter(Timestamp t1, Timestamp t2) {
        return t1.after(t2);
    }

    public static Date asDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return new Date(localDate.atStartOfDay().atZone(getDefaultZoneId()).toEpochSecond() * 1000);
    }

    public static ZoneId getDefaultZoneId() {
        return ZoneId.of("Asia/Chongqing");
    }

    public static Date asDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return new Date(localDateTime.atZone(getDefaultZoneId()).toEpochSecond() * 1000);
    }

    public static LocalDate asLocalDate(Date date) {
        if (date == null) {
            return null;
        }

        return Instant.ofEpochMilli(date.getTime()).atZone(getDefaultZoneId()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return Instant.ofEpochMilli(date.getTime()).atZone(getDefaultZoneId()).toLocalDateTime();
    }

    public static final class DatePattern {

        @Description(value = "yyyy")
        public static final String FOUR_NUMBER = "^[0-9]{0,4}$";

        @Description(value = "yyyyMM")
        public static final String SIX_NUMBER = "^[0-9]{5,6}$";

        @Description(value = "yyyyMMdd")
        public static final String EIGHT_NUMBER = "^[0-9]{8}$";

        @Description(value = "yyyy_MM_dd")
        public static final String UNDERLINE_SPLIT = "^[0-9]{4}(_[0-9]{1,2}){2}$";

        @Description(value = "yyyy/MM/dd")
        public static final String SLASH_SPLIT = "^[0-9]{4}(/[0-9]{1,2}){2}$";

//        @TermDescription(value = "yyyy/MM/dd")
//        public static final String GENERAL_PURPOSE_SPLIT = "^[0-9]{4}([/_-][0-9]{1,2}){2}$";


        @Description(value = "yyyy-MM-dd")
        public static final String DEFAULT = "^-?[0-9]{1,4}(-[0-9]{0,2}){0,2}-?$";

        public static final Map<String, String> PATTERN_2_DATEPATTERN = new HashMap<>();

        static {
            Field[] declaredFields = DatePattern.class.getDeclaredFields();
            Arrays.stream(declaredFields).forEach(declaredField -> {
                Description annotation = declaredField.getAnnotation(Description.class);
                if (annotation != null) {
                    try {
                        PATTERN_2_DATEPATTERN.put(declaredField.get(null).toString(), annotation.value());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public static String getDatePattern(String date) {
            String datePattern = PATTERN_2_DATEPATTERN.keySet().stream().filter(m -> Pattern.matches(m, date)).findFirst().orElse(null);
            return datePattern == null ? PATTERN_2_DATEPATTERN.get(DEFAULT) : PATTERN_2_DATEPATTERN.get(datePattern);
        }
    }

//    public static void main(String[] args) throws ParseException,NullPointerException {
//        System.out.println(parseDateTime("2019/3_24 0").toString());
////        System.out.println(parseDate("3").toString());
//    }
}
