package com.voltstream.grid;

public class TariffEngine {
    private static final double BASE_RATE_PER_KWH = 8.50;

    public static double calculateCurrentTariff(ChargingBay bay, double gridLoadFactor) {
        double multiplier = 1.0;

        if (bay instanceof UltraFastBay) {
            multiplier += 0.5;
        } else if (bay instanceof FastDCBay) {
            multiplier += 0.2;
        }

        if (gridLoadFactor > 0.75) {
            multiplier += 0.3;
        }

        return BASE_RATE_PER_KWH * multiplier;
    }
}