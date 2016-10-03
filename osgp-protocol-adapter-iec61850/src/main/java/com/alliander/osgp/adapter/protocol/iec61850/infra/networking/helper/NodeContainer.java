/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.openmuc.openiec61850.BdaBoolean;
import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.BdaInt16;
import org.openmuc.openiec61850.BdaInt16U;
import org.openmuc.openiec61850.BdaInt32;
import org.openmuc.openiec61850.BdaInt64;
import org.openmuc.openiec61850.BdaInt8;
import org.openmuc.openiec61850.BdaInt8U;
import org.openmuc.openiec61850.BdaTimestamp;
import org.openmuc.openiec61850.BdaVisibleString;
import org.openmuc.openiec61850.FcModelNode;
import org.openmuc.openiec61850.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alliander.osgp.adapter.protocol.iec61850.exceptions.NodeWriteException;

public class NodeContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeContainer.class);

    protected final String deviceIdentification;
    protected final DeviceConnection connection;
    protected final FcModelNode parent;

    public NodeContainer(final DeviceConnection connection, final FcModelNode fcmodelNode) {
        Objects.requireNonNull(connection, "connection must not be null");
        Objects.requireNonNull(fcmodelNode, "fcmodelNode must not be null");
        this.deviceIdentification = connection.getDeviceIdentification();
        this.connection = connection;
        this.parent = fcmodelNode;
    }

    public NodeContainer(final String deviceIdentification, final FcModelNode fcmodelNode) {
        this.deviceIdentification = deviceIdentification;
        this.connection = null;
        this.parent = fcmodelNode;
    }

    /**
     * Write the {@link FcModelNode} which is stored in the
     * {@link NodeContainer#parent} field.
     *
     * @throws NodeWriteException
     *             In case the write action fails {@link ServiceError} or
     *             {@link IOException} is thrown by OpenMUC OpenIEC61850.
     *             {@link NodeWriteException} wraps the thrown exception and
     *             indicates if the connection with the IED is still usable. See
     *             {@link ConnectionState}.
     */
    public void write() throws NodeWriteException {
        this.writeNode(this.parent);
    }

    /**
     * Returns a String for {@link BdaVisibleString} values
     */
    public String getString(final SubDataAttribute child) {
        final BdaVisibleString bdaString = (BdaVisibleString) this.parent.getChild(child.getDescription());

        if (bdaString == null) {
            LOGGER.error("BdaVisibleString is null, most likely attribute: {} does not exist");
        }

        LOGGER.info("device: {}, {} has value {}", this.deviceIdentification, child.getDescription(),
                bdaString.getStringValue());
        return bdaString.getStringValue();
    }

    /**
     * Writes a String value to the given child on the device
     */
    public void writeString(final SubDataAttribute child, final String value) throws NodeWriteException {
        final BdaVisibleString stringNode = (BdaVisibleString) this.parent.getChild(child.getDescription());

        LOGGER.info("device: {}, writing {} to {}", this.deviceIdentification, value, child.getDescription());

        stringNode.setValue(value);
        this.writeNode(stringNode);
    }

    /**
     * Returns a {@link Date} for {@link BdaTimestamp} values
     */
    public Date getDate(final SubDataAttribute child) {
        final BdaTimestamp dBdaTimestamp = (BdaTimestamp) this.parent.getChild(child.getDescription());

        if (dBdaTimestamp == null) {
            LOGGER.error("BdaTimeStamp is null, most likely attribute: {} does not exist");
        }

        LOGGER.info("device: {}, {} has value {}", this.deviceIdentification, child.getDescription(),
                dBdaTimestamp.getDate());
        return dBdaTimestamp.getDate();
    }

    /**
     * Writes a Date value to the given child on the device
     */
    public void writeDate(final SubDataAttribute child, final Date value) throws NodeWriteException {
        final BdaTimestamp dBdaTimestamp = (BdaTimestamp) this.parent.getChild(child.getDescription());

        LOGGER.info("device: {}, writing {} to {}", this.deviceIdentification, value, child.getDescription());
        dBdaTimestamp.setDate(value);
        this.writeNode(dBdaTimestamp);
    }

    public BdaBoolean getBoolean(final SubDataAttribute child) {
        return (BdaBoolean) this.parent.getChild(child.getDescription());
    }

    public void writeBoolean(final SubDataAttribute child, final boolean value) throws NodeWriteException {
        final BdaBoolean bdaBoolean = (BdaBoolean) this.parent.getChild(child.getDescription());
        bdaBoolean.setValue(value);
        this.writeNode(bdaBoolean);
    }

    public BdaInt8 getByte(final SubDataAttribute child) {
        return (BdaInt8) this.parent.getChild(child.getDescription());
    }

    public void writeByte(final SubDataAttribute child, final byte value) throws NodeWriteException {
        final BdaInt8 bdaByte = (BdaInt8) this.parent.getChild(child.getDescription());
        bdaByte.setValue(value);
        this.writeNode(bdaByte);
    }

    public BdaInt8U getUnsignedByte(final SubDataAttribute child) {
        return (BdaInt8U) this.parent.getChild(child.getDescription());
    }

    public BdaInt16 getShort(final SubDataAttribute child) {
        return (BdaInt16) this.parent.getChild(child.getDescription());
    }

    public void writeShort(final SubDataAttribute child, final Short value) throws NodeWriteException {
        final BdaInt16 bdaShort = (BdaInt16) this.parent.getChild(child.getDescription());
        bdaShort.setValue(value);
        this.writeNode(bdaShort);
    }

    public BdaInt16U getUnsignedShort(final SubDataAttribute child) {
        return (BdaInt16U) this.parent.getChild(child.getDescription());
    }

    public void writeUnsignedShort(final SubDataAttribute child, final Integer value) throws NodeWriteException {
        final BdaInt16U bdaUnsignedShort = (BdaInt16U) this.parent.getChild(child.getDescription());
        bdaUnsignedShort.setValue(value);
        this.writeNode(bdaUnsignedShort);
    }

    public BdaInt32 getInteger(final SubDataAttribute child) {
        return (BdaInt32) this.parent.getChild(child.getDescription());
    }

    public void writeInteger(final SubDataAttribute child, final Integer value) throws NodeWriteException {
        final BdaInt32 bdaInteger = (BdaInt32) this.parent.getChild(child.getDescription());
        bdaInteger.setValue(value);
        this.writeNode(bdaInteger);
    }

    public BdaInt64 getLong(final SubDataAttribute child) {
        return (BdaInt64) this.parent.getChild(child.getDescription());
    }

    public void writeLong(final SubDataAttribute child, final Integer value) throws NodeWriteException {
        final BdaInt64 bdaInteger = (BdaInt64) this.parent.getChild(child.getDescription());
        bdaInteger.setValue(value);
        this.writeNode(bdaInteger);
    }

    public BdaFloat32 getFloat(final SubDataAttribute child) {
        return (BdaFloat32) this.parent.getChild(child.getDescription());
    }

    public void writeFloat(final SubDataAttribute child, final Float value) throws NodeWriteException {
        final BdaFloat32 bdaFloat = (BdaFloat32) this.parent.getChild(child.getDescription());
        bdaFloat.setFloat(value);
        this.writeNode(bdaFloat);
    }

    /**
     * Writes the new data of the node to the device.
     */
    private void writeNode(final FcModelNode node) throws NodeWriteException {
        try {
            this.connection.getConnection().getClientAssociation().setDataValues(node);
        } catch (final ServiceError e) {
            LOGGER.error("ServiceError during writeNode()", e);
            throw new NodeWriteException(e.getMessage(), e, ConnectionState.OK);
        } catch (final IOException e) {
            // "if a fatal association error occurs. The association object will
            // be closed and can no longer be used after this exception is
            // thrown."
            LOGGER.error("IOException during writeNode()", e);
            throw new NodeWriteException(e.getMessage(), e, ConnectionState.BROKEN);
        }
    }

    /**
     * Get the {@link FcModelNode} instance.
     *
     * @return The {@link FcModelNode} instance.
     */
    public FcModelNode getFcmodelNode() {
        return this.parent;
    }

    /**
     * Return a child or sub-data-attribute for this node.
     *
     * @param child
     *            The name of the child to fetch.
     */
    public NodeContainer getChild(final SubDataAttribute child) {
        return new NodeContainer(this.connection, (FcModelNode) this.parent.getChild(child.getDescription()));
    }

    /**
     * Return a child or sub-data-attribute for this node.
     *
     * @param child
     *            The name of the child to fetch.
     */
    public NodeContainer getChild(final String child) {
        return new NodeContainer(this.connection, (FcModelNode) this.parent.getChild(child));
    }

    @Override
    public String toString() {
        if (this.parent == null) {
            return "";
        }

        return this.parent.toString();
    }
}
