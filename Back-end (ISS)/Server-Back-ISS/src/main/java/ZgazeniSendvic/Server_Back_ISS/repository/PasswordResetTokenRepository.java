package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {


    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteAllByAccount(Account account);


}
