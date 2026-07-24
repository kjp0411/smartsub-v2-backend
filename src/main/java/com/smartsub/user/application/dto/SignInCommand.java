package com.smartsub.user.application.dto;

public record SignInCommand(
    String email,
    String password
) {
}
