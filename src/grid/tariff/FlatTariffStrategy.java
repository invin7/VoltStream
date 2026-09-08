package grid.tariff;

public class FlatTariffStrategy implements TariffStrategy {

    private final double multiplier;

    public FlatTariffStrategy(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public double getMultiplier(double gridLoadFactor) {
        return multiplier;
    }
}
