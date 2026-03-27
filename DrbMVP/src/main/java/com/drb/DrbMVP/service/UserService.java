package com.drb.DrbMVP.service;

import com.drb.DrbMVP.constant.AppConstant;
import com.drb.DrbMVP.dto.user.LoginDto;
import com.drb.DrbMVP.dto.user.RegisterDto;
import com.drb.DrbMVP.dto.user.TokenResponseDto;
import com.drb.DrbMVP.exception.UnauthorizedDeletionException;
import com.drb.DrbMVP.repository.UserRepository;
import com.drb.DrbMVP.service.service_api.EmailService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public TokenResponseDto register(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        Map<String, Object> user = userRepository.save(
                dto.getEmail(), dto.getName(), hashedPassword
        );

        emailService.sendRegistrationEmail(dto.getEmail(), dto.getName());

        String token = jwtService.generateToken(dto.getEmail(), "ROLE_USER");
        return new TokenResponseDto(
                token,
                ((Number) user.get("id")).longValue(),
                dto.getEmail(),
                "ROLE_USER"
        );
    }

    public TokenResponseDto login(LoginDto dto) {
        Map<String, Object> user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), (String) user.get("password"))) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        String token = jwtService.generateToken(
                dto.getEmail(), (String) user.get("role")
        );
        return new TokenResponseDto(
                token,
                ((Number) user.get("id")).longValue(),
                dto.getEmail(),
                (String) user.get("role")
        );
    }

    public void deleteUser(Long targetId) {
        if (!userRepository.existsById(targetId)) {
            throw new IllegalArgumentException("User with id " + targetId + " does not exist");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstant.ROLE_ADMIN));

        if (!isAdmin) {
            Long currentUserId = userRepository.findIdByEmail(currentEmail);
            if (!currentUserId.equals(targetId)) {
                throw new UnauthorizedDeletionException(currentUserId, targetId);
            }
        }

        userRepository.deleteById(targetId);
    }
}
