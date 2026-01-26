package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.GetAccountDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UpdateAccountDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UpdatedAccountDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    AccountServiceImpl accountService;

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getAccount(@PathVariable Long id) {

        Account acc = accountService.findAccount(id);
        if (acc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        GetAccountDTO dto = new GetAccountDTO();
        dto.setId(acc.getId());
        dto.setEmail(acc.getEmail());
        dto.setName(acc.getName());
        dto.setLastName(acc.getLastName());
        dto.setAddress(acc.getAddress());
        dto.setPhoneNumber(acc.getPhoneNumber());
        dto.setImgString(acc.getImgString());

        return ResponseEntity.ok(dto);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateAccount(
            @RequestBody UpdateAccountDTO dto,
            @PathVariable Long id
    ) {

        try {
            Account updated = accountService.updateAccount(id, dto);

            UpdatedAccountDTO response = new UpdatedAccountDTO();
            response.setId(updated.getId());
            response.setEmail(updated.getEmail());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

