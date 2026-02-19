package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.ProfileChangeRequest;
import ZgazeniSendvic.Server_Back_ISS.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileChangeRequestRepository
        extends JpaRepository<ProfileChangeRequest, Long> {

    List<ProfileChangeRequest> findByStatus(RequestStatus status);
}


