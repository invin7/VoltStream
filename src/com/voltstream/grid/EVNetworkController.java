package com.voltstream.grid;

import java.util.ArrayList;
import java.util.List;

import com.voltstream.exception.InvalidVehicleException;
import com.voltstream.util.VisualUtility;

public class EVNetworkController {

    private final List<ChargingBay> stationBays;
    private final double maxGridCapacityKw;
    private double totalRevenueGenerated = 0.0;

    public EVNetworkController(double maxGridCapacityKw) {
        this.stationBays = new ArrayList<>();
        this.maxGridCapacityKw = maxGridCapacityKw;
    }

    public void addBay(ChargingBay bay) {
        stationBays.add(bay);
    }

    public boolean dispatchVehicle(ElectricVehicle ev) {
        for (ChargingBay bay : stationBays) {
            if (!bay.isOccupied()) {
                try {
                    bay.dock(ev);
                    optimizeGridPower();
                    return true;
                } catch (InvalidVehicleException e) {
                    System.out.println("Vehicle rejected: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    public double getGridLoadFactor() {
        double currentLoad = 0.0;

        for (ChargingBay bay : stationBays) {
            if (bay.isOccupied()) {
                currentLoad += bay.getCurrentAllocatedPower();
            }
        }

        return maxGridCapacityKw > 0 ? currentLoad / maxGridCapacityKw : 0.0;
    }

    public void optimizeGridPower() {

        double totalRequestedPower = 0.0;

        for (ChargingBay bay : stationBays) {
            if (bay.isOccupied()) {
                ElectricVehicle ev = bay.getDockedVehicle();
                double requestedPower =Math.min(bay.getMaximumHardwareOutput(), ev.getMaxChargingRate());
                bay.setAllocatedPower(requestedPower);
                totalRequestedPower += requestedPower;
            }
        }

        if (totalRequestedPower > maxGridCapacityKw) {
            double scalingFactor = maxGridCapacityKw / totalRequestedPower;
            for (ChargingBay bay : stationBays) {
                if (bay.isOccupied()) {
                    bay.setAllocatedPower(
                            bay.getCurrentAllocatedPower() * scalingFactor);
                }
            }
        }
    }

    public void executeTimeStep(double hours) {
        double loadFactor = getGridLoadFactor();
        VisualUtility.printStatusHeader(loadFactor);
        for (ChargingBay bay : stationBays) {
            if (bay.isOccupied()) {
                ElectricVehicle ev = bay.getDockedVehicle();
                double power = bay.getCurrentAllocatedPower();
                double energyDelivered = power * hours;
                ev.energyDelivered(energyDelivered);
                double tariff = TariffEngine.calculateCurrentTariff(bay, loadFactor);
                double sessionCost = energyDelivered * tariff;
                totalRevenueGenerated += sessionCost;
                System.out.printf("[Charging] %-25s | Vehicle: %-10s | SoC: %6.1f%% | Power: %6.1f kW | Tariff: ₹%.2f/kWh%n",bay.getBayId(),ev.getPlateNumber(),ev.getChargePercentage(),power,tariff);

                if (ev.isFullyCharged()) {
                    System.out.printf("[Complete] Vehicle %s finished charging.%n", ev.getPlateNumber());

                    System.out.println("           Disconnecting vehicle...\n");

                    bay.undock();
                }

            } else {
                System.out.printf("[Available] %-25s%n",bay.getBayId());
            }
        }

        System.out.printf("%nTotal Revenue : ₹%.2f%n", totalRevenueGenerated);
        VisualUtility.printSeparator();
        optimizeGridPower();
    }
}