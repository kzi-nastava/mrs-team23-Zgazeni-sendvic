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

@RestController
@RequestMapping("/api/pictures")
public class PictureController {

   @Autowired
   private AccountServiceImpl accountService;
   @Autowired
   private PictureService pictureService;


    @PostMapping("/me/profile/{accountId}")
    public ResponseEntity<PictureResponse> uploadMyProfilePicture(
            @PathVariable Long accountId,
            @RequestParam("file") MultipartFile file
    ) {
        Account account = accountService.findAccount(accountId);

        Picture picture = pictureService.uploadProfilePicture(account, file);

        return ResponseEntity.ok(PictureResponse.from(picture));
    }
}
