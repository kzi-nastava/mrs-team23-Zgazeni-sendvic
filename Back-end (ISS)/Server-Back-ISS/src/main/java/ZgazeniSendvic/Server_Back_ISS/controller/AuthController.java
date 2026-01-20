package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @Autowired
    AccountServiceImpl accountService;


    @PostMapping(path = "register", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> register(@RequestBody RegisterRequestDTO body) throws Exception{


        LoginRequestedDTO loginDTO = accountService.registerAccount(body);

        return new ResponseEntity<LoginRequestedDTO>(loginDTO, HttpStatus.CREATED);

    }

    @PostMapping(path = "login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> login(@RequestBody LoginRequestDTO request) throws Exception {

        LoginRequestedDTO loginDTO = accountService.login(request);

        return new ResponseEntity<LoginRequestedDTO>(loginDTO, HttpStatus.CREATED);
    }

    @PostMapping(path = "forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendResetEmail(@RequestBody PasswordResetRequestDTO request) throws Exception {
        //would check whether exists etc...

        return new ResponseEntity<String>("Link has been sent if the email is correct", HttpStatus.CREATED);
    }

    @PostMapping(path = "reset-password", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> confirmPasswordReset(@RequestBody PasswordResetConfirmedRequestDTO request)
            throws Exception {
        //would change the password etc.;

        return new ResponseEntity<String>("Password Reset successful", HttpStatus.CREATED);
        //redirection to login would ensue? or auto login?
    }



}
