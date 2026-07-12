package com.voltstream.grid;

public enum VehicleType {
    SEDAN("Sedan EV", 60.0, 100.0),
    HEAVY_TRUCK("Heavy Truck EV", 120.0, 250.0),
    SCOOTER("Electric Scooter", 4.0, 3.3),
    BUS("Electric Bus", 320.0, 350.0);

    private final String displayName;
    private final double batteryCapacity;
    private final double maxChargingRate;

    VehicleType(String displayName, double batteryCapacity, double maxChargingRate) {
        this.displayName = displayName;
        this.batteryCapacity = batteryCapacity;
        this.maxChargingRate = maxChargingRate;
    }

    public String getDisplayName() { return displayName; }
    public double getBatteryCapacity() { return batteryCapacity; }
    public double getMaxChargingRate() { return maxChargingRate; }
}