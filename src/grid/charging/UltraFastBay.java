package grid.charging;

import grid.tariff.PeakSurchargeTariffStrategy;

class UltraFastBay extends ChargingBay {
    public UltraFastBay(String bayId) {
        super(bayId, new PeakSurchargeTariffStrategy(1.5, 0.30, 0.75));
    }
    @Override 
    public double getMaximumHardwareOutput() { return 350.0; }
}
