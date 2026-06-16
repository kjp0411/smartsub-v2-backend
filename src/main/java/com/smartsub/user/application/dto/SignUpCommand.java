package com.smartsub.user.application.dto;

public record SignUpCommand(
    String email,
    String password,
    String name
) {
}
