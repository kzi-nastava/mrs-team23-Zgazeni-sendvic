package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AccountRepositoryTest")
public class AccountRepositoryDockerTest {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountRepository accountRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");


    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }



    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void testExistsByEmail_EmailExists() {
        Account accountA = new User();
        accountA.setEmail("accountTEST@test.com");
        accountA.setPassword(passwordEncoder.encode("password123"));
        accountA.setName("John");
        accountA.setLastName("Doe");
        accountA.setAddress("123 Main St, Test City");
        accountA.setPhoneNumber("1234567890");
        accountA.setConfirmed(true);
        accountRepository.save(accountA);

        boolean exists = accountRepository.existsByEmail("accountTEST@test.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void testExistsByEmail_EmailNotExists() {
        boolean exists = accountRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should return Account when email exists")
    void testFindByEmail_EmailExists() {
        Account accountA = new User();
        accountA.setEmail("findtest@test.com");
        accountA.setPassword(passwordEncoder.encode("password123"));
        accountA.setName("Jane");
        accountA.setLastName("Smith");
        accountA.setAddress("456 Oak Ave, Test City");
        accountA.setPhoneNumber("0987654321");
        accountA.setConfirmed(true);
        accountRepository.save(accountA);

        Optional<Account> result = accountRepository.findByEmail("findtest@test.com");

        assertTrue(result.isPresent());
        assertEquals("findtest@test.com", result.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty Optional when email does not exist")
    void testFindByEmail_EmailNotExists() {
        Optional<Account> result = accountRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }


}
