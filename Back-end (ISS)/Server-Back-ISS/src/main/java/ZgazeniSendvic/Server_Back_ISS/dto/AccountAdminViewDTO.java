package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Account;

public record AccountAdminViewDTO(
        Long id,
        String email,
        String name,
        String lastName,
        String phoneNumber,
        String address,
        boolean confirmed,
        String accountType
) {
    public static AccountAdminViewDTO from(Account a) {
        return new AccountAdminViewDTO(
                a.getId(),
                a.getEmail(),
                a.getName(),
                a.getLastName(),
                a.getPhoneNumber(),
                a.getAddress(),
                a.isConfirmed(),
                a.getClass().getSimpleName() // or discriminator if you have it
        );
    }
}
