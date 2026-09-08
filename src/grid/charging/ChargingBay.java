package grid.charging;

import grid.vehicles.ElectricVehicle;
import grid.tariff.TariffStrategy;

public abstract class ChargingBay implements ChargingInterface {

    private static final double BASE_RATE_PER_KWH = 8.50;

    private final String bayId;
    private ElectricVehicle dockedVehicle;
    private double currentAllocatedPower;
    private final TariffStrategy tariffStrategy;

    public ChargingBay(String bayId, TariffStrategy tariffStrategy) {
        this.bayId = bayId;
        this.dockedVehicle = null;
        this.currentAllocatedPower = 0.0;
        this.tariffStrategy = tariffStrategy;
    }

    // --- Getters & Setters ---

    public String getBayId() { return bayId; }
    public ElectricVehicle getDockedVehicle() { return dockedVehicle; }
    public boolean isOccupied() { return dockedVehicle != null; }
    public double getCurrentAllocatedPower() { return currentAllocatedPower; }
    public void setAllocatedPower(double power) { this.currentAllocatedPower = power; }

    // --- Behavior ---

    public double getBillableRate(double gridLoadFactor) {
        return BASE_RATE_PER_KWH * tariffStrategy.getMultiplier(gridLoadFactor);
    }

    @Override
    public boolean dock(ElectricVehicle ev) {
        if (ev == null) return false;
        if (ev.isFullyCharged()) return false;
        this.dockedVehicle = ev;
        return true;
    }

    @Override
    public void undock() {
        this.dockedVehicle = null;
        this.currentAllocatedPower = 0.0;
    }

    @Override
    public abstract double getMaximumHardwareOutput();
}
