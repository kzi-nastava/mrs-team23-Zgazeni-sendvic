package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
class AuthController {


    @PostMapping(path = "register", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> register(@RequestBody RegisterRequestDTO body) throws Exception{

        //would check whether creation is acceptable here
        boolean passed = true; //would be handled with service
        if(!passed)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        //successful
        UserLoginDTO userDto = new UserLoginDTO(body.getEmail(), "82340248SAsdad", body.getFirstName(), body.getLastName(),
                body.getPictUrl());

        //login immediately
        LoginRequestedDTO loginDTO = new LoginRequestedDTO("3424asd", "bearer", userDto);

        return new ResponseEntity<LoginRequestedDTO>(loginDTO, HttpStatus.CREATED);

    }

    @PostMapping(path = "login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> login(@RequestBody LoginRequestDTO request) throws Exception {

        //would check login logic here
        boolean passed = true; //would be handled with service
        if(!passed)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        LoginRequestedDTO requested = new LoginRequestedDTO("2131","bearer",new UserLoginDTO());

        return new ResponseEntity<LoginRequestedDTO>(requested,HttpStatus.CREATED);
    }

    @PostMapping(path = "forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendResetEmail(@RequestBody PasswordResetRequestDTO request) throws Exception {
        //would check whether exists etc...

        return new ResponseEntity<String>("Link has been sent if email exists", HttpStatus.CREATED);
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
