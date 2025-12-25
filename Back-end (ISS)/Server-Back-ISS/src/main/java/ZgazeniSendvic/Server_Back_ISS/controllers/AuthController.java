package ZgazeniSendvic.Server_Back_ISS.controllers;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
class AuthController {

    @PostMapping(path = "login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> login(@RequestBody LoginRequestDTO request) throws Exception {

        //would check login logic here

        LoginRequestedDTO requested = new LoginRequestedDTO("2131","bearer",new UserDTO());

        return new ResponseEntity<LoginRequestedDTO>(requested,HttpStatus.OK);
    }

    @PostMapping(path = "forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendResetEmail(@RequestBody PasswordResetRequestDTO request) throws Exception {
        //would check wether exists etc...

        return new ResponseEntity<String>("Link sent if email exists", HttpStatus.OK);
    }

    @PostMapping(path = "reset-password", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> confirmPasswordReset(@RequestBody PasswordResetConfirmedRequestDTO request)
            throws Exception {
        //would change the password etc;

        return new ResponseEntity<String>("Password Reset successful", HttpStatus.OK);
        //redirection to login would ensue? or auto login?
    }

}
