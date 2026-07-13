package com.voltstream.grid;

class UltraFastBay extends ChargingBay {
    public UltraFastBay(String bayId) { super(bayId); }
    @Override 
    public double getMaximumHardwareOutput() { return 350.0; }
    @Override
    public double getTariffMultiplier() {
        return 1.5;
    }
}

