package msu.timetable.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import msu.timetable.APIResponse;
import msu.timetable.models.Homework;
import msu.timetable.models.Homeworks;
import msu.timetable.models.Subject;
import msu.timetable.models.Subjects;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Controller of all endpoints related to homework manipulation
 * Quick note: since web browsers don't have direct access to Subject and Homework objects,
 * requests point to them by their IDs
 */
@RestController
public class HomeworkController {

    private final Homeworks homeworks = Homeworks.getInstance();
    private final Subjects subjects = Subjects.getInstance();

    /**
     * Creates new Homework for Subject with "subject" ID and "b64" content
     * @param subject - ID of Subject
     * @param b64 - Base64 encoded content
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/createHomework")
    public APIResponse createHomework(@RequestParam short subject, @RequestParam("content") String b64) {
        String content = new String(b64Decoder.decode(b64));
        Homework hw = homeworks.createHomework(subject, content);
        return new APIResponse(isObjectNull(hw), hw);
    }

    /**
     * Deletes Homework
     * @param homework - id of the homework */
    @GetMapping("/api/removeHomework")
    public APIResponse removeHomework(@RequestParam short homework) {
        Homework hw = homeworks.getHomeworkById(homework);
        if (hw == null) return APIResponse.NOT_FOUND;

        short subId = hw.getSubject();
        Subject sub = subjects.getSubjectById(subId);
        if (sub != null)
            sub.getHomeworks().remove(subId);
        homeworks.removeHomework(hw);
        return APIResponse.SUCCESS;
    }

    /**
     * Returns all homeworks for subject
     * @param subject - id of the subject
     * */
    @GetMapping("/api/getSubjectHomework")
    public APIResponse getHomeworksFor(@RequestParam short subject) {
        Subject sub = subjects.getSubjectById(subject);
        if (sub == null) return APIResponse.NOT_FOUND;

        List<Short> homeworkIds = sub.getHomeworks();
        List<Homework> subHomeworks = homeworkIds.stream().map(homeworks::getHomeworkById).toList();
        return new APIResponse(0, subHomeworks);
    }

    /**
     * Assigns Homework with "homework" ID to subject with "subject" ID
     * @param subject - id of subject
     * @param homework - id of homework
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     * */
    @GetMapping("/api/setHomeworkSubject")
    public APIResponse setSubject(@RequestParam short homework, @RequestParam short subject) {
        Homework hw = homeworks.getHomeworkById(homework);
        if (hw != null && subjects.getSubjectById(subject) != null) {
            hw.setSubject(subject);
            return APIResponse.SUCCESS;
        } else {
            return APIResponse.NOT_FOUND;
        }
    }

    /**
     * Assigns new content to given Homework
     * @param homework - id of homework
     * @param b64 - Base64 encoded content
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/setHomeworkContent")
    public APIResponse setHomeworkContent(@RequestParam short homework, @RequestParam("content") String b64) {
        try{
            String content = new String(b64Decoder.decode(b64));
            Homework hw = homeworks.getHomeworkById(homework);
            if (hw == null) return APIResponse.NOT_FOUND;
            hw.setDescription(content);
        } catch (IllegalArgumentException e) {
            return new APIResponse(-2, e.getMessage());
        }
        return APIResponse.SUCCESS;
    }

    /**
     * Assigns new date to given Homework
     * @param homework - id of Homework
     * @param timestamp - milliseconds timestamp of new due date
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/setHomeworkDate")
    public APIResponse setHomeworkDate(@RequestParam short homework, @RequestParam long timestamp) {
        Homework hw = homeworks.getHomeworkById(homework);
        if (hw == null) return APIResponse.NOT_FOUND;
        hw.setDueDate(new Date(timestamp));
        return APIResponse.SUCCESS;
    }



    // JSON parser user in setHomeworkExtras()
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    // Base64 decoder used for String decoding
    // TODO move Base64 decoding into function stored elsewhere
    private final Base64.Decoder b64Decoder = Base64.getUrlDecoder();

    /**
     * Assigns new extra data to given Homework
     * @param homework - id of the homework
     * @param extras - base64 encoded JSON data
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/setHomeworkExtras")
    public APIResponse setHomeworkExtras(@RequestParam short homework, @RequestParam String extras) {
        Homework hw = homeworks.getHomeworkById(homework);
        if (Objects.isNull(hw)) {
            return new APIResponse(-1, "Homework with ID=%x not found".formatted(homework));
        } else {
            byte[] b64 = b64Decoder.decode(extras);
            try {
                hw.setExtras(mapper.readTree(b64));
            } catch (IOException e) {
                Logger.getGlobal().warning("Bad JsonNode data: " + new String(b64));
                return new APIResponse(-1, "Bad data");
            }
            return APIResponse.SUCCESS;
        }
    }

    /** Checks whether given Object is null, and returns C-like boolean (1 for true or 0 for false) */
    //    TODO remove to elsewhere
    private int isObjectNull(Object obj) {
        return obj == null ? 1 : 0;
    }

    /** Method executed before server shutdown responsible for data saving */
    @PreDestroy
    public void onShutdown() {
        homeworks.save();
    }

}