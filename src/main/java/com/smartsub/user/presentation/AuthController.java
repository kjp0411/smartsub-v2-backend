package com.smartsub.user.presentation;

import com.smartsub.user.application.AuthService;
import com.smartsub.user.application.dto.AuthResult;
import com.smartsub.user.presentation.request.SignInRequest;
import com.smartsub.user.presentation.request.SignUpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(
        @Valid @RequestBody SignUpRequest request
    ) {
        authService.signUp(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResult> signIn(
        @Valid @RequestBody SignInRequest request
    ) {
        AuthResult result = authService.signIn(request.toCommand());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signOut(
        @RequestHeader ("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        authService.signOut(token);
        return ResponseEntity.ok().build();
    }
}
