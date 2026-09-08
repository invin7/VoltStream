package grid.charging;

import grid.vehicles.ElectricVehicle;

public interface ChargingInterface {
    double getMaximumHardwareOutput();
    boolean dock(ElectricVehicle ev);
    void undock();
}
