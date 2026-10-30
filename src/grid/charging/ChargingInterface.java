package grid.charging;

import grid.exceptions.BayDockingException;
import grid.vehicles.ElectricVehicle;

public interface ChargingInterface {
    double getMaximumHardwareOutput();
    void dock(ElectricVehicle ev) throws BayDockingException;
    void undock();
}
