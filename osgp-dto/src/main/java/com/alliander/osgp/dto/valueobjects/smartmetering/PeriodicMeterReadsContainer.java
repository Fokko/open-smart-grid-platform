/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.dto.valueobjects.smartmetering;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class PeriodicMeterReadsContainer implements Serializable {

    private static final long serialVersionUID = -156966569210717654L;

    private final String deviceIdentification;
    private final List<PeriodicMeterReads> periodicMeterReads;

    public PeriodicMeterReadsContainer(String deviceIdentification, List<PeriodicMeterReads> periodicMeterReads) {
        this.deviceIdentification = deviceIdentification;
        this.periodicMeterReads = Collections.unmodifiableList(periodicMeterReads);
    }

    public String getDeviceIdentification() {
        return this.deviceIdentification;
    }

    public List<PeriodicMeterReads> getPeriodicMeterReads() {
        return periodicMeterReads;
    }

}