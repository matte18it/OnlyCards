package unical.enterpriceapplication.onlycards.application.dto.annotations.trustedurl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TrustedUrlValidator implements ConstraintValidator<TrustedUrl, String>{

    @Value("classpath:/trustedUrl.csv")
    private Resource urlWhitelist;
    @Value("${google.firebase.bucket}")
    private String bucketName;

    private List<String> trustedUrls = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Load URLs from CSV into trustedUrls list
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlWhitelist.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.hasText(line)) {
                    // Replace BUCKET_NAME with the actual bucket name if it exists
                    if (line.contains("BUCKET_NAME") && StringUtils.hasText(bucketName)) {
                        line = line.replace("BUCKET_NAME", bucketName.trim());
                    }
                    trustedUrls.add(line.trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load trusted URLs from CSV", e);
        }
    }
    
    @Override
    public boolean isValid(String url, ConstraintValidatorContext arg1) {
        if (!StringUtils.hasText(url)) {
            return true; // Null or empty URL is  valid
        }

        // Check if the URL starts with any of the trusted URLs
        return trustedUrls.stream().anyMatch(url::startsWith);
    }
    

}
