package msu.timetable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * POJO for API responses
 */
// TODO think of moving APIResponse into enumeration
public class APIResponse {

    private static final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    /** Response, that returns nothing and display success */
    public static final APIResponse SUCCESS = new APIResponse(0, "Success");

    /** Response, that returns nothing and indicates, that something is not found */
    public static final APIResponse NOT_FOUND = new APIResponse(-1, "Not found");

    /** Integer status of response (0 for success, other number for failure)*/
    private final int status;

    /** Getter for status */
    public int getStatus() {
        return status;
    }

    /** Date held by API response */
    private final JsonNode data;

    /** Getter for data*/
    public JsonNode getData() {
        return data;
    }

//    public APIResponse(int id, JsonNode extras) {
//        this.status = id;
//        this.data = extras == null ? NullNode.getInstance(): extras;
//    }

    /** Default constructor */
    public APIResponse(int status, Object data) {
        JsonNode candidate;
        try {
            candidate = mapper.valueToTree(data);
        } catch (IllegalArgumentException e) {
            candidate = NullNode.getInstance();
        }

        this.status = status;
        this.data = candidate;
    }
}
