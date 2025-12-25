package ZgazeniSendvic.Server_Back_ISS.controllers;

import ZgazeniSendvic.Server_Back_ISS.dto.LoginRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.LoginRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UserDTO;
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

}
