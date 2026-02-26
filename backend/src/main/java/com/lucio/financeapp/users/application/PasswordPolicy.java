package com.lucio.financeapp.users.application;

public final class PasswordPolicy {

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    private PasswordPolicy() {
    }

    public static void validateOrThrow(String password) {
        if (password == null || !password.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("password is too weak");
        }
    }
}
