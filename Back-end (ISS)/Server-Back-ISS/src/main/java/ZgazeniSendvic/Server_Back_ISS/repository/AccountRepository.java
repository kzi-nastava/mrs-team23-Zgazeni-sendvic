package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Basic operations already exist, such as find all, or pagination, pretty sure

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);


}
