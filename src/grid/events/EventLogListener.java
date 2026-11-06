package grid.events;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import grid.charging.ChargingBay;
import grid.vehicles.ElectricVehicle;

/** Records controller events so the web dashboard can show them as an activity feed. */
public class EventLogListener implements ChargingEventListener {

    public record Entry(int step, String kind, String message) { }

    private static final int MAX_ENTRIES = 200;

    private final Deque<Entry> entries = new ArrayDeque<>();
    private int stepCount = 0;

    // --- Getters ---

    public int getStepCount() { return stepCount; }
    public List<Entry> getEntries() { return new ArrayList<>(entries); }

    // --- Events ---

    @Override
    public void onVehicleDocked(ChargingBay bay, ElectricVehicle ev) {
        record("docked", String.format("%s docked at %s", ev.getPlateNumber(), bay.getBayId()));
    }

    @Override
    public void onChargingComplete(ChargingBay bay, ElectricVehicle ev) {
        record("complete", String.format("%s finished charging at %s", ev.getPlateNumber(), bay.getBayId()));
    }

    @Override
    public void onStepStart(double gridLoadFactor) {
        stepCount++;
    }

    @Override
    public void onBayCharging(ChargingBay bay, ElectricVehicle ev, double power, double tariff) {
        record("charging", String.format(Locale.ROOT, "%s at %.1f%%, drawing %.1f kW at \u20b9%.2f/kWh",
                ev.getPlateNumber(), ev.getChargePercentage(), power, tariff));
    }

    @Override
    public void onBayIdle(ChargingBay bay) { }

    @Override
    public void onStepEnd(double totalRevenue) {
        record("step", String.format(Locale.ROOT, "Step %d complete. Total revenue \u20b9%,.2f", stepCount, totalRevenue));
    }

    private void record(String kind, String message) {
        entries.addFirst(new Entry(stepCount, kind, message));
        if (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }
}
