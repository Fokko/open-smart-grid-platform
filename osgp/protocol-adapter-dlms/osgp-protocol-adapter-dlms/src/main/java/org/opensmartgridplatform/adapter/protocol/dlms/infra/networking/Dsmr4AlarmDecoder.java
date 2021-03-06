/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.protocol.dlms.infra.networking;

import static org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.DlmsObjectType.EXTERNAL_TRIGGER_CSD;
import static org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.DlmsObjectType.EXTERNAL_TRIGGER_SMS;
import static org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.DlmsObjectType.PUSH_SCHEDULER;
import static org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.DlmsObjectType.PUSH_SETUP_SCHEDULER;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.DlmsObjectConfigDsmr422;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.opensmartgridplatform.dlms.DlmsPushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dsmr4AlarmDecoder extends AlarmDecoder {

    private DlmsObjectConfigDsmr422 dlmsObjectConfigDsmr422 = new DlmsObjectConfigDsmr422();

    private static final Logger LOGGER = LoggerFactory.getLogger(Dsmr4AlarmDecoder.class);

    /**
     * The elements inside the DSMR4 DLMS Push notification (Alarm or Wakeup SMS are
     * expressed in bytes separated by a comma (byte 0x2C).
     */
    private static final byte COMMA = 0x2C;

    private DlmsPushNotification.Builder builder = new DlmsPushNotification.Builder();

    public DlmsPushNotification decodeDsmr4alarm(final ChannelBuffer buffer) throws UnrecognizedMessageDataException {

        this.decodeEquipmentIdentifier(buffer);
        this.decodeReceivedData(buffer);
        return builder.build();
    }

    private void decodeEquipmentIdentifier(final ChannelBuffer buffer) throws UnrecognizedMessageDataException {

        final byte[] equipmentIdentifierPlusSeparatorBytes = read(buffer, EQUIPMENT_IDENTIFIER_LENGTH + 1);

        if (equipmentIdentifierPlusSeparatorBytes[EQUIPMENT_IDENTIFIER_LENGTH] != COMMA) {
            throw new UnrecognizedMessageDataException("message must start with " + EQUIPMENT_IDENTIFIER_LENGTH
                    + " bytes for the equipment identifier, followed by byte 0x2C (a comma).");
        }

        final byte[] equipmentIdentifierBytes = Arrays
                .copyOfRange(equipmentIdentifierPlusSeparatorBytes, 0, EQUIPMENT_IDENTIFIER_LENGTH);
        final String equipmentIdentifier = new String(equipmentIdentifierBytes, StandardCharsets.US_ASCII);
        this.builder.withEquipmentIdentifier(equipmentIdentifier);
        this.builder.appendBytes(equipmentIdentifierPlusSeparatorBytes);
    }

    private void decodeReceivedData(final ChannelBuffer buffer) throws UnrecognizedMessageDataException {
        // SLIM-1711 Is a very weird bug, where readableBytes turns out to be almost MAXINT
        // Seems like BigEndianHeapChannelBuffer has some kind of overflow/underflow.
        final int readableBytes = buffer.writerIndex() - buffer.readerIndex();
        if (readableBytes > Math.max(NUMBER_OF_BYTES_FOR_ALARM, NUMBER_OF_BYTES_FOR_LOGICAL_NAME)) {
            throw new UnrecognizedMessageDataException(
                    "length of data bytes is not " + NUMBER_OF_BYTES_FOR_ALARM + " (alarm) or "
                            + NUMBER_OF_BYTES_FOR_LOGICAL_NAME + " (obiscode)");
        }

        if (readableBytes == NUMBER_OF_BYTES_FOR_ALARM) {
            this.decodeAlarmRegisterData(buffer, builder);
        } else if (readableBytes == NUMBER_OF_BYTES_FOR_LOGICAL_NAME) {
            this.decodeObisCodeData(buffer);
        } else {
            throw new UnrecognizedMessageDataException("Incorrect amount of bytes: " + readableBytes + ". Expected " +
                    NUMBER_OF_BYTES_FOR_ALARM + " or " + NUMBER_OF_BYTES_FOR_LOGICAL_NAME);
        }
    }

    private void decodeObisCodeData(final ChannelBuffer buffer) throws UnrecognizedMessageDataException {

        final byte[] logicalNameBytes = read(buffer, NUMBER_OF_BYTES_FOR_LOGICAL_NAME);

        try {
            if (isLogicalNameSmsTrigger(logicalNameBytes)) {
                this.builder.withTriggerType(PUSH_SMS_TRIGGER);
            } else if (isLogicalNameCsdTrigger(logicalNameBytes)) {
                LOGGER.warn("CSD Push notification not supported");
                this.builder.withTriggerType(PUSH_CSD_TRIGGER);
            } else if (isLogicalNameSchedulerTrigger(logicalNameBytes)) {
                LOGGER.warn("Scheduler Push notification not supported");
                this.builder.withTriggerType(PUSH_SCHEDULER_TRIGGER);
            } else {
                LOGGER.warn("Unknown Push notification not supported. Unable to decode");
                this.builder.withTriggerType("");
            }
        } catch (ProtocolAdapterException e) {
            throw new UnrecognizedMessageDataException("Error decoding logical name", e);
        }

        this.builder.withAlarms(null);
        this.builder.appendBytes(logicalNameBytes);
    }

    private boolean isLogicalNameSmsTrigger(byte[] logicalNameBytes) throws ProtocolAdapterException {
        // DSMR4 has specific objects for the different external trigger types for SMS and CSD
        return Arrays.equals(dlmsObjectConfigDsmr422.getObisForObject(EXTERNAL_TRIGGER_SMS).bytes(), logicalNameBytes);
    }

    private boolean isLogicalNameCsdTrigger(byte[] logicalNameBytes) throws ProtocolAdapterException {
        // DSMR4 has specific objects for the different external trigger types for SMS and CSD
        return Arrays.equals(dlmsObjectConfigDsmr422.getObisForObject(EXTERNAL_TRIGGER_CSD).bytes(), logicalNameBytes);
    }

    private boolean isLogicalNameSchedulerTrigger(byte[] logicalNameBytes) throws ProtocolAdapterException {
        return Arrays.equals(dlmsObjectConfigDsmr422.getObisForObject(PUSH_SCHEDULER).bytes(), logicalNameBytes) ||
                Arrays.equals(dlmsObjectConfigDsmr422.getObisForObject(PUSH_SETUP_SCHEDULER).bytes(), logicalNameBytes);
    }
}
