package com.lucio.financeapp.users.infrastructure.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "security.password-reset")
public class PasswordResetProperties {

    @Min(1)
    private long tokenMinutes;

    @NotBlank
    private String resetUrl;

    @NotBlank
    @Email
    private String fromEmail;

    public long getTokenMinutes() {
        return tokenMinutes;
    }

    public void setTokenMinutes(long tokenMinutes) {
        this.tokenMinutes = tokenMinutes;
    }

    public String getResetUrl() {
        return resetUrl;
    }

    public void setResetUrl(String resetUrl) {
        this.resetUrl = resetUrl;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String buildResetLink(String token) {
        String separator = resetUrl.contains("?") ? "&" : "?";
        return resetUrl + separator + "token=" + token;
    }
}
