package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {

    Optional<Picture> findByFileName(String fileName);

    Optional<Picture> findByOwner(Account owner);

    Optional<Picture> findByOwnerId(Long ownerId);

    boolean existsByFileName(String fileName);

    void deleteByOwner(Account owner);


}
