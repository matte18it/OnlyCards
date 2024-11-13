package unical.enterpriceapplication.onlycards.application.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import unical.enterpriceapplication.onlycards.application.utility.captcha.CaptchaProperties;
import unical.enterpriceapplication.onlycards.application.utility.captcha.CaptchaResponse;

@Service
@RequiredArgsConstructor
public class CaptchaService {
    private final CaptchaProperties captchaProperties;
    private final RestTemplate restTemplate;

    public CaptchaResponse verifyCaptcha(String token) {
        String url = captchaProperties.getUrl();
        String secret = captchaProperties.getSecret();

        String requestUrl = String.format("%s?secret=%s&response=%s", url, secret, token);
        return restTemplate.postForObject(requestUrl, null, CaptchaResponse.class);
    }
}
