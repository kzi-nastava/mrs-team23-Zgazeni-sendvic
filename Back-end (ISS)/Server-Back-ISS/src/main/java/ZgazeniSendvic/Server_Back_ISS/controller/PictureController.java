package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.PictureResponse;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Picture;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
import ZgazeniSendvic.Server_Back_ISS.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/pictures")
public class PictureController {

   @Autowired
   private AccountServiceImpl accountService;
   @Autowired
   private PictureService pictureService;

   //WHAT IF ACCOUNT NOT FOUND?
    // Will be handled later, once secure handling is actually implemented

    @PostMapping("/register/profile/{accountId}")
    public ResponseEntity<PictureResponse> uploadMyProfilePicture(
            @PathVariable Long accountId,
            @RequestParam("file") MultipartFile file
    ) {
        Account account = accountService.findAccount(accountId);

        Picture picture = pictureService.uploadProfilePicture(account, file);

        return ResponseEntity.ok(PictureResponse.from(picture));
    }

    @GetMapping("/retrieve/profile/{accountId}")
    public ResponseEntity<Resource> getProfilePictureForTest(
            @PathVariable Long accountId
    ) {
        Account account = accountService.findAccount(accountId);
        // Metadata needed for contentType
        Picture picture = pictureService.getProfilePicture(account);

        Path filePath = pictureService.getPicturePath(picture);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(picture.getContentType()))
                .body(resource);
    }
}
