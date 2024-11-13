package unical.enterpriceapplication.onlycards.application.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.utility.firebase.FirebaseProperties;

import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FirebaseInitializer {
    private final FirebaseProperties firebaseProperties;

    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/onlycards.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(firebaseProperties.getBucket())
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
