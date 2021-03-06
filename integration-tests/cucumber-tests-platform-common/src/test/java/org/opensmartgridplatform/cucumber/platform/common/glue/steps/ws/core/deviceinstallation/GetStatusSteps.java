/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.cucumber.platform.common.glue.steps.ws.core.deviceinstallation;

import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getEnum;
import static org.opensmartgridplatform.cucumber.core.ReadSettingsHelper.getString;
import static org.opensmartgridplatform.cucumber.platform.core.CorrelationUidHelper.saveCorrelationUidInScenarioContext;

import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.soap.client.SoapFaultClientException;

import org.opensmartgridplatform.adapter.ws.schema.core.common.AsyncRequest;
import org.opensmartgridplatform.adapter.ws.schema.core.common.OsgpResultType;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.DeviceStatus;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.GetStatusAsyncRequest;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.GetStatusAsyncResponse;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.GetStatusRequest;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.GetStatusResponse;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.LightType;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.LightValue;
import org.opensmartgridplatform.adapter.ws.schema.core.deviceinstallation.LinkType;
import org.opensmartgridplatform.adapter.ws.schema.core.devicemanagement.EventNotificationType;
import org.opensmartgridplatform.cucumber.core.GlueBase;
import org.opensmartgridplatform.cucumber.core.ScenarioContext;
import org.opensmartgridplatform.cucumber.core.Wait;
import org.opensmartgridplatform.cucumber.platform.PlatformDefaults;
import org.opensmartgridplatform.cucumber.platform.PlatformKeys;
import org.opensmartgridplatform.cucumber.platform.common.support.ws.core.CoreDeviceInstallationClient;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class GetStatusSteps extends GlueBase {

    @Autowired
    private CoreDeviceInstallationClient client;

    @When("receiving a device installation get status request")
    public void receivingADeviceInstallationGetStatusRequest(final Map<String, String> settings) throws Throwable {
        final GetStatusRequest request = new GetStatusRequest();

        request.setDeviceIdentification(getString(settings, PlatformKeys.KEY_DEVICE_IDENTIFICATION,
                PlatformDefaults.DEFAULT_DEVICE_IDENTIFICATION));

        try {
            ScenarioContext.current().put(PlatformKeys.RESPONSE, this.client.getStatus(request));
        } catch (final SoapFaultClientException ex) {
            ScenarioContext.current().put(PlatformKeys.RESPONSE, ex);
        }
    }

    @Then("the device installation get status async response contains")
    public void theDeviceInstallationGetStatusAsyncResponseContains(final Map<String, String> expectedResponseData)
            throws Throwable {
        final GetStatusAsyncResponse asyncResponse = (GetStatusAsyncResponse) ScenarioContext.current()
                .get(PlatformKeys.RESPONSE);

        Assert.assertNotNull(asyncResponse.getAsyncResponse().getCorrelationUid());
        Assert.assertEquals(getString(expectedResponseData, PlatformKeys.KEY_DEVICE_IDENTIFICATION),
                asyncResponse.getAsyncResponse().getDeviceId());

        saveCorrelationUidInScenarioContext(asyncResponse.getAsyncResponse().getCorrelationUid(),
                getString(expectedResponseData, PlatformKeys.KEY_ORGANIZATION_IDENTIFICATION,
                        PlatformDefaults.DEFAULT_ORGANIZATION_IDENTIFICATION));
    }

    @Then("the platform buffers a device installation get status response message for device \"([^\"]*)\"")
    public void thePlatformBuffersADeviceInstallationGetStatusResponseMessageForDevice(
            final String deviceIdentification, final Map<String, String> expectedResult) throws Throwable {
        final GetStatusAsyncRequest request = new GetStatusAsyncRequest();
        final AsyncRequest asyncRequest = new AsyncRequest();
        asyncRequest.setDeviceId(deviceIdentification);
        asyncRequest.setCorrelationUid((String) ScenarioContext.current().get(PlatformKeys.KEY_CORRELATION_UID));
        request.setAsyncRequest(asyncRequest);

        final GetStatusResponse response = Wait.untilAndReturn(() -> {
            final GetStatusResponse retval = this.client.getStatusResponse(request);
            Assert.assertNotNull(retval);
            Assert.assertEquals(Enum.valueOf(OsgpResultType.class, expectedResult.get(PlatformKeys.KEY_RESULT)),
                    retval.getResult());
            return retval;
        });

        final DeviceStatus deviceStatus = response.getDeviceStatus();

        Assert.assertEquals(getEnum(expectedResult, PlatformKeys.KEY_PREFERRED_LINKTYPE, LinkType.class),
                deviceStatus.getPreferredLinkType());
        Assert.assertEquals(getEnum(expectedResult, PlatformKeys.KEY_ACTUAL_LINKTYPE, LinkType.class),
                deviceStatus.getActualLinkType());
        Assert.assertEquals(getEnum(expectedResult, PlatformKeys.KEY_LIGHTTYPE, LightType.class),
                deviceStatus.getLightType());

        if (expectedResult.containsKey(PlatformKeys.KEY_EVENTNOTIFICATIONTYPES)
                && !expectedResult.get(PlatformKeys.KEY_EVENTNOTIFICATIONTYPES).isEmpty()) {
            Assert.assertEquals(
                    getString(expectedResult, PlatformKeys.KEY_EVENTNOTIFICATIONS,
                            PlatformDefaults.DEFAULT_EVENTNOTIFICATIONS).split(PlatformKeys.SEPARATOR_COMMA).length,
                    deviceStatus.getEventNotifications().size());
            for (final String eventNotification : getString(expectedResult, PlatformKeys.KEY_EVENTNOTIFICATIONS,
                    PlatformDefaults.DEFAULT_EVENTNOTIFICATIONS).split(PlatformKeys.SEPARATOR_COMMA)) {
                Assert.assertTrue(deviceStatus.getEventNotifications()
                        .contains(Enum.valueOf(EventNotificationType.class, eventNotification)));
            }
        }

        if (expectedResult.containsKey(PlatformKeys.KEY_LIGHTVALUES)
                && !expectedResult.get(PlatformKeys.KEY_LIGHTVALUES).isEmpty()) {
            Assert.assertEquals(
                    getString(expectedResult, PlatformKeys.KEY_LIGHTVALUES, PlatformDefaults.DEFAULT_LIGHTVALUES)
                            .split(PlatformKeys.SEPARATOR_COMMA).length,
                    deviceStatus.getLightValues().size());
            for (final String lightValues : getString(expectedResult, PlatformKeys.KEY_LIGHTVALUES,
                    PlatformDefaults.DEFAULT_LIGHTVALUES).split(PlatformKeys.SEPARATOR_COMMA)) {

                final String[] parts = lightValues.split(PlatformKeys.SEPARATOR_SEMICOLON);
                final Integer index = Integer.parseInt(parts[0]);
                final Boolean on = Boolean.parseBoolean(parts[1]);
                final Integer dimValue = Integer.parseInt(parts[2]);

                boolean found = false;
                for (final LightValue lightValue : deviceStatus.getLightValues()) {

                    if (Objects.equals(lightValue.getIndex(), index) && lightValue.isOn() == on
                            && Objects.equals(lightValue.getDimValue(), dimValue)) {
                        found = true;
                        break;
                    }
                }

                Assert.assertTrue(found);
            }
        }
    }
}
