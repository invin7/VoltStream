package com.voltstream.grid;

class FastDCBay extends ChargingBay {
    public FastDCBay(String bayId) { super(bayId); }
    @Override 
    public double getMaximumHardwareOutput() { return 150.0; }
    @Override
    public double getTariffMultiplier() {
        return 1.2;
    }
}
