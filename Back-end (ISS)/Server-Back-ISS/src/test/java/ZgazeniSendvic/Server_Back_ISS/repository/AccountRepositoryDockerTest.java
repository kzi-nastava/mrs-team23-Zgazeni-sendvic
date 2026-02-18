package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Admin;
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


import java.util.List;
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

    @Test
    @DisplayName("Should return list of admins when admins exist")
    void testFindAllAdmins_AdminsExist() {
        // Create and save multiple admins
        Admin admin1 = new Admin();
        admin1.setEmail("admin1@test.com");
        admin1.setPassword(passwordEncoder.encode("adminpass123"));
        admin1.setName("Admin");
        admin1.setLastName("One");
        admin1.setAddress("100 Admin St, Test City");
        admin1.setPhoneNumber("1111111111");
        admin1.setConfirmed(true);
        accountRepository.save(admin1);

        Admin admin2 = new Admin();
        admin2.setEmail("admin2@test.com");
        admin2.setPassword(passwordEncoder.encode("adminpass456"));
        admin2.setName("Admin");
        admin2.setLastName("Two");
        admin2.setAddress("200 Admin Ave, Test City");
        admin2.setPhoneNumber("2222222222");
        admin2.setConfirmed(true);
        accountRepository.save(admin2);

        // Create a regular user (should not be included)
        User regularUser = new User();
        regularUser.setEmail("user@test.com");
        regularUser.setPassword(passwordEncoder.encode("userpass123"));
        regularUser.setName("Regular");
        regularUser.setLastName("User");
        regularUser.setAddress("300 User Blvd, Test City");
        regularUser.setPhoneNumber("3333333333");
        regularUser.setConfirmed(true);
        accountRepository.save(regularUser);

        List<Admin> admins = accountRepository.findAllAdmins();

        assertNotNull(admins);
        assertEquals(2, admins.size());
        assertTrue(admins.stream().anyMatch(a -> a.getEmail().equals("admin1@test.com")));
        assertTrue(admins.stream().anyMatch(a -> a.getEmail().equals("admin2@test.com")));
    }

    @Test
    @DisplayName("Should return empty list when no admins exist")
    void testFindAllAdmins_NoAdminsExist() {
        // Create only regular users
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setPassword(passwordEncoder.encode("userpass123"));
        user1.setName("User");
        user1.setLastName("One");
        user1.setAddress("400 User St, Test City");
        user1.setPhoneNumber("4444444444");
        user1.setConfirmed(true);
        accountRepository.save(user1);

        List<Admin> admins = accountRepository.findAllAdmins();

        assertNotNull(admins);
        assertTrue(admins.isEmpty());
        assertEquals(0, admins.size());
    }


}
