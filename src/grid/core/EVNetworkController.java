package grid.core;

import java.util.ArrayList;
import java.util.List;

import grid.charging.ChargingBay;
import grid.exceptions.BayDockingException;
import grid.exceptions.InvalidGridConfigurationException;
import grid.exceptions.NoAvailableBayException;
import grid.vehicles.ElectricVehicle;
import grid.events.ChargingEventListener;

public class EVNetworkController {

    private final List<ChargingBay> stationBays;
    private final List<ChargingEventListener> listeners;
    private final double maxGridCapacityKw;
    private double totalRevenueGenerated = 0.0;

    public EVNetworkController(double maxGridCapacityKw) {
        if (maxGridCapacityKw <= 0) {
            throw new InvalidGridConfigurationException("Grid capacity must be positive: " + maxGridCapacityKw);
        }
        this.stationBays = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.maxGridCapacityKw = maxGridCapacityKw;
    }

    public void addBay(ChargingBay bay) {
        stationBays.add(bay);
    }

    public void addListener(ChargingEventListener listener) {
        listeners.add(listener);
    }

    private void notifyDocked(ChargingBay bay, ElectricVehicle ev) {
        for (ChargingEventListener listener : listeners) {
            listener.onVehicleDocked(bay, ev);
        }
    }

    private void notifyChargingComplete(ChargingBay bay, ElectricVehicle ev) {
        for (ChargingEventListener listener : listeners) {
            listener.onChargingComplete(bay, ev);
        }
    }

    private void notifyStepStart(double gridLoadFactor) {
        for (ChargingEventListener listener : listeners) {
            listener.onStepStart(gridLoadFactor);
        }
    }

    private void notifyBayCharging(ChargingBay bay, ElectricVehicle ev, double power, double tariff) {
        for (ChargingEventListener listener : listeners) {
            listener.onBayCharging(bay, ev, power, tariff);
        }
    }

    private void notifyBayIdle(ChargingBay bay) {
        for (ChargingEventListener listener : listeners) {
            listener.onBayIdle(bay);
        }
    }

    private void notifyStepEnd(double totalRevenue) {
        for (ChargingEventListener listener : listeners) {
            listener.onStepEnd(totalRevenue);
        }
    }

    public void dispatchVehicle(ElectricVehicle ev) throws NoAvailableBayException, BayDockingException {
        for (ChargingBay bay : stationBays) {
            if (!bay.isOccupied()) {
                bay.dock(ev);
                notifyDocked(bay, ev);
                optimizeGridPower();
                return;
            }
        }
        throw new NoAvailableBayException();
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
        if (hours < 0 || Double.isNaN(hours)) {
            throw new InvalidGridConfigurationException("Time step cannot be negative: " + hours);
        }
        double loadFactor = getGridLoadFactor();
        notifyStepStart(loadFactor);
        for (ChargingBay bay : stationBays) {
            if (bay.isOccupied()) {
                ElectricVehicle ev = bay.getDockedVehicle();
                double power = bay.getCurrentAllocatedPower();
                double energyDelivered = power * hours;
                ev.energyDelivered(energyDelivered);
                double tariff = bay.getBillableRate(loadFactor);
                double sessionCost = energyDelivered * tariff;
                totalRevenueGenerated += sessionCost;
                notifyBayCharging(bay, ev, power, tariff);

                if (ev.isFullyCharged()) {
                    notifyChargingComplete(bay, ev);
                    bay.undock();
                }

            } else {
                notifyBayIdle(bay);
            }
        }

        notifyStepEnd(totalRevenueGenerated);
        optimizeGridPower();
    }
}
