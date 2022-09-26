package msu.timetable.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Logger;

public class Homeworks {

    private static final Homeworks instance = new Homeworks();
    private static final Subjects subjects = Subjects.getInstance();
    private static final Logger logger = Logger.getLogger("Homeworks");
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
    private final String filepath = "homeworks.json";
    private final File fileStore = new File(filepath);

    public static Homeworks getInstance() {
        return instance;
    }

    public Homeworks() {
        loadJSON();
    }

    private void loadJSON() {
        if (fileStore.exists()) {
            try {
                List<Homework> list = mapper.readValue(
                        fileStore,
                        TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Homework.class)
                );
                list.forEach(d -> homeworkList.put(d.getId(), d));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    TreeMap<Short, Homework> homeworkList = new TreeMap<>();

    public Homework createHomework(short subjectId, String description) {
//        check subject presence
        Subject hwSubject = subjects.getSubjectById(subjectId);
        if (hwSubject == null) {
            logger.warning("Invalid subject ID provided: " + subjectId);
            return null;
        }

//        allocate index
        short index = generator.generateIndex();

        Homework homework = new Homework();
        homework.setId(index);
        homework.setSubject(hwSubject.getId());
        homework.setDescription(description);

        homeworkList.put(index, homework);
        hwSubject.getHomeworks().add(index);
        return homework;
    }

    public void removeHomework(Homework hw) {
        homeworkList.remove(hw.getId());
    }

    public Homework getHomeworkById(short hwId) {
        return homeworkList.get(hwId);
    }

    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileStore, homeworkList.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final IndexGenerator generator = new IndexGenerator();


    private class IndexGenerator {

        private final Random random = new Random();

        private short nextIndex() {
            return (short) random.nextInt(Short.MAX_VALUE);
        }

        public short generateIndex() {
            short index;
            do {
                index = nextIndex();
            } while (homeworkList.containsKey(index));
            return index;
        }

    }
}
