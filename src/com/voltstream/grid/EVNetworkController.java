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
                    System.out.println("[❌ GRID REJECTION] " + e.getMessage());
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
                double optimalDemand = Math.min(bay.getMaximumHardwareOutput(), ev.getMaxChargingRate());
                bay.setAllocatedPower(optimalDemand);
                totalRequestedPower += optimalDemand;
            }
        }

        if (totalRequestedPower > maxGridCapacityKw) {
            double scalingFactor = maxGridCapacityKw / totalRequestedPower;
            for (ChargingBay bay : stationBays) {
                if (bay.isOccupied()) {
                    bay.setAllocatedPower(bay.getCurrentAllocatedPower() * scalingFactor);
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

                double rate = TariffEngine.calculateCurrentTariff(bay, loadFactor);
                double stepCost = energyDelivered * rate;
                totalRevenueGenerated += stepCost;

                System.out.printf("🔌 [Active] %s -> Unit %s | SoC: %.1f%% | Power Output: %.1f kW | Dynamic Cost: ₹%.2f/kWh%n", 
                    bay.getBayId(), ev.getPlateNumber(), ev.getChargePercentage(), power, rate);

                if (ev.isFullyCharged()) {
                    System.out.printf(" 🧾 [INVOICED] Vehicle %s processing complete. Clearing line state...%n", ev.getPlateNumber());
                    bay.undock();
                }
            } else {
                System.out.printf("🔌 [Idle]   %s -> Standing Ready.%n", bay.getBayId());
            }
        }
        System.out.printf("💰 Consolidated Infrastructure Cashflow: ₹%.2f%n", totalRevenueGenerated);
        VisualUtility.printSeparator();
        optimizeGridPower();
    }
}