package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Admin;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Basic operations already exist, such as find all, or pagination, pretty sure

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT a FROM Account a WHERE TYPE(a) = Driver AND a.active = true")
    List<Driver> findAllActiveDrivers();

    List<Account> findByEmailIn(Collection<String> emails);

    @Query("SELECT a FROM Admin a")
    List<Admin> findAllAdmins();

}
