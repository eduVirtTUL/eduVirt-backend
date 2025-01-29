package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.lodz.p.it.eduvirt.exceptions.access_key.KeyGenerationException;
import pl.lodz.p.it.eduvirt.service.impl.KeyGeneratorServiceImpl;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.util.key.KeyGenerationConstants;

@ExtendWith(MockitoExtension.class)
public class KeyGeneratorServiceTest {

    @Mock
    private TeamAccessKeyRepository teamKeyRepository;

    @InjectMocks
    private KeyGeneratorServiceImpl keyGeneratorService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .name("TestCourse")
                .courseType(CourseType.TEAM_BASED)
                .build();
        setEntityId(course, UUID.randomUUID());
    }

    @Test
    void Given_NoExistingKeys_When_GenerateKey_Then_ReturnUniqueKey() {
        when(teamKeyRepository.existsByKeyValue(anyString())).thenReturn(false);

        String key = keyGeneratorService.generateUniqueTeamKey(course);

        assertNotNull(key);
        assertTrue(key.startsWith("TE-"));
        assertTrue(key.matches("^[A-Z]{2}-[A-Za-z1-9]{" + (KeyGenerationConstants.KEY_LENGTH - 3) + "}$"));
        assertFalse(key.contains("O"));
        assertFalse(key.contains("0"));
        assertFalse(key.contains("l"));
        verify(teamKeyRepository).existsByKeyValue(key);
    }

    @Test
    void Given_ExistingKeys_When_GenerateKey_Then_ReturnUniqueKey() {
        when(teamKeyRepository.existsByKeyValue(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        String key = keyGeneratorService.generateUniqueTeamKey(course);

        assertNotNull(key);
        assertTrue(key.startsWith("TE-"));
        verify(teamKeyRepository, times(3)).existsByKeyValue(anyString());
    }

    @Test
    void Given_MaxAttemptsReached_When_GenerateKey_Then_ThrowException() {
        when(teamKeyRepository.existsByKeyValue(anyString())).thenReturn(true);

        assertThrows(KeyGenerationException.class,
                () -> keyGeneratorService.generateUniqueTeamKey(course));
        verify(teamKeyRepository, times(KeyGenerationConstants.MAX_ATTEMPTS))
                .existsByKeyValue(anyString());
    }

    @Test
    void Given_ShortCourseName_When_GenerateKey_Then_UseAvailableChars() {
        Course shortNameCourse = Course.builder()
                .name("A")
                .courseType(CourseType.TEAM_BASED)
                .build();
        when(teamKeyRepository.existsByKeyValue(anyString())).thenReturn(false);

        String key = keyGeneratorService.generateUniqueTeamKey(shortNameCourse);

        assertNotNull(key);
        assertTrue(key.startsWith("A-"));
        assertEquals(KeyGenerationConstants.KEY_LENGTH, key.length());
        assertTrue(key.matches("^[A-Z]-[A-Za-z1-9]{" + (KeyGenerationConstants.KEY_LENGTH - 2) + "}$"));
    }

    @Test
    void Given_MultipleGenerations_When_GenerateKey_Then_UseValidCharacters() {
        when(teamKeyRepository.existsByKeyValue(anyString())).thenReturn(false);

        Set<Character> usedChars = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String key = keyGeneratorService.generateUniqueTeamKey(course);
            for (char c : key.replaceAll("-", "").toCharArray()) {
                usedChars.add(c);
            }
        }

        usedChars.forEach(c ->
                assertTrue(KeyGenerationConstants.ALLOWED_CHARS.indexOf(c) >= 0
                        || Character.isUpperCase(c)));
    }

    @Test
    void Given_ValidCourse_When_GenerateKey_Then_VerifyKeyFormat() {
        when(teamKeyRepository.existsByKeyValue(anyString())).thenReturn(false);

        String key = keyGeneratorService.generateUniqueTeamKey(course);

        String[] parts = key.split("-");
        assertEquals(2, parts.length);
        assertEquals(2, parts[0].length());
        assertEquals(KeyGenerationConstants.KEY_LENGTH - 3, parts[1].length());
        assertTrue(parts[0].matches("^[A-Z]{2}$"));
        assertTrue(parts[1].matches("^[A-Za-z1-9]+$"));
    }

    @Test
    void Given_MultipleGenerations_When_GenerateKey_Then_VerifyCharacterSet() {
        when(teamKeyRepository.existsByKeyValue(anyString())).thenReturn(false);
        Set<Character> allowedChars = KeyGenerationConstants.ALLOWED_CHARS
                .chars()
                .mapToObj(ch -> (char) ch)
                .collect(Collectors.toSet());

        Set<Character> usedChars = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String key = keyGeneratorService.generateUniqueTeamKey(course);
            key.substring(3)
                    .chars()
                    .mapToObj(ch -> (char) ch)
                    .forEach(usedChars::add);
        }

        assertTrue(allowedChars.containsAll(usedChars));
        assertFalse(usedChars.contains('O'));
        assertFalse(usedChars.contains('0'));
        assertFalse(usedChars.contains('l'));
    }

    @Test
    void Given_GeneratedKey_When_CheckLength_Then_MatchesConstant() {
        when(teamKeyRepository.existsByKeyValue(anyString())).thenReturn(false);

        String key = keyGeneratorService.generateUniqueTeamKey(course);

        assertEquals(KeyGenerationConstants.KEY_LENGTH, key.length());
        assertEquals(KeyGenerationConstants.KEY_LENGTH - 3, key.substring(3).length());
        assertTrue(key.matches("^[A-Z]{2}-[A-Za-z1-9]{" + (KeyGenerationConstants.KEY_LENGTH - 3) + "}$"));
    }

    /* Helper Methods */

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }
}