/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.platform.dlms.cucumber.steps.database.device;

import static com.alliander.osgp.platform.cucumber.steps.Defaults.SMART_METER_E;
import static com.alliander.osgp.platform.cucumber.steps.Defaults.SMART_METER_G;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.osgp.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.osgp.adapter.protocol.dlms.domain.entities.SecurityKey;
import org.osgp.adapter.protocol.dlms.domain.entities.SecurityKeyType;
import org.osgp.adapter.protocol.dlms.domain.repositories.DlmsDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alliander.osgp.domain.core.entities.Device;
import com.alliander.osgp.domain.core.entities.DeviceAuthorization;
import com.alliander.osgp.domain.core.entities.Organisation;
import com.alliander.osgp.domain.core.entities.ProtocolInfo;
import com.alliander.osgp.domain.core.entities.SmartMeter;
import com.alliander.osgp.domain.core.repositories.DeviceAuthorizationRepository;
import com.alliander.osgp.domain.core.repositories.DeviceRepository;
import com.alliander.osgp.domain.core.repositories.OrganisationRepository;
import com.alliander.osgp.domain.core.repositories.ProtocolInfoRepository;
import com.alliander.osgp.domain.core.repositories.SmartMeterRepository;
import com.alliander.osgp.domain.core.valueobjects.DeviceFunctionGroup;
import com.alliander.osgp.platform.cucumber.core.ScenarioContext;
import com.alliander.osgp.platform.dlms.cucumber.builders.entities.DeviceBuilder;
import com.alliander.osgp.platform.dlms.cucumber.builders.entities.DlmsDeviceBuilder;
import com.alliander.osgp.platform.dlms.cucumber.builders.entities.SmartMeterBuilder;
import com.alliander.osgp.platform.dlms.cucumber.steps.Defaults;
import com.alliander.osgp.platform.dlms.cucumber.steps.Keys;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

/**
 * DLMS device specific steps.
 */
@Transactional(value = "txMgrCore")
public class DlmsDeviceSteps {

    @Autowired
    private SmartMeterRepository smartMeterRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DlmsDeviceRepository dlmsDeviceRepository;

    @Autowired
    private ProtocolInfoRepository protocolInfoRepository;

    @Autowired
    private OrganisationRepository organisationRepo;

    @Autowired
    private DeviceAuthorizationRepository deviceAuthorizationRepository;

    @Given("^a dlms device$")
    public void aDlmsDevice(final Map<String, String> inputSettings) throws Throwable {

        final Device device = this.createDeviceInCoreDatabase(inputSettings);
        this.setScenarioContextForDevice(inputSettings, device);

        this.createDeviceAuthorisationInCoreDatabase(device);

        this.createDlmsDeviceInProtocolAdapterDatabase(inputSettings);
    }

    @Then("^the dlms device with identification \"([^\"]*)\" exists$")
    public void theDlmsDeviceWithIdentificationExists(final String deviceIdentification) throws Throwable {

        final Device device = this.deviceRepository.findByDeviceIdentification(deviceIdentification);
        assertNotNull("DLMS device with identification " + deviceIdentification + " in core database", device);

        final DlmsDevice dlmsDevice = this.dlmsDeviceRepository.findByDeviceIdentification(deviceIdentification);
        assertNotNull("DLMS device with identification " + deviceIdentification + " in protocol database", dlmsDevice);
    }

    @Then("^the dlms device with identification \"([^\"]*)\" does not exist$")
    public void theDlmsDeviceWithIdentificationDoesNotExist(final String deviceIdentification) throws Throwable {

        final DlmsDevice dlmsDevice = this.dlmsDeviceRepository.findByDeviceIdentification(deviceIdentification);
        assertNull("DLMS device with identification " + deviceIdentification + " in protocol database", dlmsDevice);

        final Device device = this.deviceRepository.findByDeviceIdentification(deviceIdentification);
        assertNull("DLMS device with identification " + deviceIdentification + " in core database", device);
    }

    @Then("^the stored keys are not equal to the received keys$")
    public void theStoredKeysAreNotEqualToTheReceivedKeys() throws Throwable {
        final String keyDeviceIdentification = Keys.DEVICE_IDENTIFICATION;
        final String deviceIdentification = (String) ScenarioContext.Current().get(keyDeviceIdentification);
        assertNotNull("Device identification must be in the scenario context for key " + keyDeviceIdentification,
                deviceIdentification);

        final String deviceDescription = "DLMS device with identification " + deviceIdentification;
        final DlmsDevice dlmsDevice = this.dlmsDeviceRepository.findByDeviceIdentification(deviceIdentification);
        assertNotNull(deviceDescription + " must be in the protocol database", dlmsDevice);

        final SecurityKey masterKey = dlmsDevice.getValidSecurityKey(SecurityKeyType.E_METER_MASTER);
        assertNotNull("Master key for " + deviceDescription + " must be stored", masterKey);

        final String receivedMasterKey = (String) ScenarioContext.Current().get(Keys.KEY_DEVICE_MASTERKEY);
        assertNotEquals("Stored master key for " + deviceDescription + " must be different from received key",
                receivedMasterKey, masterKey.getKey());
        final SecurityKey authenticationKey = dlmsDevice.getValidSecurityKey(SecurityKeyType.E_METER_AUTHENTICATION);
        assertNotNull("Authentication key for " + deviceDescription, authenticationKey);

        final String receivedAuthenticationKey = (String) ScenarioContext.Current()
                .get(Keys.KEY_DEVICE_AUTHENTICATIONKEY);
        assertNotEquals("Stored authentication key for " + deviceDescription + " must be different from received key",
                receivedAuthenticationKey, authenticationKey.getKey());

        final SecurityKey encryptionKey = dlmsDevice.getValidSecurityKey(SecurityKeyType.E_METER_ENCRYPTION);
        assertNotNull("Encryption key for " + deviceDescription, encryptionKey);

        final String receivedEncryptionKey = (String) ScenarioContext.Current().get(Keys.KEY_DEVICE_AUTHENTICATIONKEY);
        assertNotEquals("Stored encryption key for " + deviceDescription + " must be different from received key",
                receivedEncryptionKey, encryptionKey.getKey());
    }

    private void setScenarioContextForDevice(final Map<String, String> inputSettings, final Device device) {
        final String deviceType = inputSettings.get(Keys.DEVICE_TYPE);
        if (this.isGasSmartMeter(deviceType)) {
            ScenarioContext.Current().put(Keys.GAS_DEVICE_IDENTIFICATION, device.getDeviceIdentification());
        } else {
            ScenarioContext.Current().put(Keys.DEVICE_IDENTIFICATION, device.getDeviceIdentification());
        }
    }

    private Device createDeviceInCoreDatabase(final Map<String, String> inputSettings) {
        Device device;
        if (this.isSmartMeter(inputSettings)) {
            final SmartMeter smartMeter = new SmartMeterBuilder().withSettings(inputSettings)
                    .setProtocolInfo(this.getProtocolInfo(inputSettings)).build();
            device = this.smartMeterRepository.save(smartMeter);

        } else {
            device = new DeviceBuilder().withSettings(inputSettings)
                    .setProtocolInfo(this.getProtocolInfo(inputSettings)).build();
            this.deviceRepository.save(device);
        }

        if (inputSettings.containsKey(Keys.GATEWAY_DEVICE_IDENTIFICATION)) {
            final Device gatewayDevice = this.deviceRepository.findByDeviceIdentification(inputSettings
                    .get(Keys.GATEWAY_DEVICE_IDENTIFICATION));
            device.updateGatewayDevice(gatewayDevice);
            device = this.deviceRepository.save(device);
        }
        return device;
    }

    private void createDeviceAuthorisationInCoreDatabase(final Device device) {
        final Organisation organisation = this.organisationRepo
                .findByOrganisationIdentification(Defaults.ORGANISATION_IDENTIFICATION);
        final DeviceAuthorization deviceAuthorization = device
                .addAuthorization(organisation, DeviceFunctionGroup.OWNER);

        this.deviceAuthorizationRepository.save(deviceAuthorization);
        this.deviceRepository.save(device);
    }

    private void createDlmsDeviceInProtocolAdapterDatabase(final Map<String, String> inputSettings) {
        final DlmsDeviceBuilder dlmsDeviceBuilder = new DlmsDeviceBuilder().withSettings(inputSettings);
        if (inputSettings.containsKey(Keys.GATEWAY_DEVICE_IDENTIFICATION)) {
            // MBUS devices dont need these keys.
            dlmsDeviceBuilder.getEncryptionSecurityKeyBuilder().disable();
            dlmsDeviceBuilder.getMasterSecurityKeyBuilder().disable();
            dlmsDeviceBuilder.getAuthenticationSecurityKeyBuilder().disable();
        } else {
            dlmsDeviceBuilder.getMbusEncryptionSecurityKeyBuilder().disable();
            dlmsDeviceBuilder.getMbusMasterSecurityKeyBuilder().disable();
        }

        final DlmsDevice dlmsDevice = dlmsDeviceBuilder.build();
        this.dlmsDeviceRepository.save(dlmsDevice);
    }

    private boolean isSmartMeter(final Map<String, String> settings) {
        final String deviceType = settings.get(Keys.DEVICE_TYPE);
        return this.isGasSmartMeter(deviceType) || this.isESmartMeter(deviceType);
    }

    private boolean isGasSmartMeter(final String deviceType) {
        return SMART_METER_G.equals(deviceType);
    }

    private boolean isESmartMeter(final String deviceType) {
        return SMART_METER_E.equals(deviceType);
    }

    /**
     * ProtocolInfo is fixed system data, inserted by flyway. Therefore the
     * ProtocolInfo instance will be retrieved from the database, and not built.
     *
     * @param inputSettings
     * @return ProtocolInfo
     */
    private ProtocolInfo getProtocolInfo(final Map<String, String> inputSettings) {
        if (inputSettings.containsKey(Keys.PROTOCOL) && inputSettings.containsKey(Keys.PROTOCOL_VERSION)) {
            return this.protocolInfoRepository.findByProtocolAndProtocolVersion(inputSettings.get(Keys.PROTOCOL),
                    inputSettings.get(Keys.PROTOCOL_VERSION));
        } else {
            return this.protocolInfoRepository.findByProtocolAndProtocolVersion(Defaults.PROTOCOL,
                    Defaults.PROTOCOL_VERSION);
        }
    }
}
