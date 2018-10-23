@SmartMetering @Platform
Feature: Core Operations, DeviceLifeCycle
  As a grid operator
  I want to distinguish the various statuses of a device
  So I know what I can or cannot do with the device

  Scenario Outline: Set dlms device status
    Given a dlms device
      | DeviceIdentification | TEST1024000000001 |
      | DeviceType           | SMART_METER_E     |
    When the SetDeviceLifecycleStatus request is received
      | DeviceIdentification  | TEST1024000000001 |
      | DeviceLifecycleStatus | <Status>          |
    Then the device lifecycle status in the database is
      | DeviceIdentification  | TEST1024000000001 |
      | DeviceLifecycleStatus | <Status>          |

    Examples: 
      | Status                |
      | NEW_IN_INVENTORY      |
      | READY_FOR_USE         |
      | REGISTERED            |
      | IN_USE                |
      | RETURNED_TO_INVENTORY |
      | UNDER_TEST            |
      | DESTROYED             |