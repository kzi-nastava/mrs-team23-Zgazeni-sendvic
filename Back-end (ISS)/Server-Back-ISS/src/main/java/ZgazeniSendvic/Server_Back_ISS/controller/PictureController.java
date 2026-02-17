package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.PictureResponse;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Picture;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.security.jwt.JwtUtils;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
import ZgazeniSendvic.Server_Back_ISS.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

   @Autowired
   JwtUtils tokenUtils;



    @PostMapping("/register/profile")
    public ResponseEntity<PictureResponse> uploadMyProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pictureToken") String pictureToken
    ) throws Exception{
        try{
        String email = tokenUtils.getUsernameFromToken(pictureToken);
        if (email == null) {
            return ResponseEntity.notFound().build();
        }
        //shouldn't fail, because if token is proper, surely the email inside is too, but what if it was deleted?
        //could cover with global exception or one big try catch tbh
        Account account = accountService.findAccountByEmail(email);

        Picture picture = pictureService.uploadProfilePicture(account, file);

        return ResponseEntity.ok(PictureResponse.from(picture));
        } catch (Exception e){
            //account didnt exist
            return ResponseEntity.badRequest().build();
        }
    }


    //Time to fix, this is, of course, going to assume proper auth was established
    @GetMapping("/retrieve/profile")
    public ResponseEntity<Resource> getProfilePictureForTest(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws Exception{
        //returns 404 if TOKEN INVALID
        if(userDetails == null) {
            return ResponseEntity.notFound().build();
        }
        Account accountTest = userDetails.getAccount();
        //System.out.println(accountTest);
        // Metadata needed for contentType
        Picture picture = pictureService.getProfilePicture(accountTest);

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
