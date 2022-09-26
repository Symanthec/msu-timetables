package msu.timetable.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Subject is a POJO class
 */
public class Subject {

    /** Identifier of subject */
    private short id;

    /** Identifier getter */
    public short getId() {
        return id;
    }

    /** Identifier setter */
    public void setId(short id) {
        this.id = id;
    }

    /** Name of the subject */
    private String name = "UNNAMED";

    /** Name getter */
    public String getName() {
        return name;
    }

    /** Name setter */
    public void setName(String name) {
        this.name = name;
    }

    /** List of homeworks by their respective identifiers*/
    private List<Short> homeworks = new ArrayList<>();

    /** Getter for homeworks list */
    public List<Short> getHomeworks() {
        return homeworks;
    }

    /** Setter for homeworks list */
    public void setHomeworks(List<Short> newHomeworks) {
        this.homeworks = newHomeworks;
    }

    /** List containing dates of each lesson */
    private final List<Date> schedule = new ArrayList<>();

    /** Getter for schedule */
    public List<Date> getSchedule() {
        return schedule;
    }

    /** Setter for schedule */
    public void setSchedule(List<Date> newSchedule) {
        if (newSchedule != null) {
            schedule.clear();
            newSchedule.forEach(d -> {
//                avoid NullPointerException
                if (Objects.nonNull(d)) this.schedule.add(d);
            });
        }
    }

    /** Extra data */
    private JsonNode extras = JsonNodeFactory.instance.missingNode();

    /** Getter for extra date*/
    public JsonNode getExtras() {
        return extras;
    }

    /** Setter for extra date*/
    public void setExtras(JsonNode node) {
        extras = node;
    }
}
