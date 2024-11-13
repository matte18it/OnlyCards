package unical.enterpriceapplication.onlycards.application.utility.captcha;

import lombok.Data;

@Data
public class CaptchaResponse {
    private boolean success;
    private double score;

    public CaptchaResponse(boolean success, double score) {
        this.success = success;
        this.score = score;
    }
}
