package grid.charging;

import java.util.Objects;

public class ChargingBayFactory {

    public static ChargingBay createBay(BayType type, String bayId) {
        Objects.requireNonNull(type, "Bay type must not be null");
        return switch (type) {
            case ULTRA_FAST -> new UltraFastBay(bayId);
            case FAST_DC -> new FastDCBay(bayId);
        };
    }
}
