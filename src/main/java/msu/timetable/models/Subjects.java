package msu.timetable.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Stream;

import static msu.timetable.models.DateUtils.dayOfWeek;
import static msu.timetable.models.DateUtils.hasLessonsAt;

/**
 * Subjects contains all Subject classes and provides access to them
 */
public final class Subjects {

//    private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("Subjects singleton");

    /** Subjects exploits singleton pattern */
    private static final Subjects singleton = new Subjects();

    /**
     * Object mapper is required for serialization and deserialization.
     * Between runs data is stored in JSON format
     */
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    /** Singleton acquisition method */
    public static Subjects getInstance() {
        return singleton;
    }

    /** Path to the file containing JSON data */
    private final String filepath = "subjects.json";

    /** File object which stores JSON data */
    private final File fileStore = new File(filepath);

    /** Singleton constructor. Involves cache initialization, JSON parsing, cache generation */
    private Subjects() {
        initCache();
        loadJSON();
        updateAllCache();
    }

    /** Method for loading JSON data */
    private void loadJSON() {
        if (fileStore.exists()) {
            try {
                Set<Subject> list = mapper.readValue(fileStore, TypeFactory.defaultInstance().constructCollectionLikeType(Set.class, Subject.class));
                list.forEach(s -> subjectsList.put(s.getId(), s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** subjectList stores all "Subject"s and provides access to them by their ID */
    private final TreeMap<Short, Subject> subjectsList = new TreeMap<>();

    /** Returns immutable collection of all subjects */
    public Collection<Subject> getAllSubjects() {
        return subjectsList.values();
    }

    /**
     * Creates new subject with given name and schedule
     * @param name non null name
     * @param schedule may be null
     * */
    public Subject createSubject(@NonNull String name, @Nullable List<Date> schedule) {
        Subject subject = new Subject();

        short index = generator.generateIndex();
        subject.setId(index);
        subject.setName(name);
        subject.setSchedule(schedule);

        for (Date lesson :
                subject.getSchedule()) {
            int weekday = dayOfWeek(lesson);
            dropCacheForDay(weekday);
        }

        subjectsList.put(index, subject);
        return subject;
    }

    /**
     * Returns Subject with given id
     * @param id - Subject identifier
     * */
    public Subject getSubjectById(short id) {
        return subjectsList.getOrDefault(id, null);
    }

    /**
     * Returns first Subject with given name
     * @param name - name of the subject
     * */
    public Subject getSubjectByName(String name) {
        Optional<Subject> candidate = subjectsList
                .values()
                .stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
        return candidate.orElse(null);
    }

    /**
     * Returns all subjects, which are taught on given weekday
     * @param weekday - weekday number from 1 (MONDAY) to 7 (SUNDAY)
     */
    public Collection<Subject> getSubjectsOn(int weekday) {
        weekday %= cacheCapacity;
        List<Subject> response = weekdaySubjectCache[weekday];

//        logger.info("Cache for " + DayOfWeek.of(weekday) + " was" + (response == null ? "n't": "") +" found");
        if (response == null)
            response = createCacheForDay(weekday, weekdaySubjectCache[weekday]);

        return response;
    }

    /** Saves all subjects in JSON file located at "filepath" variable */
    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileStore, subjectsList.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runtime constant which determines size of cache
     * Equal to number of weekdays in a week (7 in 2022)
     */
    private final int cacheCapacity = DayOfWeek.values().length;

    /**
     * Weekday subject cache is used by getSubjectOn(int weekday): Subject function
     * To avoid repetitive filtering subjects by weekday and ordering of them by date, cache is used.
     *
     */
    private List<Subject>[] weekdaySubjectCache = null;

    /** Creates weekdaySubjectCache list and fills it with nulls*/
    private void initCache() {
        //noinspection unchecked
        weekdaySubjectCache = new List[cacheCapacity];
        for (int i = 0; i < cacheCapacity; i++) {
            weekdaySubjectCache[i] = new LinkedList<>();
        }
    }

    /** Renews all cache */
    @SuppressWarnings("unchecked")
    private void updateAllCache() {
        List<Subject>[] cache = new List[cacheCapacity];

        for (DayOfWeek weekday :
                DayOfWeek.values()) {
            int index = weekday.getValue() - 1;
            cache[index] = new LinkedList<>();
            createCacheForDay(index + 1, cache[index]);
        }

        weekdaySubjectCache = cache;
    }

    /**
     * Reset cache for specific weekday
     * @param weekday - number of weekday
     */
    private void dropCacheForDay(int weekday) {
        weekdaySubjectCache[weekday - 1] = null;
    }

    /** Creates cache for specific day and puts it into output list
     * @param weekday - number of weekday
     * @param output - non-null list, to which the cache will be inserted
     */
    private List<Subject> createCacheForDay(int weekday, @NonNull List<Subject> output) {
//        logger.info("Updating weekday cache for " + DayOfWeek.of(weekday));
        TreeMap<Date, Short> subjectTree = new TreeMap<>();

        Stream<Subject> filtered = getAllSubjects().stream().filter(hasLessonsAt(weekday));
        filtered.forEach(s -> {
            List<Date> dates = s.getSchedule();
            dates.stream().filter(d -> dayOfWeek(d) == weekday).forEach(d ->
                    subjectTree.put(d, s.getId())
            );
        });

        List<Subject> cache = subjectTree.values().stream().map(this::getSubjectById).toList();
        output.clear();
        output.addAll(cache);
        return output;
    }

    /** Instance of IndexGenerator */
    private final IndexGenerator generator = new IndexGenerator();

    /** Utility class for index generation. Helpful for encapsulation of index generation */
    private class IndexGenerator {

        private final Random random = new Random();

        /** Generates new index, which, however, may already be occupied by older subjects */
        private short nextIndex() {
            return (short) random.nextInt(Short.MAX_VALUE);
        }

        /** Generates new index to be used by newly created subject */
        public short generateIndex() {
            short index;
            do {
                index = nextIndex();
            } while (subjectsList.containsKey(index));
            return index;
        }

    }

}
