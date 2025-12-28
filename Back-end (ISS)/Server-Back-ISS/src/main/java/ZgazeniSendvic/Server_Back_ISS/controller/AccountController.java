package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.GetAccountDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UpdateAccountDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UpdatedAccountDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAccountDTO> getAccount(@PathVariable("id") Long id) {
        GetAccountDTO acc = new GetAccountDTO();

        if (acc == null) {
            return new ResponseEntity<GetAccountDTO>(HttpStatus.NOT_FOUND);
        }

        acc.setId(1L);
        acc.setEmail("user@gmail.com");
        acc.setPassword("user123456");

        return new ResponseEntity<GetAccountDTO>(acc, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedAccountDTO> updateAccount(@RequestBody UpdateAccountDTO acc, @PathVariable Long id) {
        UpdatedAccountDTO updatedAcc = new UpdatedAccountDTO();

        updatedAcc.setId(1L);
        updatedAcc.setEmail(acc.getEmail());
        updatedAcc.setPassword(acc.getPassword());

        return new ResponseEntity<UpdatedAccountDTO>(updatedAcc, HttpStatus.OK);
    }
}
