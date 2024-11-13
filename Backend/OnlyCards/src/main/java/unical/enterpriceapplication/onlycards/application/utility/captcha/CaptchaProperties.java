package unical.enterpriceapplication.onlycards.application.utility.captcha;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.recaptcha")
public class CaptchaProperties {
    private String secret;
    private String url;
    private String threshold;
    private String siteKey;
    private String action;
}
