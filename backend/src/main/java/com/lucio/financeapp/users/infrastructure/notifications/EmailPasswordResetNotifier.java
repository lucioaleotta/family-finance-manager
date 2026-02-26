package com.lucio.financeapp.users.infrastructure.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.lucio.financeapp.users.application.PasswordResetNotifier;
import com.lucio.financeapp.users.infrastructure.config.PasswordResetProperties;

@Component
public class EmailPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailPasswordResetNotifier.class);

    private final JavaMailSender mailSender;
    private final PasswordResetProperties properties;

    public EmailPasswordResetNotifier(ObjectProvider<JavaMailSender> mailSenderProvider,
            PasswordResetProperties properties) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.properties = properties;
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        if (mailSender == null) {
            LOGGER.info("Password reset link for {}: {}", email, resetLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.getFromEmail());
            message.setTo(email);
            message.setSubject("Reimposta la tua password");
            message.setText("Abbiamo ricevuto una richiesta di reset password.\n\n"
                    + "Usa questo link per impostare una nuova password:\n"
                    + resetLink
                    + "\n\n"
                    + "Se non hai richiesto il reset, ignora questa email.");
            mailSender.send(message);
        } catch (Exception ex) {
            LOGGER.warn("Unable to send password reset email to {}. Link: {}", email, resetLink, ex);
        }
    }
}
