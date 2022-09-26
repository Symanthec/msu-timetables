package msu.timetable.models;

import org.springframework.lang.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

/** Common date functions used by application */
public class DateUtils {

    /** Instance of Calendar used by dayOfWeek() function */
    private static final Calendar calendar = Calendar.getInstance();

    /**
     * Returns day of week for given date
     * @param date - date, which weekday needs to be retrieved
     */
    public static int dayOfWeek(@NonNull Date date) {
//      1. Recommended way of getting day of week

//      Since dayOfWeek() is intended to be used by multiple threads, calendar should be synchronized between them
        synchronized (calendar) {
            calendar.setTime(date);
            return calendar.get(Calendar.DAY_OF_WEEK);
        }

//      2. Deprecated way, but may be faster
//        return date.getDay();
    }

    /**
     * Predicate creator, which determines whether given Subject has lessons at the given weekday
     * @param weekday - weekday to be filtered
     */
    public static Predicate<Subject> hasLessonsAt(int weekday) {
        return s -> s.getSchedule().stream().anyMatch(date -> dayOfWeek(date) == weekday);
    }

}
