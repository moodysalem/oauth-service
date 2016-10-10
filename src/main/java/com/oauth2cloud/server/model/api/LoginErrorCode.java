package com.oauth2cloud.server.model.api;

/**
 * Represents some error states that can bring us back to the authorize page from the login page
 */
public enum LoginErrorCode {
    login_code_used("Your log in code has already been used."),
    login_code_expired("Your log in code has expired."),
    internal_error("An internal error has occurred. Please try again."),
    permission_denied("You must accept the permissions to log in.");

    private final String message;

    LoginErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
