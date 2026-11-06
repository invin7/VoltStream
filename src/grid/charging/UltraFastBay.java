package grid.charging;

import grid.tariff.PeakSurchargeTariffStrategy;

class UltraFastBay extends ChargingBay {
    public UltraFastBay(String bayId) {
        super(bayId, new PeakSurchargeTariffStrategy(1.5, 0.30, PEAK_LOAD_THRESHOLD));
    }
    @Override 
    public double getMaximumHardwareOutput() { return 350.0; }
    @Override
    public BayType getBayType() { return BayType.ULTRA_FAST; }
}
