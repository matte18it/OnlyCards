package unical.enterpriceapplication.onlycards.application.core.service;

import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlugifyService {
    public String slugify(String input) {
        return Slugify.builder().build().slugify(input);
    }
}
