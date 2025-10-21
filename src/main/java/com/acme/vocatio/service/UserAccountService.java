package com.acme.vocatio.service;

import com.acme.vocatio.dto.account.DeleteAccountRequest;
import com.acme.vocatio.dto.account.DeleteAccountResponse;
import com.acme.vocatio.exception.InvalidAccountDeletionConfirmationException;
import com.acme.vocatio.exception.InvalidCurrentPasswordException;
import com.acme.vocatio.exception.UserNotFoundException;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.ProfileRepository;
import com.acme.vocatio.repository.SavedResourceRepository;
import com.acme.vocatio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private static final String EXPECTED_CONFIRMATION = "ELIMINAR";

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SavedResourceRepository savedResourceRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DeleteAccountResponse deleteAccount(Long userId, DeleteAccountRequest request) {
        validateConfirmation(request);

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException("La contraseña actual no es correcta");
        }

        refreshTokenService.deleteAllForUser(user);
        savedResourceRepository.deleteAllByUserId(userId);
        profileRepository.deleteById(userId);

        userRepository.delete(user);

        String message = "Tu cuenta y datos se eliminaron de forma irreversible. Todas tus sesiones fueron cerradas.";
        return new DeleteAccountResponse(message, false);
    }

    private void validateConfirmation(DeleteAccountRequest request) {
        String confirmation = request.confirmation();
        String normalized = confirmation == null ? "" : confirmation.trim();
        if (!EXPECTED_CONFIRMATION.equalsIgnoreCase(normalized)) {
            throw new InvalidAccountDeletionConfirmationException("Debes escribir 'ELIMINAR' para confirmar");
        }
    }
}
