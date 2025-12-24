package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.GetAccountDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAccountDTO> getAccount(@PathVariable("id") Long id) {
        GetAccountDTO acc = new GetAccountDTO();

        if (acc == null) {
            return new ResponseEntity<GetAccountDTO>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<GetAccountDTO>(acc, HttpStatus.OK);
    }
}
