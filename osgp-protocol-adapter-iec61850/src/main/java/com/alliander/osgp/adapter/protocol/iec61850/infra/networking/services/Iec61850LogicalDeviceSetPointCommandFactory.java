/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alliander.osgp.adapter.protocol.iec61850.device.logicalDevice.LogicalDeviceWriteCommand;
import com.alliander.osgp.adapter.protocol.iec61850.device.logicalDevice.LogicalDeviceWriteCommandFactory;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.DataAttribute;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850ScheduleCatCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850ScheduleIdCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850ScheduleTypeCommand;
import com.alliander.osgp.dto.valueobjects.microgrids.SetPointDto;

public final class Iec61850LogicalDeviceSetPointCommandFactory implements LogicalDeviceWriteCommandFactory<SetPointDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec61850LogicalDeviceSetPointCommandFactory.class);

    private static final int ID_START = 1;
    private static final int ID_END = 4;

    private static final Map<String, LogicalDeviceWriteCommand<SetPointDto>> RTU_COMMAND_MAP = new HashMap<>();

    static {
        initializeLogicalDeviceCommandMap();
    }

    private static Iec61850LogicalDeviceSetPointCommandFactory instance;

    private Iec61850LogicalDeviceSetPointCommandFactory() {
    }

    public static synchronized Iec61850LogicalDeviceSetPointCommandFactory getInstance() {
        if (instance == null) {
            instance = new Iec61850LogicalDeviceSetPointCommandFactory();
        }
        return instance;
    }

    @Override
    public LogicalDeviceWriteCommand<SetPointDto> getCommand(final String node) {

        final LogicalDeviceWriteCommand<SetPointDto> command = RTU_COMMAND_MAP.get(node);

        if (command == null) {
            LOGGER.warn("No command found for data attribute {}", node);
        }
        return command;
    }

    private static void initializeLogicalDeviceCommandMap() {
        for (int i = ID_START; i <= ID_END; i++) {
            RTU_COMMAND_MAP.put(createMapKey(DataAttribute.SCHEDULE_ID, i), new Iec61850ScheduleIdCommand(i));
            RTU_COMMAND_MAP.put(createMapKey(DataAttribute.SCHEDULE_TYPE, i), new Iec61850ScheduleTypeCommand(i));
            RTU_COMMAND_MAP.put(createMapKey(DataAttribute.SCHEDULE_CAT, i), new Iec61850ScheduleCatCommand(i));
        }
    }

    private static String createMapKey(final DataAttribute da, final int index) {
        return da.getDescription() + index;
    }
}
