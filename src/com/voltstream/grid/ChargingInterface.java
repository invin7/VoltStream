package com.voltstream.grid;

public interface ChargingInterface {
    double getMaximumHardwareOutput();
    void dock(ElectricVehicle ev) throws com.voltstream.exception.InvalidVehicleException;
    void undock();
}