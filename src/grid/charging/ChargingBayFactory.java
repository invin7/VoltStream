package grid.charging;

public class ChargingBayFactory {

    public static ChargingBay createBay(String type, String bayId) {
        switch (type) {
            case "ULTRA_FAST":
                return new UltraFastBay(bayId);
            case "FAST_DC":
                return new FastDCBay(bayId);
            default:
                throw new IllegalArgumentException("Unknown bay type: " + type);
        }
    }
}
