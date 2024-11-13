package unical.enterpriceapplication.onlycards.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nimbusds.jose.shaded.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceError {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
    private String url;
    private String message;

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp", timestamp.toString());
        jsonObject.addProperty("url", url);
        jsonObject.addProperty("message", message);
        return jsonObject;

    }
}
