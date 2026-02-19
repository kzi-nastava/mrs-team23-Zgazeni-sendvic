package ZgazeniSendvic.Server_Back_ISS.model;

public enum VehicleType {
    STANDARD(300),
    VAN(500),
    LUXURY(800);

    private final int basePrice;

    VehicleType(int basePrice) {
        this.basePrice = basePrice;
    }

    public int getBasePrice() {
        return basePrice;
    }
}

