package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.ProfileChangeRequest;
import ZgazeniSendvic.Server_Back_ISS.repository.ProfileChangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChangeRequestServiceImpl
        implements ChangeRequestService {

    @Autowired
    ProfileChangeRequestRepository repository;

    @Override
    public ProfileChangeRequest save(ProfileChangeRequest request) {
        return repository.save(request);
    }

    @Override
    public Optional<ProfileChangeRequest> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(ProfileChangeRequest request) {
        repository.delete(request);
    }
}

