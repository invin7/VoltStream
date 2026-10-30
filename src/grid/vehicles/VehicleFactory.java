package grid.vehicles;

import grid.exceptions.InvalidVehicleConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;

public class VehicleFactory {

    private record VehicleSpec(String displayName, double batteryCapacity, double maxChargingRate) {}

    private static final Map<String, VehicleSpec> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put("SEDAN", new VehicleSpec("Sedan EV", 60.0, 100.0));
        CATALOG.put("HEAVY_TRUCK", new VehicleSpec("Heavy Truck EV", 120.0, 250.0));
        CATALOG.put("SCOOTER", new VehicleSpec("Electric Scooter", 4.0, 3.3));
        CATALOG.put("BUS", new VehicleSpec("Electric Bus", 320.0, 350.0));
    }

    // --- Getters ---

    public static String[] getAvailableTypes() {
        return CATALOG.keySet().toArray(new String[0]);
    }

    public static String getDisplayName(String type) {
        VehicleSpec spec = CATALOG.get(type);
        return spec != null ? spec.displayName() : type;
    }

    public static double getBatteryCapacity(String type) {
        VehicleSpec spec = CATALOG.get(type);
        return spec != null ? spec.batteryCapacity() : 0.0;
    }

    public static double getMaxChargingRate(String type) {
        VehicleSpec spec = CATALOG.get(type);
        return spec != null ? spec.maxChargingRate() : 0.0;
    }

    // --- Factory method ---

    public static ElectricVehicle createVehicle(String type, String plateNumber, double initialChargePercentage)
            throws InvalidVehicleConfigurationException {
        VehicleSpec spec = CATALOG.get(type);
        if (spec == null) {
            throw new InvalidVehicleConfigurationException("Unknown vehicle type: " + type);
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

        return new ElectricVehicle(plateNumber, spec.batteryCapacity(), spec.maxChargingRate(), initialChargePercentage);
    }
}
