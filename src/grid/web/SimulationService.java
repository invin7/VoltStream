package grid.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import grid.charging.BayType;
import grid.charging.ChargingBay;
import grid.charging.ChargingBayFactory;
import grid.core.EVNetworkController;
import grid.events.EventLogListener;
import grid.exceptions.BayDockingException;
import grid.exceptions.InvalidVehicleConfigurationException;
import grid.exceptions.NoAvailableBayException;
import grid.vehicles.ElectricVehicle;
import grid.vehicles.VehicleFactory;
import grid.vehicles.VehicleType;

/** Owns the running simulation and turns its state into the data the frontend renders. */
public class SimulationService {

    private static final double GRID_CAPACITY = 400.0;
    private static final double TIME_STEP = 0.2;

    private EVNetworkController controller;
    private EventLogListener eventLog;

    public SimulationService() {
        reset();
    }

    // --- Actions ---

    public void reset() {
        controller = new EVNetworkController(GRID_CAPACITY);
        eventLog = new EventLogListener();
        controller.addListener(eventLog);

        controller.addBay(ChargingBayFactory.createBay(BayType.ULTRA_FAST, "BAY-01"));
        controller.addBay(ChargingBayFactory.createBay(BayType.ULTRA_FAST, "BAY-02"));
        controller.addBay(ChargingBayFactory.createBay(BayType.FAST_DC, "BAY-03"));
    }

    public void addVehicle(String type, String plate, String charge)
            throws InvalidVehicleConfigurationException, NoAvailableBayException, BayDockingException {
        double soc;

        try {
            soc = Double.parseDouble(charge == null ? "" : charge.trim());
        } catch (NumberFormatException e) {
            throw new InvalidVehicleConfigurationException("Invalid percentage.");
        }

        String normalizedPlate = plate == null ? null : plate.trim().toUpperCase();
        ElectricVehicle newEV = VehicleFactory.createVehicle(parseVehicleType(type), normalizedPlate, soc);
        controller.dispatchVehicle(newEV);
    }

    public void step() {
        controller.executeTimeStep(TIME_STEP);
    }

    // --- Snapshot ---

    public Map<String, Object> snapshot() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("stepMinutes", TIME_STEP * 60);
        state.put("stepCount", eventLog.getStepCount());
        state.put("revenue", controller.getTotalRevenue());
        state.put("grid", gridState());
        state.put("bays", bayStates());
        state.put("vehicleTypes", vehicleTypes());
        state.put("events", eventStates());
        return state;
    }

    private Map<String, Object> gridState() {
        double requested = 0.0;
        double allocated = 0.0;

        for (ChargingBay bay : controller.getBays()) {
            requested += bay.getRequestedPower();
            allocated += bay.getCurrentAllocatedPower();
        }

        Map<String, Object> grid = new LinkedHashMap<>();
        grid.put("capacityKw", controller.getMaxGridCapacityKw());
        grid.put("requestedKw", requested);
        grid.put("allocatedKw", allocated);
        grid.put("loadFactor", controller.getGridLoadFactor());
        grid.put("peakThreshold", ChargingBay.PEAK_LOAD_THRESHOLD);
        return grid;
    }

    private List<Map<String, Object>> bayStates() {
        double loadFactor = controller.getGridLoadFactor();
        List<Map<String, Object>> bays = new ArrayList<>();

        for (ChargingBay bay : controller.getBays()) {
            double tariff = bay.getBillableRate(loadFactor);

            Map<String, Object> state = new LinkedHashMap<>();
            state.put("id", bay.getBayId());
            state.put("typeName", bay.getBayType().getDisplayName());
            state.put("maxOutputKw", bay.getMaximumHardwareOutput());
            state.put("requestedKw", bay.getRequestedPower());
            state.put("allocatedKw", bay.getCurrentAllocatedPower());
            state.put("tariff", tariff);
            state.put("surcharged", tariff > bay.getBillableRate(0.0));
            state.put("vehicle", bay.isOccupied() ? vehicleState(bay.getDockedVehicle()) : null);
            bays.add(state);
        }

        return bays;
    }

    private Map<String, Object> vehicleState(ElectricVehicle ev) {
        Map<String, Object> vehicle = new LinkedHashMap<>();
        vehicle.put("plate", ev.getPlateNumber());
        vehicle.put("typeName", ev.getType().getDisplayName());
        vehicle.put("chargePercent", ev.getChargePercentage());
        vehicle.put("currentKwh", ev.getCurrentCharge());
        vehicle.put("batteryKwh", ev.getBatteryCapacity());
        return vehicle;
    }

    private List<Map<String, Object>> vehicleTypes() {
        List<Map<String, Object>> types = new ArrayList<>();

        for (VehicleType type : VehicleType.values()) {
            Map<String, Object> state = new LinkedHashMap<>();
            state.put("id", type.name());
            state.put("name", type.getDisplayName());
            state.put("batteryKwh", type.getBatteryCapacity());
            state.put("maxRateKw", type.getMaxChargingRate());
            types.add(state);
        }

        return types;
    }

    private List<Map<String, Object>> eventStates() {
        List<Map<String, Object>> events = new ArrayList<>();

        for (EventLogListener.Entry entry : eventLog.getEntries()) {
            Map<String, Object> state = new LinkedHashMap<>();
            state.put("step", entry.step());
            state.put("kind", entry.kind());
            state.put("message", entry.message());
            events.add(state);
        }

        return events;
    }

    private VehicleType parseVehicleType(String type) {
        for (VehicleType candidate : VehicleType.values()) {
            if (candidate.name().equals(type)) {
                return candidate;
            }
        }
        return null;
    }
}
