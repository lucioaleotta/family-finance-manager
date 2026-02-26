package com.lucio.financeapp.users.application;

public interface PasswordResetNotifier {

    void sendPasswordResetEmail(String email, String resetLink);
}
