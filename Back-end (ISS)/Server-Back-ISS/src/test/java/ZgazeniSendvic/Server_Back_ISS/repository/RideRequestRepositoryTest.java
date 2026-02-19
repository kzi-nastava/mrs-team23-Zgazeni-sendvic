package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RideRequestRepositoryTest {

    @Autowired RideRequestRepository repo;

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
}

