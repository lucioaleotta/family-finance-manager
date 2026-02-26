package com.lucio.financeapp.users.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.password-reset")
public class PasswordResetProperties {

    private long tokenMinutes = 30;
    private String resetUrl = "http://localhost:3000/reset-password";
    private String fromEmail = "no-reply@family-finance.local";

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
