package com.voltstream.grid;

import com.voltstream.exception.InvalidVehicleException;

public abstract class ChargingBay implements ChargingInterface {
    private final String bayId;
    private ElectricVehicle dockedVehicle;
    private double currentAllocatedPower;

    public ChargingBay(String bayId) {
        this.bayId = bayId;
        this.dockedVehicle = null;
        this.currentAllocatedPower = 0.0;
    }

    public String getBayId() { return bayId; }
    public ElectricVehicle getDockedVehicle() { return dockedVehicle; }
    public boolean isOccupied() { return dockedVehicle != null; }
    
    public double getCurrentAllocatedPower() { return currentAllocatedPower; }
    public void setAllocatedPower(double power) { this.currentAllocatedPower = power; }

    @Override
    public void dock(ElectricVehicle ev) throws InvalidVehicleException {
        if (ev == null) throw new InvalidVehicleException("Null reference passed to charging port register.");
        if (ev.isFullyCharged()) throw new InvalidVehicleException("Allocation refused: Targets already verify maximum charge parameters.");
        this.dockedVehicle = ev; 
    }

    @Override
    public void undock() { 
        this.dockedVehicle = null; 
        this.currentAllocatedPower = 0.0; 
    }


    @Override
    public abstract double getMaximumHardwareOutput(); 
} 


class UltraFastBay extends ChargingBay {
    public UltraFastBay(String bayId) { super(bayId); }
    @Override 
    public double getMaximumHardwareOutput() { return 350.0; }
}

class FastDCBay extends ChargingBay {
    public FastDCBay(String bayId) { super(bayId); }
    @Override 
    public double getMaximumHardwareOutput() { return 150.0; }
}