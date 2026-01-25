package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
import ZgazeniSendvic.Server_Back_ISS.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @Autowired
    AccountServiceImpl accountService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    TokenUtils tokenUtils;





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

        // The func from the UserDetailsService is called here
        Authentication auth = authenticationManager.authenticate(authReq);

        // If the login is successful, sc gets set
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        //JWT created for the user
        // This would eventually work, though Account would I think need to implement UserDetails
        // Also userDetailsService would maybe need to return Account as well, as it does in certain examples
        //Account account = (Account) auth.getPrincipal();

        //as previous checks passed, account must exist
        Account account = accountService.findAccountByEmail(request.getEmail());
        String jwt = tokenUtils.generateToken(account);
        int expiresIn = tokenUtils.getExpiredIn();


        LoginRequestedDTO loginDTO = new LoginRequestedDTO(jwt, expiresIn, new AccountLoginDTO(account));

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
