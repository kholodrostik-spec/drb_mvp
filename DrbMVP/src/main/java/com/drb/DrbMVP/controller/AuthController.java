package com.drb.DrbMVP.controller;


import com.drb.DrbMVP.dto.user.LoginDto;
import com.drb.DrbMVP.dto.user.RegisterDto;
import com.drb.DrbMVP.dto.user.TokenResponseDto;
import com.drb.DrbMVP.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth API", description = "Registration and login")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public TokenResponseDto register(@RequestBody RegisterDto dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public TokenResponseDto login(@RequestBody LoginDto dto) {
        return userService.login(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user",
            description = "Only for admin or this user"
    )
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
