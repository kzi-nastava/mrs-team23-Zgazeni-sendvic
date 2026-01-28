package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.AccountConfirmationToken;
import ZgazeniSendvic.Server_Back_ISS.model.PasswordResetToken;
import ZgazeniSendvic.Server_Back_ISS.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PasswordResetTokenRepositoryService {

    @Autowired
    private PasswordResetTokenRepository allResetTokens;

    //Using a deprecated one because of determinism, to avoid making another util
    //ISNT DETERMNISTIC BASED ON VALUE ALONE AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    private final MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("SHA-256");

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final HexFormat hexFormat = HexFormat.of();
    private static final long RESET_EXPIRATION_SECONDS = 10 * 60;
    private static final long CONFIRMATION_EXPIRATION_SECONDS = 24 * 60 * 60;


    private Optional<PasswordResetToken> findToken(String rawToken) {
        String hashedToken = rawToken; // encoder.encode(rawToken)
        return allResetTokens.findByTokenHash(hashedToken);
    }

    @Transactional
    public String createResetToken(Account account){

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = hexFormat.formatHex(randomBytes);

        String hashedToken = rawToken;//encoder.encode(rawToken);

        PasswordResetToken token = new PasswordResetToken();
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(RESET_EXPIRATION_SECONDS));
        token.setUsed(false);
        token.setAccount(account);
        token.setTokenHash(hashedToken);
        //allResetTokens.deleteAllByAccount(account); //If i only wanted one active
        allResetTokens.save(token);
        allResetTokens.flush();
        return rawToken;

    }

    public String createConfirmationToken(Account account){
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = hexFormat.formatHex(randomBytes);

        String hashedToken = rawToken;//encoder.encode(rawToken);

        AccountConfirmationToken token = new AccountConfirmationToken();
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(CONFIRMATION_EXPIRATION_SECONDS));
        token.setUsed(false);
        token.setAccount(account);
        token.setTokenHash(hashedToken);
        //allResetTokens.deleteAllByAccount(account); //If i only wanted one active
        allResetTokens.save(token);
        allResetTokens.flush();
        return rawToken;
    }

    public Optional<Account> validateResetToken(String rawToken){

        //what if more than one token?
        //also should it return the Token, or the account Associated with it?

        Optional<Account> foundAccount = Optional.empty();
        System.out.println("hashedToken: " + rawToken);
        Optional<PasswordResetToken> tokenFound = findToken(rawToken);
        if(tokenFound.isEmpty()){
            System.out.println("NO TOKEN FOUND");
            return foundAccount;

        }

        PasswordResetToken token = tokenFound.get();
        if(token.isValid()){
            foundAccount = Optional.ofNullable(token.getAccount());
            return foundAccount;
        }
        //isn't valid but exists
        System.out.println("Token isn't valid but exists");
        return foundAccount;


    }

    public void markAsUsed(String rawToken){
        Optional<PasswordResetToken> tokenFound = findToken(rawToken);
        if(tokenFound.isEmpty()){
            return;
        }
        PasswordResetToken token = tokenFound.get();
        token.setUsed(true);
        allResetTokens.save(token);
        allResetTokens.flush();

    }

    boolean isReset(String rawToken){
        Optional<PasswordResetToken> tokenFound = findToken(rawToken);
        return tokenFound.isPresent() && !(tokenFound.get() instanceof AccountConfirmationToken);

    }

    boolean isConfirmation(String rawToken){

        Optional<PasswordResetToken> tokenFound = findToken(rawToken);
        return tokenFound.isPresent() && tokenFound.get() instanceof AccountConfirmationToken;

    }


}
