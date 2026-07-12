package com.voltstream.grid;

public abstract class ElectricVehicle {
    private final String plateNumber;
    private final double batteryCapacity;
    private double currentCharge;       
    
    public ElectricVehicle(String plateNumber, double batteryCapacity, double initialChargePercentage) {
        this.plateNumber = plateNumber;
        this.batteryCapacity = batteryCapacity;
        this.currentCharge = (initialChargePercentage / 100.0) * batteryCapacity;
    }


    public String getPlateNumber() { return plateNumber; }
    public double getBatteryCapacity() { return batteryCapacity; }
    public double getCurrentCharge() { return currentCharge; }
    
    public double getChargePercentage() {
        return (currentCharge / batteryCapacity) * 100.0;
    }


    public void energyDelivered(double kwh) {
        this.currentCharge = Math.min(this.batteryCapacity, this.currentCharge + kwh);
    }

    public boolean isFullyCharged() {
        return this.currentCharge >= this.batteryCapacity;
    }


    public abstract double getMaxChargingRate(); 
}