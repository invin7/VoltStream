package grid.charging;

import grid.tariff.PeakSurchargeTariffStrategy;

class FastDCBay extends ChargingBay {
    public FastDCBay(String bayId) {
        super(bayId, new PeakSurchargeTariffStrategy(1.2, 0.30, PEAK_LOAD_THRESHOLD));
    }
    @Override 
    public double getMaximumHardwareOutput() { return 150.0; }
    @Override
    public BayType getBayType() { return BayType.FAST_DC; }
}
