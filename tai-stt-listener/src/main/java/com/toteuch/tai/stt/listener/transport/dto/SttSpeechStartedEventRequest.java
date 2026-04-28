// SPDX-License-Identifier: GPL-3.0-only
package com.toteuch.tai.stt.listener.transport.dto;

public class SttSpeechStartedEventRequest extends AbstractTransportEventRequest {
    private Double averageEnergy;
    private Double peakEnergy;

    public Double getAverageEnergy() {
        return averageEnergy;
    }

    public void setAverageEnergy(Double averageEnergy) {
        this.averageEnergy = averageEnergy;
    }

    public Double getPeakEnergy() {
        return peakEnergy;
    }

    public void setPeakEnergy(Double peakEnergy) {
        this.peakEnergy = peakEnergy;
    }
}
