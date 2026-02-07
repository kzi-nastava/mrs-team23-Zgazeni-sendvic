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
import java.util.UUID;

@Service
@Transactional
public class ImageService {

    private final PictureRepository pictureRepository;

    private final Path uploadRoot = Paths.get("uploads");

    public ImageService(PictureRepository pictureRepository) throws IOException {
        this.pictureRepository = pictureRepository;
        Files.createDirectories(uploadRoot);
    }

    public Picture uploadProfileImage(Account account, MultipartFile file) {
        validateImage(file);

        // Enforce single profile image per account
        pictureRepository.findByOwner(account)
                .ifPresent(existing -> {
                    deleteFile(existing.getFileName());
                    pictureRepository.delete(existing);
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

    private void validateImage(MultipartFile file) {
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
}

