/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.cucumber.platform.common.glue.steps.ws.core.deviceinstallation;

import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getBoolean;
import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getInteger;
import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getString;

import java.util.Map;

import org.junit.Assert;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.Device;
import org.opensmartgridplatform.cucumber.platform.PlatformKeys;

public class DeviceSteps {

    private static Map<String, String> localExpectedDevice;

    private static void checkAndAssert(final String key, final Object actualValue) {
        if (localExpectedDevice.containsKey(key)) {

            Object expectedObj = null;

            if (actualValue instanceof String) {
                expectedObj = getString(localExpectedDevice, key);
            } else if (actualValue instanceof Integer) {
                expectedObj = getInteger(localExpectedDevice, key);
            } else if (actualValue instanceof Boolean) {
                expectedObj = getBoolean(localExpectedDevice, key);
            }

            if (expectedObj != null) {
                Assert.assertEquals(expectedObj, actualValue);
            }
        }
    }

    public static void checkDevice(final Map<String, String> expectedDevice, final Device actualDevice) {
        localExpectedDevice = expectedDevice;

        checkAndAssert(PlatformKeys.KEY_DEVICE_IDENTIFICATION, actualDevice.getDeviceIdentification());
        checkAndAssert(PlatformKeys.ALIAS, actualDevice.getAlias());
        checkAndAssert(PlatformKeys.KEY_CITY, actualDevice.getContainerAddress().getCity());
        checkAndAssert(PlatformKeys.KEY_MUNICIPALITY, actualDevice.getContainerAddress().getMunicipality());
        checkAndAssert(PlatformKeys.KEY_NUMBER, actualDevice.getContainerAddress().getNumber());
        checkAndAssert(PlatformKeys.KEY_POSTCODE, actualDevice.getContainerAddress().getPostalCode());
        checkAndAssert(PlatformKeys.KEY_STREET, actualDevice.getContainerAddress().getStreet());
        checkAndAssert(PlatformKeys.KEY_DEVICE_UID, actualDevice.getDeviceUid());
        checkAndAssert(PlatformKeys.KEY_LATITUDE, actualDevice.getGpsLatitude());
        checkAndAssert(PlatformKeys.KEY_LONGITUDE, actualDevice.getGpsLongitude());
        checkAndAssert(PlatformKeys.KEY_OWNER, actualDevice.getOwner());
        checkAndAssert(PlatformKeys.KEY_HAS_SCHEDULE, actualDevice.isHasSchedule());

    }

    public static void checkDeviceOld(final Map<String, String> expectedDevice, final Device actualDevice) {
        if (expectedDevice.containsKey(PlatformKeys.KEY_DEVICE_IDENTIFICATION)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_DEVICE_IDENTIFICATION),
                    actualDevice.getDeviceIdentification());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_ORGANIZATION_IDENTIFICATION)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_ORGANIZATION_IDENTIFICATION),
                    actualDevice.getDeviceIdentification());
        }

        if (expectedDevice.containsKey(PlatformKeys.ALIAS)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.ALIAS), actualDevice.getAlias());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_CITY)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_CITY),
                    actualDevice.getContainerAddress().getCity());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_MUNICIPALITY)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_MUNICIPALITY),
                    actualDevice.getContainerAddress().getMunicipality());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_NUMBER)) {
            Assert.assertEquals(getInteger(expectedDevice, PlatformKeys.KEY_NUMBER),
                    actualDevice.getContainerAddress().getNumber());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_NUMBER_ADDITION)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_NUMBER_ADDITION),
                    actualDevice.getContainerAddress().getNumberAddition());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_POSTCODE)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_POSTCODE),
                    actualDevice.getContainerAddress().getPostalCode());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_STREET)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_STREET),
                    actualDevice.getContainerAddress().getStreet());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_DEVICE_UID)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_DEVICE_UID), actualDevice.getDeviceUid());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_LATITUDE)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_LATITUDE), actualDevice.getGpsLatitude());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_LONGITUDE)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_LONGITUDE), actualDevice.getGpsLongitude());
        }

        if (expectedDevice.containsKey(PlatformKeys.KEY_OWNER)) {
            Assert.assertEquals(getString(expectedDevice, PlatformKeys.KEY_OWNER), actualDevice.getOwner());
        }
    }
}
