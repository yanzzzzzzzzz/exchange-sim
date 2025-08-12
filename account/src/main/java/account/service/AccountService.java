package account.service;

import account.exception.ConflictException;
import account.exception.ValidationException;
import account.model.User;
import account.repository.UserRepository;
import dto.RegisterRequest;
import dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;

    public RegisterResponse register(RegisterRequest request) {
        validateRegisterRequest(request);
        checkEmailConflict(request.email());

        User user = createUser(request);
        User savedUser = userRepository.save(user);

        return new RegisterResponse("register success", savedUser);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        Map<String, List<String>> errors = new HashMap<>();

        if (request.email() == null || request.email().trim().isEmpty()) {
            errors.put("email", Arrays.asList("must be a valid email"));
        } else if (!isValidEmail(request.email())) {
            errors.put("email", Arrays.asList("must be a valid email"));
        }

        if (request.username() == null || request.username().trim().isEmpty()) {
            errors.put("username", Arrays.asList("must be a valid username"));
        }

        if (request.password() == null || request.password().trim().isEmpty()) {
            errors.put("password", Arrays.asList("must be a valid password"));
        }
        if (request.password().length() < 7) {
            errors.put("password", Arrays.asList("password length must more then 7"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("validation_error", "invalid fields", errors);
        }
    }

    private void checkEmailConflict(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("conflict", "email already registered");
        }
    }

    private User createUser(RegisterRequest request) {
        String id = generateUserId();
        Instant now = Instant.now(); // UTC time

        return User.builder()
                .id(id)
                .username(request.username().trim())
                .email(request.email().trim())
                .password(request.password())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private String generateUserId() {
        return "usr_" + UUID.randomUUID().toString();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
