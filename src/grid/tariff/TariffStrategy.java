package grid.tariff;

public interface TariffStrategy {
    double getMultiplier(double gridLoadFactor);
}
