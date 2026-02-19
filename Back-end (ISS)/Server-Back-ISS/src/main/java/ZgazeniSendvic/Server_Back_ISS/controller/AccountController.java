package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.GetAccountDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UpdateAccountDTO;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;

import ZgazeniSendvic.Server_Back_ISS.service.ChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    AccountServiceImpl accountService;

    @Autowired
    ChangeRequestService changeRequestService;

    @PreAuthorize("hasAnyRole('DRIVER','ACCOUNT','USER')")
    @GetMapping(value = "/me")
    public ResponseEntity<GetAccountDTO> getMyAccount(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getName();
        Account acc = accountService.findByEmail(email);

        if (acc == null) {
            return ResponseEntity.notFound().build();
        }

        GetAccountDTO dto = new GetAccountDTO();
        dto.setId(acc.getId());
        dto.setEmail(acc.getEmail());
        dto.setName(acc.getName());
        dto.setLastName(acc.getLastName());
        dto.setPhoneNumber(acc.getPhoneNumber());
        dto.setAddress(acc.getAddress());
        dto.setImgString(acc.getImgString());
        dto.setRole(acc.getClass().getSimpleName().toUpperCase());

        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('DRIVER','ACCOUNT','USER')")
    @PutMapping(value = "/me/change-request",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateAccount(@RequestBody UpdateAccountDTO dto) {
        Account updated = accountService.updateAccount(dto);
        return ResponseEntity.ok("Account updated: " + updated);
    }

    @PreAuthorize("hasRole('ACCOUNT')")
    @PutMapping("/approve-driver-changes/{id}")
    public ResponseEntity<String> approveDriverChange(@PathVariable Long id) {

        Account current = accountService.getCurrentAccount();

        if (!(current instanceof Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Not authorized.");
        }

        ProfileChangeRequest request = changeRequestService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Change request not found"));

        Account driver = accountService.findAccount(request.getAccountId());

        if (!(driver instanceof Driver)) {
            return ResponseEntity.badRequest().body("Account is not a driver.");
        }

        // Apply changes
        driver.setName(request.getNewName());
        driver.setLastName(request.getNewLastName());
        driver.setAddress(request.getNewAddress());
        driver.setPhoneNumber(request.getNewPhoneNumber());
        driver.setImgString(request.getNewImgString());

        accountService.update(driver);

        request.setStatus(RequestStatus.ACCEPTED);
        changeRequestService.save(request);

        return ResponseEntity.ok("Changes approved.");
    }

    @PreAuthorize("hasRole('ACCOUNT')")
    @PutMapping("/ban/{id}")
    public ResponseEntity<String> ban(@PathVariable Long id) {
        Account current = accountService.getCurrentAccount();
        if (!(current instanceof Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Not authorized.");
        }

        Account banned = accountService.findAccount(id);
        banned.setIsBanned(true);
        accountService.update(banned);
        return ResponseEntity.ok("Account banned.");
    }
}

