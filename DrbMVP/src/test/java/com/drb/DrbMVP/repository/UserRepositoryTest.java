package com.drb.DrbMVP.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserRepository userRepository;

    @Test
    void save_returnsUserMap() {
        Map<String, Object> expected = Map.of(
                "id", 1L,
                "email", "test@example.com",
                "name", "Test User",
                "role", "ROLE_USER"
        );

        Mockito.when(jdbcTemplate.queryForMap(
                Mockito.anyString(),
                Mockito.eq("test@example.com"),
                Mockito.eq("Test User"),
                Mockito.eq("hashedPwd")
        )).thenReturn(expected);

        Map<String, Object> result = userRepository.save("test@example.com", "Test User", "hashedPwd");

        Assertions.assertEquals("test@example.com", result.get("email"));
        Assertions.assertEquals("ROLE_USER", result.get("role"));
    }

    @Test
    void findByEmail_existingUser_returnsOptional() {
        List<Map<String, Object>> rows = List.of(
                Map.of(
                        "id", 1L,
                        "email", "test@example.com",
                        "name", "Test User",
                        "role", "ROLE_USER",
                        "password", "hashedPwd"
                )
        );

        Mockito.when(jdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("test@example.com")))
                .thenReturn(rows);

        Optional<Map<String, Object>> result = userRepository.findByEmail("test@example.com");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("test@example.com", result.get().get("email"));
    }

    @Test
    void findByEmail_noUser_returnsEmpty() {
        Mockito.when(jdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("ghost@example.com")))
                .thenReturn(List.of());

        Optional<Map<String, Object>> result = userRepository.findByEmail("ghost@example.com");

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_returnsTrueWhenCountPositive() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class), Mockito.eq("test@example.com")))
                .thenReturn(1);

        boolean result = userRepository.existsByEmail("test@example.com");

        Assertions.assertTrue(result);
    }

    @Test
    void blockUser_returnsTrueWhenUpdated() {
        Mockito.when(jdbcTemplate.update(Mockito.anyString(), Mockito.eq(1L))).thenReturn(1);

        boolean result = userRepository.blockUser(1L);

        Assertions.assertTrue(result);
    }

    @Test
    void deleteById_callsUpdate() {
        userRepository.deleteById(1L);
        Mockito.verify(jdbcTemplate).update(Mockito.anyString(), Mockito.eq(1L));
    }

    @Test
    void existsById_returnsFalseWhenCountZero() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class), Mockito.eq(1L)))
                .thenReturn(0);

        boolean result = userRepository.existsById(1L);

        Assertions.assertFalse(result);
    }

    @Test
    void findIdByEmail_returnsId() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Long.class), Mockito.eq("test@example.com")))
                .thenReturn(5L);

        Long result = userRepository.findIdByEmail("test@example.com");

        Assertions.assertEquals(5L, result);
    }
}