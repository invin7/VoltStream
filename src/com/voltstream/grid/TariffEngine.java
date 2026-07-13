package com.voltstream.grid;

public class TariffEngine {

    private static final double BASE_RATE_PER_KWH = 8.50;
    private static final double PEAK_LOAD_SURCHARGE = 0.30;

    public static double calculateCurrentTariff(ChargingBay bay, double gridLoadFactor) {

        double multiplier = bay.getTariffMultiplier();

        if (gridLoadFactor > 0.75) {
            multiplier += PEAK_LOAD_SURCHARGE;
        }

        return BASE_RATE_PER_KWH * multiplier;
    }
}