package ZgazeniSendvic.Server_Back_ISS.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
class AuthController {

    @GetMapping("register")
    public String getRegister(){
        /* Would send the Register page, so that the client can load */
        return "Register Page requested Successfully";
    }

    @PostMapping("register")
    public String register(@RequestBody String body){

        return "Register info sent successfully";

    }

}
