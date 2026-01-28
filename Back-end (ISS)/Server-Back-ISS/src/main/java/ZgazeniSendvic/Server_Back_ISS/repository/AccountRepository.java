package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Basic operations already exist, such as find all, or pagination, pretty sure

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
    SELECT d FROM Driver d
    WHERE d.active = true
      AND d.busy = false
      AND d.workedMinutesLast24h < 480
""")
    List<Driver> findAvailableDrivers();

}
