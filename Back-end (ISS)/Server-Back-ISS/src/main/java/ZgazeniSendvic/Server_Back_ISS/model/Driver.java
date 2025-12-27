package ZgazeniSendvic.Server_Back_ISS.model;

public class Driver {
    private Long id;
    private Account account;
    private Vehicle vehicle;

    public Driver() { super(); }

    public Driver(Long id, Account account, Vehicle vehicle) {
        super();
        this.id = id;
        this.account = account;
        this.vehicle = vehicle;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Account getAccount() { return account; }

    public void setAccount(Account account) { this.account = account; }

    public Vehicle getVehicle() { return vehicle; }

    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
}
