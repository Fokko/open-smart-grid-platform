/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.ws.smartmetering.infra.jms.messageprocessor;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alliander.osgp.adapter.ws.schema.smartmetering.notification.NotificationType;
import com.alliander.osgp.adapter.ws.smartmetering.application.mapping.MonitoringMapper;
import com.alliander.osgp.adapter.ws.smartmetering.application.services.NotificationService;
import com.alliander.osgp.adapter.ws.smartmetering.domain.repositories.PeriodicMeterDataRepository;
import com.alliander.osgp.domain.core.valueobjects.DeviceFunction;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.PeriodicMeterData;
import com.alliander.osgp.shared.exceptionhandling.OsgpException;
import com.alliander.osgp.shared.infra.jms.Constants;

/**
 * Class for processing smart metering default response messages
 */
@Component("domainSmartMeteringPeriodicMeterReadsResponseMessageProcessor")
public class PeriodicMeterReadsresponseMessageProcessor extends DomainResponseMessageProcessor {
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicMeterReadsresponseMessageProcessor.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PeriodicMeterDataRepository periodicMeterDataRepository;

    @Autowired
    private MonitoringMapper monitoringMapper;

    protected PeriodicMeterReadsresponseMessageProcessor() {
        super(DeviceFunction.REQUEST_PERIODIC_METER_DATA);
    }

    @Override
    public void processMessage(final ObjectMessage objectMessage) throws JMSException {
        LOGGER.debug("Processing smart metering periodic meter data response message");

        String correlationUid = null;
        String messageType = null;
        String organisationIdentification = null;
        String deviceIdentification = null;

        final OsgpException osgpException = null;

        String result = null;
        String message = null;
        NotificationType notificationType = null;
        PeriodicMeterData periodicMeterData = null;

        try {
            correlationUid = objectMessage.getJMSCorrelationID();
            messageType = objectMessage.getJMSType();
            organisationIdentification = objectMessage.getStringProperty(Constants.ORGANISATION_IDENTIFICATION);
            deviceIdentification = objectMessage.getStringProperty(Constants.DEVICE_IDENTIFICATION);

            result = objectMessage.getStringProperty(Constants.RESULT);
            message = objectMessage.getStringProperty(Constants.DESCRIPTION);
            notificationType = NotificationType.valueOf(messageType);

            periodicMeterData = (PeriodicMeterData) objectMessage.getObject();
        } catch (final JMSException e) {
            LOGGER.error("UNRECOVERABLE ERROR, unable to read ObjectMessage instance, giving up.", e);
            LOGGER.debug("correlationUid: {}", correlationUid);
            LOGGER.debug("messageType: {}", messageType);
            LOGGER.debug("organisationIdentification: {}", organisationIdentification);
            LOGGER.debug("deviceIdentification: {}", deviceIdentification);
            LOGGER.debug("osgpException: {}", osgpException);
            return;
        }

        try {
            LOGGER.info("Calling application service function to handle response: {}", messageType);

            // convert and Save the meterdata

            this.periodicMeterDataRepository.save(this.monitoringMapper.map(periodicMeterData,
                    com.alliander.osgp.adapter.ws.smartmetering.domain.entities.PeriodicMeterData.class));

            this.notificationService.sendNotification(organisationIdentification, deviceIdentification, result,
                    correlationUid, message, notificationType);

            // this.monitoringService.handlePeriodicMeterDataresponse(deviceIdentification,
            // organisationIdentification,
            // correlationUid, messageType, responseMessageResultType,
            // osgpException, periodicMeterData);

        } catch (final Exception e) {
            this.handleError(e, correlationUid, organisationIdentification, deviceIdentification, messageType);
        }
    }
}
