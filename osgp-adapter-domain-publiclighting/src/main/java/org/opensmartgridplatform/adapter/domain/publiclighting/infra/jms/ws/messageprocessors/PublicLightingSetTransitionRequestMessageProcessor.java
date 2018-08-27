/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.domain.publiclighting.infra.jms.ws.messageprocessors;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.opensmartgridplatform.adapter.domain.publiclighting.application.services.SetTransitionService;
import org.opensmartgridplatform.adapter.domain.publiclighting.infra.jms.ws.WebServiceRequestMessageProcessor;
import org.opensmartgridplatform.domain.core.valueobjects.DeviceFunction;
import org.opensmartgridplatform.domain.core.valueobjects.TransitionMessageDataContainer;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for processing public lighting set transition request messages
 */
@Component("domainPublicLightingSetTransitionRequestMessageProcessor")
public class PublicLightingSetTransitionRequestMessageProcessor extends WebServiceRequestMessageProcessor {
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PublicLightingSetTransitionRequestMessageProcessor.class);

    @Autowired
    private SetTransitionService setTransitionService;

    public PublicLightingSetTransitionRequestMessageProcessor() {
        super(DeviceFunction.SET_TRANSITION);
    }

    @Override
    public void processMessage(final ObjectMessage message) {
        LOGGER.debug("Processing public lighting set transition request message");

        final MessageMetadata metadata;
        Object dataObject;

        try {
            metadata = MessageMetadata.fromMessage(message);
            dataObject = message.getObject();
        } catch (final JMSException e) {
            LOGGER.error("UNRECOVERABLE ERROR, unable to read ObjectMessage instance, giving up.", e);
            return;
        }

        try {
            LOGGER.info("Calling application service function: {}", metadata.getMessageType());

            final TransitionMessageDataContainer transitionMessageDataContainer = (TransitionMessageDataContainer) dataObject;

            this.setTransitionService.setTransitionForDevice(metadata,
                    transitionMessageDataContainer.getTransitionType(), transitionMessageDataContainer.getDateTime());
        } catch (final Exception e) {
            this.handleError(e, metadata.getCorrelationUid(), metadata.getOrganisationIdentification(),
                    metadata.getDeviceIdentification(), metadata.getMessageType(), metadata.getMessagePriority());
        }
    }
}
