package grid.events;

import grid.charging.ChargingBay;
import grid.vehicles.ElectricVehicle;
import grid.util.VisualUtility;

public class ConsoleDashboardListener implements ChargingEventListener {

    @Override
    public void onVehicleDocked(ChargingBay bay, ElectricVehicle ev) {
        System.out.printf("[Event] Vehicle %s docked at %s%n", ev.getPlateNumber(), bay.getBayId());
    }

    @Override
    public void onChargingComplete(ChargingBay bay, ElectricVehicle ev) {
        System.out.printf("[Event] Vehicle %s finished charging at %s%n", ev.getPlateNumber(), bay.getBayId());
    }

    @Override
    public void onStepStart(double gridLoadFactor) {
        VisualUtility.printStatusHeader(gridLoadFactor);
    }

    @Override
    public void onBayCharging(ChargingBay bay, ElectricVehicle ev, double power, double tariff) {
        System.out.printf("[Charging] %-25s | Vehicle: %-10s | SoC: %6.1f%% | Power: %6.1f kW | Tariff: \u20b9%.2f/kWh%n",
                bay.getBayId(), ev.getPlateNumber(), ev.getChargePercentage(), power, tariff);
    }

    @Override
    public void onBayIdle(ChargingBay bay) {
        System.out.printf("[Available] %-25s%n", bay.getBayId());
    }

    @Override
    public void onStepEnd(double totalRevenue) {
        System.out.printf("%nTotal Revenue : \u20b9%.2f%n", totalRevenue);
        VisualUtility.printSeparator();
    }
}
