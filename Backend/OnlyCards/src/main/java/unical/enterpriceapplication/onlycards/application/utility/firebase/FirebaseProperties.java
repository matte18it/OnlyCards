package unical.enterpriceapplication.onlycards.application.utility.firebase;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.firebase")
public class FirebaseProperties {
    private String bucket;
}
