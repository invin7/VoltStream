package grid.vehicles;

import grid.exceptions.InvalidVehicleConfigurationException;

public class VehicleFactory {

    public static ElectricVehicle createVehicle(VehicleType type, String plateNumber, double initialChargePercentage)
            throws InvalidVehicleConfigurationException {
        if (type == null) {
            throw new InvalidVehicleConfigurationException("Vehicle type must be specified.");
        }
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new InvalidVehicleConfigurationException("License plate cannot be empty.");
        }
        if (Double.isNaN(initialChargePercentage) || initialChargePercentage < 0.0) {
            throw new InvalidVehicleConfigurationException("Charge percentage cannot be negative.");
        }
        if (initialChargePercentage >= 100.0) {
            throw new InvalidVehicleConfigurationException("Invalid charge percentage: vehicle must be below 100%.");
        }

        return new ElectricVehicle(type, plateNumber, initialChargePercentage);
    }
}
