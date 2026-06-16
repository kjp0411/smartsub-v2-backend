package com.smartsub.user.presentation.request;

import com.smartsub.user.application.dto.SignUpCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    String password,

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 최대 50자까지 입력할 수 있습니다.")
    String name
) {
    public SignUpCommand toCommand() {
        return new SignUpCommand(email, password, name);
    }
}
