package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByFileName(String fileName);

    List<Image> findAllByOwner(Account owner);

    List<Image> findAllByOwnerId(Long ownerId);

    boolean existsByFileName(String fileName);

    void deleteAllByOwner(Account owner);


}
