package com.drb.DrbMVP.service;

import com.drb.DrbMVP.dto.user.LoginDto;
import com.drb.DrbMVP.dto.user.RegisterDto;
import com.drb.DrbMVP.dto.user.TokenResponseDto;
import com.drb.DrbMVP.exception.UnauthorizedDeletionException;
import com.drb.DrbMVP.repository.UserRepository;
import com.drb.DrbMVP.service.service_api.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private RegisterDto registerDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        registerDto = new RegisterDto();
        registerDto.setEmail("test@example.com");
        registerDto.setName("Test User");
        registerDto.setPassword("password123");

        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");
    }

    @Test
    void register_newUser_returnsToken() {
        Mockito.when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("hashedPwd");
        Mockito.when(userRepository.save(registerDto.getEmail(), registerDto.getName(), "hashedPwd"))
                .thenReturn(Map.of("id", 1L, "email", registerDto.getEmail(), "role", "ROLE_USER"));
        Mockito.when(jwtService.generateToken(registerDto.getEmail(), "ROLE_USER")).thenReturn("jwt-token");

        TokenResponseDto result = userService.register(registerDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("jwt-token", result.getAccessToken());
        Assertions.assertEquals(registerDto.getEmail(), result.getEmail());
        Assertions.assertEquals("ROLE_USER", result.getRole());
        Mockito.verify(emailService).sendRegistrationEmail(registerDto.getEmail(), registerDto.getName());
    }

    @Test
    void register_existingEmail_throwsIllegalArgumentException() {
        Mockito.when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(registerDto));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        Mockito.when(userRepository.findByEmail(loginDto.getEmail()))
                .thenReturn(Optional.of(Map.of(
                        "id", 1L,
                        "email", loginDto.getEmail(),
                        "password", "hashedPwd",
                        "role", "ROLE_USER"
                )));
        Mockito.when(passwordEncoder.matches(loginDto.getPassword(), "hashedPwd")).thenReturn(true);
        Mockito.when(jwtService.generateToken(loginDto.getEmail(), "ROLE_USER")).thenReturn("jwt-token");

        TokenResponseDto result = userService.login(loginDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("jwt-token", result.getAccessToken());
        Assertions.assertEquals("ROLE_USER", result.getRole());
    }

    @Test
    void login_wrongPassword_throwsIllegalArgumentException() {
        Mockito.when(userRepository.findByEmail(loginDto.getEmail()))
                .thenReturn(Optional.of(Map.of(
                        "id", 1L,
                        "email", loginDto.getEmail(),
                        "password", "hashedPwd",
                        "role", "ROLE_USER"
                )));
        Mockito.when(passwordEncoder.matches(loginDto.getPassword(), "hashedPwd")).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.login(loginDto));
    }

    @Test
    void login_emailNotFound_throwsIllegalArgumentException() {
        Mockito.when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.login(loginDto));
    }

    @Test
    void deleteUser_nonExistingUser_throwsIllegalArgumentException() {
        Mockito.when(userRepository.existsById(99L)).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(99L));
    }

    @Test
    void deleteUser_adminDeletesAnyUser_succeeds() {
        Mockito.when(userRepository.existsById(5L)).thenReturn(true);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("admin@example.com");
        Mockito.when(auth.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContext ctx = Mockito.mock(SecurityContext.class);
        Mockito.when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        userService.deleteUser(5L);

        Mockito.verify(userRepository).deleteById(5L);
    }

    @Test
    void deleteUser_userDeletesOwnAccount_succeeds() {
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(userRepository.findIdByEmail("test@example.com")).thenReturn(1L);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("test@example.com");
        Mockito.when(auth.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext ctx = Mockito.mock(SecurityContext.class);
        Mockito.when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        userService.deleteUser(1L);

        Mockito.verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_userDeletesOtherAccount_throwsUnauthorizedDeletionException() {
        Mockito.when(userRepository.existsById(2L)).thenReturn(true);
        Mockito.when(userRepository.findIdByEmail("test@example.com")).thenReturn(1L);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("test@example.com");
        Mockito.when(auth.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext ctx = Mockito.mock(SecurityContext.class);
        Mockito.when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        Assertions.assertThrows(UnauthorizedDeletionException.class,
                () -> userService.deleteUser(2L));
    }
}
