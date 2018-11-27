package ru.starfish24.mascotte.utils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static String formatDate(Date date, String format) {
        return null;
    }

    public static <T> T getFirst(Collection<T> collection) {
        if (collection == null) {
            return null;
        }
        for (T item : collection) {
            return item;
        }
        return null;
    }

    public static <T> Stream<T> stream(Collection<T> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

    public static Object prettyString(Map<String, Object> vars) {
        return vars.entrySet().stream().map(e -> e.getKey() + " : " + e.getValue()).collect(Collectors.joining(", "));
    }

    public static Date parse(String strDate, String format) throws ParseException {
        return new SimpleDateFormat(format).parse(strDate);
    }

    public static <T> T ofNullable(T object, T nullValue) {
        return isEmpty(object) ? nullValue : object;
    }

    public static <T> List<T> newArrayList(Collection<T> collection) {
        return collection == null ? new ArrayList<T>() : new ArrayList<T>(collection);
    }

    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        Class<?> valueClass = value.getClass();
        if (Number.class.isAssignableFrom(valueClass)) {
            return isEmpty((Number) value);
        }
        if (String.class.isAssignableFrom(valueClass)) {
            return isEmpty((String) value);
        }
        if (Collection.class.isAssignableFrom(valueClass)) {
            return isEmpty((Collection) value);
        }
        if (Map.class.isAssignableFrom(valueClass)) {
            return isEmpty((Map) value);
        }
        if (value.getClass().isArray()) {
            return isEmpty((Object[]) value);
        }
        return false;
    }

    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isNotEmpty(Number number) {
        return !isEmpty(number);
    }

    public static boolean isEmpty(Number number) {
        return number == null || number.intValue() == 0;
    }

    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

    public static BigDecimal toBigDecimal(Number number) {
        if (number == null)
            return BigDecimal.ZERO;
        if (BigDecimal.class.equals(number.getClass()))
            return (BigDecimal) number;
        if (Double.class.equals(number.getClass()))
            return new BigDecimal((Double) number);
        return new BigDecimal(number.toString());
    }

    public static BigDecimal subtract(BigDecimal value1, BigDecimal value2) {
        if (value1 == null) {
            value1 = BigDecimal.ZERO;
        }
        if (value2 == null) {
            value2 = BigDecimal.ZERO;
        }
        return value1.subtract(value2);
    }

    public static DecimalFormat priceFormat() {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("ru"));
        DecimalFormat priceFormat = (DecimalFormat) nf;
        priceFormat.applyPattern("### ###.##");

        return priceFormat;
    }

    public static SimpleDateFormat ruSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern, new Locale("ru"));
    }

    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault())
                .toInstant()) : null;
    }

    public static boolean isValidEmailAddress(String email) {
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
        } catch (AddressException ex) {
            return false;
        }
        return true;
    }
}
