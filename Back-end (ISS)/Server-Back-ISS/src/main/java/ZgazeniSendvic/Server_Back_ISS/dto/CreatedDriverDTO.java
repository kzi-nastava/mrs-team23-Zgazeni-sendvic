package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;

public class CreatedDriverDTO {
    private Long id;
    private Account account;
    private Vehicle vehicle;

    public CreatedDriverDTO() { super(); }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
