package msu.timetable.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.lang.NonNull;

import java.util.Date;

/** POJO Homework class */
public class Homework {

    /** Homework unique identifier */
    private short id;

    /** ID getter */
    public short getId() {
        return id;
    }

    /** ID setter */
    public void setId(short id) {
        this.id = id;
    }

    /**
     * Identifier of subject, to which Homework belongs
     * Cannot use Subject itself, since then saving object into JSON may result in infinite loop
     */
//    TODO solve infinite loop problem
    private short subject;

    /** Subject ID getter */
    public short getSubject() {
        return subject;
    }

    /** Subject ID setter */
    public void setSubject(short subject) {
        this.subject = subject;
    }

    /** Homework description. Primarily needed for listing tasks */
    private String description = "UNNAMED HOMEWORK";

    /** Description getter */
    public String getDescription() {
        return description;
    }

    /** Description setter */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Date, until which homework should be done or sent */
    private Date dueDate = new Date();

    /** Due date getter */
    public Date getDueDate() {
        return dueDate;
    }

    /** Due date setter */
    public void setDueDate(@NonNull Date dueDate) {
        this.dueDate = dueDate;
    }

    /** Extra data for homework. May contain web links, paths to images or anything else */
    private JsonNode extras = JsonNodeFactory.instance.missingNode();

    /** Extra data getter */
    public JsonNode getExtras() {
        return extras;
    }

    /** Extra data setter */
    public void setExtras(JsonNode extras) {
        this.extras = extras;
    }

}
