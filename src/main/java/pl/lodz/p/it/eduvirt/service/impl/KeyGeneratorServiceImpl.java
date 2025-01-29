package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.exceptions.access_key.KeyGenerationException;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.KeyGeneratorService;
import pl.lodz.p.it.eduvirt.util.key.KeyGenerationConstants;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class KeyGeneratorServiceImpl implements KeyGeneratorService {
    private final TeamAccessKeyRepository teamKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateUniqueTeamKey(Course course) {
        for (int attempt = 0; attempt < KeyGenerationConstants.MAX_ATTEMPTS; attempt++) {
            String key = generateKey(course);
            if (!teamKeyRepository.existsByKeyValue(key)) {
                return key;
            }
        }
        throw new KeyGenerationException();
    }

    private String generateKey(Course course) {
        StringBuilder key = new StringBuilder();
        String prefix = course.getName().substring(0, Math.min(2, course.getName().length())).toUpperCase();
        key.append(prefix);
        key.append("-");
        int randomSequenceLength = KeyGenerationConstants.KEY_LENGTH - prefix.length() - 1;
        for (int i = 0; i < randomSequenceLength; i++) {
            key.append(KeyGenerationConstants.ALLOWED_CHARS.charAt(
                    secureRandom.nextInt(KeyGenerationConstants.ALLOWED_CHARS.length())
            ));
        }
        return key.toString();
    }
}