package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.ProfileChangeRequest;

import java.util.Optional;

public interface ChangeRequestService {

    ProfileChangeRequest save(ProfileChangeRequest request);

    Optional<ProfileChangeRequest> findById(Long id);

    void delete(ProfileChangeRequest request);
}

