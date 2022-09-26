package msu.timetable.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import msu.timetable.APIResponse;
import msu.timetable.models.Subject;
import msu.timetable.models.Subjects;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Controller of all endpoints related to Subject manipulation
 * Quick note: since web browsers don't have direct access to Subject and Homework objects,
 * requests point to them by their IDs
 */
@RestController
@ComponentScan(basePackageClasses = Subjects.class)
public class SubjectController {

    private final Subjects subjects = Subjects.getInstance();

    /**
     * Creates new Subject with given name and optional schedule
     * @param name - name of subject
     * @param schedule - schedule of subject
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/createSubject")
    public APIResponse createSubject(@RequestParam String name, @RequestParam(value = "schedule", required = false) List<Long> schedule) {
        if (name.isEmpty() || name.isBlank())
            return new APIResponse(-1, "Name is either empty or blank");

        Subject subject = subjects.createSubject(
                name,
                schedule != null ? schedule.stream().map(Date::new).toList(): null
        );

        return new APIResponse(isObjectNull(subject), subject);
    }

    /**
     * Returns info on one subject
     * @param id - id of the subject
     * @param name - name of the subject, if results for given ID aren't found
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    // TODO is name required?
    @GetMapping("/api/getSubjectInfo")
    public APIResponse getSubjectInfo(@RequestParam("id") short id, @RequestParam(value = "name", required = false) String name) {
        Logger.getAnonymousLogger().info("/api/getSubjectInfo with params id = %x, name = %s".formatted(id, name));
        Subject result = subjects.getSubjectById(id);
        result = result != null ? result : subjects.getSubjectByName(name);

        return new APIResponse(
                isObjectNull(result),
                result
        );
    }

    /**
     * Returns list of all existing subjects
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/getSubjectList")
    public APIResponse getSubjects() {
        Collection<Subject> subjectList = subjects.getAllSubjects();
        return new APIResponse(0, subjectList);
    }

    /**
     * Returns all subjects, which have lessons in the given weekday
     * @param weekday - weekday in range [1;7] (Mon - Sun)
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/getSubjectsOn")
    public APIResponse getSubjectsOn(@RequestParam("day") int weekday) {
        if (weekday < 1 || weekday > 7)
            return new APIResponse(1, "Invalid week day");
        return new APIResponse(0, subjects.getSubjectsOn(weekday));
    }

    /**
     * Renames Subject
     * @param subject - id of the subject
     * @param name - new name for subject
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/setSubjectName")
    public APIResponse renameSubject(@RequestParam short subject, @RequestParam String name) {
        Subject sub = subjects.getSubjectById(subject);
        if (Objects.isNull(sub)) {
            return new APIResponse(-1, "Subject with ID=%x not found".formatted(subject));
        } else {
            sub.setName(name);
            return APIResponse.SUCCESS;
        }
    }

    /**
     * Assigns new schedule to subject
     * @param subject - id of the subject
     * @param schedule - new schedule for subject
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/setSubjectSchedule")
    public APIResponse setSubjectSchedule(@RequestParam short subject, @RequestParam List<Date> schedule) {
        Subject sub = subjects.getSubjectById(subject);
        if (Objects.isNull(sub)) {
            return new APIResponse(-1, "Subject with ID=%x not found".formatted(subject));
        } else {
            sub.setSchedule(schedule);
            return APIResponse.SUCCESS;
        }
    }

    // JSON parser used in setSubjectExtras()
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    // Base64 decoder used in setSubjectExtras()
    // TODO move Base64 decoding into function stored elsewhere
    private final Base64.Decoder b64Decoder = Base64.getUrlDecoder();

    /**
     * Assigns new extra data to given Subject
     * @param subject - id of the subject
     * @param extras - base64 encoded JSON data
     * @return APIResponse{ int status; JsonNode data }
     * @see APIResponse
     */
    @GetMapping("/api/setSubjectExtras")
    public APIResponse setSubjectExtras(@RequestParam short subject, @RequestParam String extras) {
        Subject sub = subjects.getSubjectById(subject);
        if (Objects.isNull(sub)) {
            return new APIResponse(-1, "Subject with ID=%x not found".formatted(subject));
        } else {
            byte[] b64 = b64Decoder.decode(extras);
            try {
                sub.setExtras(mapper.readTree(b64));
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
        subjects.save();
    }

}
