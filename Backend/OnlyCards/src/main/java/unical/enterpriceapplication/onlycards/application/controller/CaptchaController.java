package unical.enterpriceapplication.onlycards.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unical.enterpriceapplication.onlycards.application.utility.captcha.CaptchaResponse;
import org.springframework.web.bind.annotation.*;
import unical.enterpriceapplication.onlycards.application.core.service.CaptchaService;

@RestController
@RequestMapping(path = "/v1/auth")
@RequiredArgsConstructor
public class CaptchaController {
    private static final Logger log = LoggerFactory.getLogger(CaptchaController.class);
    private final CaptchaService captchaService;

    @Operation(summary = "Generate a captcha token", description = "This endpoint verifies the provided captcha token to ensure it is valid and unaltered. The token is passed as a path variable, and the system checks it against the server-side records to confirm its legitimacy. If the verification succeeds, a success response is returned; otherwise, an appropriate error message is sent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Captcha token generated"),
            @ApiResponse(responseCode = "400", description = "Captcha token not generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping("/captcha/{token}")
    public CaptchaResponse verifyCaptcha(@PathVariable String token) {
        log.info("Verifying captcha token: {}", token);

        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("Token cannot be null");

        return captchaService.verifyCaptcha(token);
    }
}
