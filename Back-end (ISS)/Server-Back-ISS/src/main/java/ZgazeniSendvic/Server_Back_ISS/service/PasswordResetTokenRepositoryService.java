package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.AccountConfirmationToken;
import ZgazeniSendvic.Server_Back_ISS.model.PasswordResetToken;
import ZgazeniSendvic.Server_Back_ISS.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Issues and validates the short numeric codes used for password reset and account
 * activation.
 *
 * Codes are emailed to the user (no links) so the exact same flow works for the web
 * and the Android app. Because a 6-digit code is short, it is always validated
 * together with the owning account (looked up by email in the service layer) instead
 * of globally - two different users may legitimately hold the same code at once.
 */
@Service
public class PasswordResetTokenRepositoryService {

    @Autowired
    private PasswordResetTokenRepository allResetTokens;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final long RESET_EXPIRATION_SECONDS = 10 * 60;
    private static final long CONFIRMATION_EXPIRATION_SECONDS = 24 * 60 * 60;

    private static String generateCode() {
        // 6-digit, zero-padded so codes like "004217" keep their length.
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    /**
     * Issues a fresh password-reset code, invalidating any earlier unused reset codes
     * for the account so only the latest one works.
     */
    @Transactional
    public String createResetToken(Account account) {
        invalidateActive(account, false);

        String code = generateCode();
        PasswordResetToken token = new PasswordResetToken();
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(RESET_EXPIRATION_SECONDS));
        token.setUsed(false);
        token.setAccount(account);
        token.setTokenHash(code);
        allResetTokens.save(token);
        allResetTokens.flush();
        return code;
    }

    /**
     * Issues a fresh account-activation code, invalidating earlier unused ones.
     */
    @Transactional
    public String createConfirmationToken(Account account) {
        invalidateActive(account, true);

        String code = generateCode();
        AccountConfirmationToken token = new AccountConfirmationToken();
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(CONFIRMATION_EXPIRATION_SECONDS));
        token.setUsed(false);
        token.setAccount(account);
        token.setTokenHash(code);
        allResetTokens.save(token);
        allResetTokens.flush();
        return code;
    }

    /**
     * Looks up a valid, unused code of the requested kind for the given account.
     *
     * @param confirmation true to match an activation code, false to match a reset code
     * @return the matching token, or empty if no valid code matches
     */
    public Optional<PasswordResetToken> findValidCode(Account account, String code, boolean confirmation) {
        if (account == null || code == null) {
            return Optional.empty();
        }
        List<PasswordResetToken> tokens = allResetTokens.findByAccount(account);
        for (PasswordResetToken token : tokens) {
            boolean isConfirmation = token instanceof AccountConfirmationToken;
            if (isConfirmation != confirmation) {
                continue;
            }
            if (token.isValid() && token.getTokenHash().equals(code.trim())) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

    @Transactional
    public void markAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        allResetTokens.save(token);
        allResetTokens.flush();
    }

    // ---------------------------------------------------------------------------
    // Legacy global-token lookups, kept for the driver activation flow
    // (DriverServiceImpl) which validates a UUID token without an account context.
    // The reset/confirmation code flow above does NOT use these.
    // ---------------------------------------------------------------------------

    public Optional<Account> validateResetToken(String rawToken) {
        return allResetTokens.findByTokenHash(rawToken)
                .filter(PasswordResetToken::isValid)
                .map(PasswordResetToken::getAccount);
    }

    @Transactional
    public void markAsUsed(String rawToken) {
        allResetTokens.findByTokenHash(rawToken).ifPresent(this::markAsUsed);
    }

    /** Marks every still-valid code of the given kind for the account as used. */
    private void invalidateActive(Account account, boolean confirmation) {
        for (PasswordResetToken token : allResetTokens.findByAccount(account)) {
            boolean isConfirmation = token instanceof AccountConfirmationToken;
            if (isConfirmation == confirmation && !token.isUsed()) {
                token.setUsed(true);
                allResetTokens.save(token);
            }
        }
    }
}
