package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @Autowired
    AccountServiceImpl accountService;
    @Autowired
    private AuthenticationManager authenticationManager;





    @PostMapping(path = "register", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> register(@RequestBody RegisterRequestDTO body) throws Exception{


        LoginRequestedDTO loginDTO = accountService.registerAccount(body);

        return new ResponseEntity<LoginRequestedDTO>(loginDTO, HttpStatus.CREATED);

    }

    @PostMapping(path = "login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> login(@RequestBody LoginRequestDTO request) throws Exception {

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword());

        //remember, here is called the service
        Authentication auth = authenticationManager.authenticate(authReq);

        // Security context, contains whos session it is,
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        //jwt Stuff here

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
