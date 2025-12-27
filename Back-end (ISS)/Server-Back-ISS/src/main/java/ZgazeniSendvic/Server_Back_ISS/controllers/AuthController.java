package ZgazeniSendvic.Server_Back_ISS.controllers;

import ZgazeniSendvic.Server_Back_ISS.dto.LoginRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RegisterRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UserLoginDTO;
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

        //would check wether creation is acceptable here
        //successful
        UserLoginDTO userLoginDto = new UserLoginDTO(body.getEmail(), "82340248SAsdad", body.getFirstName(), body.getLastName(),
                body.getAddress(), body.getPhoneNum(), body.getPictUrl());

        //login immediately
        LoginRequestedDTO loginDTO = new LoginRequestedDTO("3424asd", "bearer", userLoginDto);

        return new ResponseEntity<LoginRequestedDTO>(loginDTO, HttpStatus.CREATED);

    }

}
