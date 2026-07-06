package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RideRequestRepositoryTest {

    @Autowired RideRequestRepository repo;

    @Autowired AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    void findByStatus_returnsMatching() {
        RideRequest r1 = new RideRequest();
        r1.setStatus(RequestStatus.PENDING);

        RideRequest r2 = new RideRequest();
        r2.setStatus(RequestStatus.ACCEPTED);

        repo.save(r1);
        repo.save(r2);

        List<RideRequest> pending = repo.findByStatus(RequestStatus.PENDING);
        assertEquals(1, pending.size());
        assertEquals(RequestStatus.PENDING, pending.get(0).getStatus());
    }

    @Test
    void findTop10ByCreatorEmailOrderByIdDesc_returnsOnlyUserRides() {
        User u1 = createValidUser("u1@test.com");
        accountRepository.save(u1);

        User u2 = createValidUser("u2@test.com");
        accountRepository.save(u2);

        RideRequest r1 = new RideRequest();
        r1.setCreator(u1);

        RideRequest r2 = new RideRequest();
        r2.setCreator(u2);

        RideRequest r3 = new RideRequest();
        r3.setCreator(u1);

        repo.save(r1);
        repo.save(r2);
        repo.save(r3);

        List<RideRequest> result =
                repo.findTop10ByCreator_EmailOrderByIdDesc("u1@test.com");

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(r -> r.getCreator().getEmail().equals("u1@test.com"))
        );

        List<Long> ids = result.stream().map(RideRequest::getId).toList();
        List<Long> sorted = new ArrayList<>(ids);
        sorted.sort(Comparator.reverseOrder());

        assertEquals(sorted, ids);
    }

    private User createValidUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setName("Test");
        u.setLastName("User");
        u.setPassword("password123");
        u.setPhoneNumber("0001010101");
        u.setAddress("test");
        return u;
    }
}

