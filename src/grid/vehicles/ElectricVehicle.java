package grid.vehicles;

public class ElectricVehicle {
    private final String plateNumber;
    private final double batteryCapacity;
    private final double maxChargingRate;
    private double currentCharge;

    public ElectricVehicle(String plateNumber, double batteryCapacity, double maxChargingRate, double initialChargePercentage) {
        this.plateNumber = plateNumber;
        this.batteryCapacity = batteryCapacity;
        this.maxChargingRate = maxChargingRate;
        this.currentCharge = (initialChargePercentage / 100.0) * batteryCapacity;
    }

    // --- Getters & Setters ---

    public String getPlateNumber() { return plateNumber; }
    public double getBatteryCapacity() { return batteryCapacity; }
    public double getCurrentCharge() { return currentCharge; }
    public double getMaxChargingRate() { return maxChargingRate; }
    public double getChargePercentage() { return (currentCharge / batteryCapacity) * 100.0; }

    // --- Behavior ---

    public void energyDelivered(double kwh) {
        this.currentCharge = Math.min(this.batteryCapacity, this.currentCharge + kwh);
    }

    public boolean isFullyCharged() {
        return this.currentCharge >= this.batteryCapacity;
    }
}
