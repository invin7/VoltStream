package grid.events;

import grid.charging.ChargingBay;
import grid.vehicles.ElectricVehicle;

public interface ChargingEventListener {
    void onVehicleDocked(ChargingBay bay, ElectricVehicle ev);
    void onChargingComplete(ChargingBay bay, ElectricVehicle ev);
    void onStepStart(double gridLoadFactor);
    void onBayCharging(ChargingBay bay, ElectricVehicle ev, double power, double tariff);
    void onBayIdle(ChargingBay bay);
    void onStepEnd(double totalRevenue);
}
