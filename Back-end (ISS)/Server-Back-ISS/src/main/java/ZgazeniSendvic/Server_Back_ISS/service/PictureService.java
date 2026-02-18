package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Picture;
import ZgazeniSendvic.Server_Back_ISS.repository.PictureRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PictureService {

    private final PictureRepository pictureRepository;

    private final Path uploadRoot = Paths.get("uploads");

    // Path/URL to default image
    private static final String DEFAULT_PICTURE_URL = "/images/default-profile.png";
    private static final String DEFAULT_CONTENT_TYPE = "image/png";
    private static final String DEFAULT_PICTURE_NAME = "Default-profile.png";

    public PictureService(PictureRepository pictureRepository) throws IOException {
        this.pictureRepository = pictureRepository;
        Files.createDirectories(uploadRoot);
    }

    public Picture uploadProfilePicture(Account account, MultipartFile file) {
        validatePicture(file);

        // Enforce single profile image per account
        pictureRepository.findByOwner(account)
                .ifPresent(existing -> {
                    deleteFile(existing.getFileName());
                    pictureRepository.delete(existing);
                    pictureRepository.flush();
                });

        String extension = getExtension(file.getOriginalFilename());
        String fileName = buildFileName(account.getId(), extension);

        Path target = uploadRoot.resolve(fileName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }

        String publicUrl = "/images/" + fileName;

        Picture picture = new Picture();
        picture.setFileName(fileName);
        picture.setUrl(publicUrl);
        picture.setContentType(file.getContentType());
        picture.setSize(file.getSize());
        picture.setOwner(account);

        return pictureRepository.save(picture);
    }

    private void deleteFile(String fileName) {
        try {
            Files.deleteIfExists(uploadRoot.resolve(fileName));
        } catch (IOException e) {
            // Log only — don't fail business logic
            System.err.println("Failed to delete image file: " + fileName);
        }
    }

    private void validatePicture(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Max file size is 5MB");
        }
    }

    private String buildFileName(Long accountId, String extension) {
        return "account-" + accountId + "-" + UUID.randomUUID() + "." + extension;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    // This is for login, front end first logins, if successful THEN based ON TOKEN, will request an image
    // This is how I choose what image will be returned
    private String returnRelatedURL(Account account) {
        Optional<Picture> picture =  pictureRepository.findByOwner(account);
        if (picture.isPresent()) {
            return picture.get().getUrl();
        }

        //else just use the default
        return DEFAULT_PICTURE_URL;

    }


    //--------------------------Sending a picture------------------------------


    //  either returns picture associated with account, or a new picture if it doesn't exist

    //Ah but all this can be significantly simplified can it not
    //all you do is either return return the PicturePath to its picture or the default picture, of which you have the
    //name
    public Picture getProfilePicture(Account account) {
        return pictureRepository.findByOwner(account)
                .orElseGet(this::buildDefaultPicture);
    }

    private Picture buildDefaultPicture() {
        Picture picture = new Picture();
        picture.setId(0L); // synthetic ID
        picture.setUrl(DEFAULT_PICTURE_URL);
        picture.setContentType(DEFAULT_CONTENT_TYPE);
        picture.setSize(0L);
        picture.setCreatedAt(Instant.EPOCH);
        picture.setFileName(DEFAULT_PICTURE_NAME);
        return picture;
    }

    public Path getPicturePath(Picture picture) {
        return uploadRoot.resolve(picture.getFileName());
    }

    public Path getPicturePath(Account account){
        Optional<Picture> picture =  pictureRepository.findByOwner(account);
        if (picture.isPresent()) {
            return uploadRoot.resolve(picture.get().getFileName());
        }
        //else its default
        return uploadRoot.resolve(DEFAULT_PICTURE_NAME);
    }


}

