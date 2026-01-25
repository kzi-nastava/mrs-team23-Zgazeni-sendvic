package ZgazeniSendvic.Server_Back_ISS.dto;

public class RegisteredVehicleDTO {

    private Long id;
    private String registration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }
}