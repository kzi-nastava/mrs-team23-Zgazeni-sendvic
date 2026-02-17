package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.security.jwt.JwtUtils;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
import ZgazeniSendvic.Server_Back_ISS.service.DriverServiceImpl;
//import ZgazeniSendvic.Server_Back_ISS.util.TokenUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin (origins="*")
@RequestMapping("/api/auth")
class AuthController {

    @Autowired
    AccountServiceImpl accountService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils tokenUtils;
    @Autowired
    DriverServiceImpl driverService;





    @PostMapping(path = "register", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDTO body) throws Exception{


            LoginRequestedDTO loginDTO = accountService.registerAccount(body);
            String  pictureToken = tokenUtils.generateToken(accountService.findAccountByEmail(body.getEmail()));


        Map<String, String> response = new HashMap<>();
        response.put("pictureToken", pictureToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);


    }

    @PostMapping(path = "login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginRequestedDTO> login(@Valid @RequestBody LoginRequestDTO request) throws Exception {

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

        //all checks passed
        driverService.ActivateIfDriver(request.getEmail());
        LoginRequestedDTO loginDTO = new LoginRequestedDTO(jwt, expiresIn, new AccountLoginDTO(account), account.getRole());

        return new ResponseEntity<LoginRequestedDTO>(loginDTO, HttpStatus.CREATED);
    }

    @PostMapping(path = "forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendResetEmail(@RequestBody PasswordResetRequestDTO request) throws Exception {
        //So I need to receive the mail, and based on it check wether in database if so send email
        //if not, do nothing, do not reveal anything, sounds relatively simple tbh
        accountService.forgotPassword(request.getEmail());

        return new ResponseEntity<String>("Link has been sent if an account exists", HttpStatus.OK);
    }

    @PostMapping(path = "reset-password", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> confirmPasswordReset(@RequestBody PasswordResetConfirmedRequestDTO request)
            throws Exception {
        //would change the password etc.;

        accountService.resetPassword(request);

        return new ResponseEntity<String>("Password Reset successful", HttpStatus.CREATED);
        //redirection to login would ensue? or auto login?
    }

    @PostMapping(path = "confirm-account", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> confirmAccount(@RequestBody AccountConfirmationDTO request)
            throws Exception {
        //would change the password etc.;

        accountService.confirmAccount(request);

        return new ResponseEntity<String>("Account confirmation successful", HttpStatus.CREATED);
        //redirection to login would ensue? or auto login?
    }


    @PostMapping(path = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logOut(@RequestBody logOutDTO request)
            throws Exception {


        if(driverService.isAvailableDriver(request.getEmail())) {
            return new ResponseEntity<String>("Driver must be unavailable", HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<String>("Log Out successful", HttpStatus.OK);

    }



}
