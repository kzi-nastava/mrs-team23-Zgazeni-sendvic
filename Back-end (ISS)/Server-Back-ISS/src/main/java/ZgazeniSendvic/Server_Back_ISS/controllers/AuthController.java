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

        return "Register info sent successfully \n" + body;

    }

    @GetMapping("login")
    public String getLogin(){
        return "Login Page requested Successfully";
    }

    @PostMapping("login")
    public String login(@RequestBody String body){
        return "Login info sent successfully \n" + body;
    }

    @GetMapping("forgotten-password/{email}")
    public String forgotPassword(@PathVariable String email){
        return "Sent Email: " + email;
    }

    @PostMapping("reset-password")
    public String resetPassword(@RequestBody String body){

        return "resetPass info sent!";
    }

}
