package grid.charging;

import grid.exceptions.BayDockingException;
import grid.vehicles.ElectricVehicle;
import grid.tariff.TariffStrategy;

public abstract class ChargingBay implements ChargingInterface {

    public static final double PEAK_LOAD_THRESHOLD = 0.75;
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

    public double getRequestedPower() {
        if (!isOccupied()) return 0.0;
        return Math.min(getMaximumHardwareOutput(), dockedVehicle.getMaxChargingRate());
    }

    public double getBillableRate(double gridLoadFactor) {
        return BASE_RATE_PER_KWH * tariffStrategy.getMultiplier(gridLoadFactor);
    }

    @Override
    public void dock(ElectricVehicle ev) throws BayDockingException {
        if (ev == null) throw new BayDockingException("Cannot dock a null vehicle.");
        if (ev.isFullyCharged()) throw new BayDockingException("Vehicle " + ev.getPlateNumber() + " is already fully charged.");
        if (isOccupied()) throw new BayDockingException("Bay " + bayId + " is already occupied.");
        this.dockedVehicle = ev;
    }

    @Override
    public void undock() {
        this.dockedVehicle = null;
        this.currentAllocatedPower = 0.0;
    }

    @Override
    public abstract double getMaximumHardwareOutput();

    public abstract BayType getBayType();
}
