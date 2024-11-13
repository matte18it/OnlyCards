package unical.enterpriceapplication.onlycards.application.utility.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.email")
public class EmailProperties {
    private String email;
}
