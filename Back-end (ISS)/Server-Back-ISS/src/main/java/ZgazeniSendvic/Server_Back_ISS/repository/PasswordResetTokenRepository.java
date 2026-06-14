package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {


    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    // Codes are short and scoped per account, so we look them up by the owning
    // account (the same 6-digit code may legitimately exist for two users).
    List<PasswordResetToken> findByAccount(Account account);

    void deleteAllByAccount(Account account);


}
