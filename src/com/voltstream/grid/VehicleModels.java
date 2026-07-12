package com.voltstream.grid;

class GenericEV extends ElectricVehicle {
    private final VehicleType type;

    public GenericEV(String plateNumber, VehicleType type, double initialChargePercentage) 
            throws com.voltstream.exception.InvalidVehicleException { // <-- Add the throws declaration here
        
        super(plateNumber, type.getBatteryCapacity(), initialChargePercentage);
        
        // Explicitly enforce the exception safeguard condition
        if (initialChargePercentage >= 100.0) {
            throw new com.voltstream.exception.InvalidVehicleException(
                "Targets already verify maximum charge parameters."
            );
        }
        
        this.type = type;
    }

    @Override
    public double getMaxChargingRate() {
        return type.getMaxChargingRate();
    }
}