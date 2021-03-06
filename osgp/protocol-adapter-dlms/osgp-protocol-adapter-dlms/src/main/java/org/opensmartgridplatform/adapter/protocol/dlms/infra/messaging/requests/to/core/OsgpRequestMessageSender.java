/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.protocol.dlms.infra.messaging.requests.to.core;

import javax.jms.ObjectMessage;

import org.opensmartgridplatform.shared.infra.jms.Constants;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.opensmartgridplatform.shared.infra.jms.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;

public class OsgpRequestMessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgpRequestMessageSender.class);

    @Autowired
    @Qualifier("osgpRequestsJmsTemplate")
    private JmsTemplate osgpRequestsJmsTemplate;

    public void send(final RequestMessage requestMessage, final String messageType,
            final MessageMetadata messageMetadata) {
        LOGGER.info("Sending request message to OSGP.");

        this.osgpRequestsJmsTemplate.send(session -> {
            final ObjectMessage objectMessage = session.createObjectMessage(requestMessage);
            objectMessage.setJMSCorrelationID(requestMessage.getCorrelationUid());
            objectMessage.setJMSType(messageType);
            objectMessage.setStringProperty(Constants.ORGANISATION_IDENTIFICATION,
                    requestMessage.getOrganisationIdentification());
            objectMessage.setStringProperty(Constants.DEVICE_IDENTIFICATION, requestMessage.getDeviceIdentification());
            if (messageMetadata != null) {
                objectMessage.setStringProperty(Constants.DOMAIN, messageMetadata.getDomain());
                objectMessage.setStringProperty(Constants.DOMAIN_VERSION, messageMetadata.getDomainVersion());
                objectMessage.setStringProperty(Constants.IP_ADDRESS, messageMetadata.getIpAddress());
                objectMessage.setBooleanProperty(Constants.IS_SCHEDULED, messageMetadata.isScheduled());
                objectMessage.setIntProperty(Constants.RETRY_COUNT, messageMetadata.getRetryCount());
                objectMessage.setBooleanProperty(Constants.BYPASS_RETRY, messageMetadata.isBypassRetry());
            }
            return objectMessage;
        });
    }

}
