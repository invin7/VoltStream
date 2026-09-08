package grid.tariff;

public class PeakSurchargeTariffStrategy implements TariffStrategy {

    private final double baseMultiplier;
    private final double peakSurcharge;
    private final double loadThreshold;

    public PeakSurchargeTariffStrategy(double baseMultiplier, double peakSurcharge, double loadThreshold) {
        this.baseMultiplier = baseMultiplier;
        this.peakSurcharge = peakSurcharge;
        this.loadThreshold = loadThreshold;
    }

    @Override
    public double getMultiplier(double gridLoadFactor) {
        if (gridLoadFactor > loadThreshold) {
            return baseMultiplier + peakSurcharge;
        }
        return baseMultiplier;
    }
}
