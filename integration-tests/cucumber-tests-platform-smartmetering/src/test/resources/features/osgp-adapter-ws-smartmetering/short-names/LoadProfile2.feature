# Needs a DlmsDevice simulator with e650 profile on port 1026

# This test is broken, needs fixing later when support for L+G E650 devices is needed:
@SmartMetering @Platform @SN @Skip
Feature: SmartMetering short names - Load profile 2

  Scenario: Get load profile2 capture objects from L+G E650
    Given a dlms device
      | DeviceIdentification | TEST1024000000005 |
      | DeviceType           | SMART_METER_E     |
      | UseSn                | true              |
      | UseHdlc              | true              |
      | Port                 |              1026 |
      | Hls5active           | false             |
      | Lls1active           | true              |
    When the get specific attribute value request is received
      | DeviceIdentification | TEST1024000000005 |
      | ClassId              |                 7 |
      | ObisCodeA            |                 1 |
      | ObisCodeB            |                 0 |
      | ObisCodeC            |                99 |
      | ObisCodeD            |                 2 |
      | ObisCodeE            |                 0 |
      | ObisCodeF            |               255 |
      | Attribute            |                 3 |
    Then a get specific attribute value response should be returned
      | DeviceIdentification | TEST1024000000005 |
      | Result               | OK                |
      | ResponsePart         | 0-0:96.240.12.255 |

  Scenario: Get load profile2 buffer from L+G E650
    Given a dlms device
      | DeviceIdentification | TEST1024000000005 |
      | DeviceType           | SMART_METER_E     |
      | UseSn                | true              |
      | UseHdlc              | true              |
      | Port                 |              1026 |
      | Hls5active           | false             |
      | Lls1active           | true              |
    When the get profile generic data request is received
      | DeviceIdentification | TEST1024000000005 |
      | ClassId              |                 7 |
      | ObisCodeA            |                 1 |
      | ObisCodeB            |                 0 |
      | ObisCodeC            |                99 |
      | ObisCodeD            |                 2 |
      | ObisCodeE            |                 0 |
      | ObisCodeF            |               255 |
      | Attribute            |                 2 |
      | BeginDate            | 2015-09-06T23:59  |
      | EndDate              | 2015-09-08T00:01  |
    Then the profile generic data result should be returned
      | DeviceIdentification            | TEST1024000000005 |
      | Result                          | OK                |
      | NumberOfProfileEntries          |               289 |
      | NumberOfCaptureObjects          |                18 |
      | CaptureObject_ClassId_1         |                 8 |
      | CaptureObject_LogicalName_1     | 0.0.1.0.0.255     |
      | CaptureObject_AttributeIndex_1  |                 2 |
      | CaptureObject_DataIndex_1       |                 0 |
      | CaptureObject_Unit_1            | UNDEFINED         |
      | CaptureObject_ClassId_2         |                 3 |
      | CaptureObject_LogicalName_2     | 0.0.96.240.12.255 |
      | CaptureObject_AttributeIndex_2  |                20 |
      | CaptureObject_DataIndex_2       |                 0 |
      | CaptureObject_Unit_2            | UNDEFINED         |
      | CaptureObject_ClassId_3         |                 3 |
      | CaptureObject_LogicalName_3     | 1.0.32.7.126.255  |
      | CaptureObject_AttributeIndex_3  |                 2 |
      | CaptureObject_DataIndex_3       |                 0 |
      | CaptureObject_Unit_3            | V                 |
      | CaptureObject_ClassId_4         |                 3 |
      | CaptureObject_LogicalName_4     | 1.0.52.7.126.255  |
      | CaptureObject_AttributeIndex_4  |                 2 |
      | CaptureObject_DataIndex_4       |                 0 |
      | CaptureObject_Unit_4            | V                 |
      | CaptureObject_ClassId_5         |                 3 |
      | CaptureObject_LogicalName_5     | 1.0.72.7.126.255  |
      | CaptureObject_AttributeIndex_5  |                 2 |
      | CaptureObject_DataIndex_5       |                 0 |
      | CaptureObject_Unit_5            | V                 |
      | CaptureObject_ClassId_6         |                 3 |
      | CaptureObject_LogicalName_6     | 1.0.31.7.126.255  |
      | CaptureObject_AttributeIndex_6  |                 2 |
      | CaptureObject_DataIndex_6       |                 0 |
      | CaptureObject_Unit_6            | AMP               |
      | CaptureObject_ClassId_7         |                 3 |
      | CaptureObject_LogicalName_7     | 1.0.51.7.126.255  |
      | CaptureObject_AttributeIndex_7  |                 2 |
      | CaptureObject_DataIndex_7       |                 0 |
      | CaptureObject_Unit_7            | AMP               |
      | CaptureObject_ClassId_8         |                 3 |
      | CaptureObject_LogicalName_8     | 1.0.71.7.126.255  |
      | CaptureObject_AttributeIndex_8  |                 2 |
      | CaptureObject_DataIndex_8       |                 0 |
      | CaptureObject_Unit_8            | AMP               |
      | CaptureObject_ClassId_9         |                 3 |
      | CaptureObject_LogicalName_9     | 1.1.32.7.0.255    |
      | CaptureObject_AttributeIndex_9  |                11 |
      | CaptureObject_DataIndex_9       |                 0 |
      | CaptureObject_Unit_9            | V                 |
      | CaptureObject_ClassId_10        |                 3 |
      | CaptureObject_LogicalName_10    | 1.1.52.7.0.255    |
      | CaptureObject_AttributeIndex_10 |                11 |
      | CaptureObject_DataIndex_10      |                 0 |
      | CaptureObject_Unit_10           | V                 |
      | CaptureObject_ClassId_11        |                 3 |
      | CaptureObject_LogicalName_11    | 1.1.72.7.0.255    |
      | CaptureObject_AttributeIndex_11 |                11 |
      | CaptureObject_DataIndex_11      |                 0 |
      | CaptureObject_Unit_11           | V                 |
      | CaptureObject_ClassId_12        |                 3 |
      | CaptureObject_LogicalName_12    | 1.1.31.7.0.255    |
      | CaptureObject_AttributeIndex_12 |                11 |
      | CaptureObject_DataIndex_12      |                 0 |
      | CaptureObject_Unit_12           | AMP               |
      | CaptureObject_ClassId_13        |                 3 |
      | CaptureObject_LogicalName_13    | 1.1.51.7.0.255    |
      | CaptureObject_AttributeIndex_13 |                11 |
      | CaptureObject_DataIndex_13      |                 0 |
      | CaptureObject_Unit_13           | AMP               |
      | CaptureObject_ClassId_14        |                 3 |
      | CaptureObject_LogicalName_14    | 1.1.71.7.0.255    |
      | CaptureObject_AttributeIndex_14 |                11 |
      | CaptureObject_DataIndex_14      |                 0 |
      | CaptureObject_Unit_14           | AMP               |
      | CaptureObject_ClassId_15        |                 3 |
      | CaptureObject_LogicalName_15    | 1.1.91.7.0.255    |
      | CaptureObject_AttributeIndex_15 |                11 |
      | CaptureObject_DataIndex_15      |                 0 |
      | CaptureObject_Unit_15           | AMP               |
      | CaptureObject_ClassId_16        |                 3 |
      | CaptureObject_LogicalName_16    | 1.1.36.7.0.255    |
      | CaptureObject_AttributeIndex_16 |                11 |
      | CaptureObject_DataIndex_16      |                 0 |
      | CaptureObject_Unit_16           | W                 |
      | CaptureObject_ClassId_17        |                 3 |
      | CaptureObject_LogicalName_17    | 1.1.56.7.0.255    |
      | CaptureObject_AttributeIndex_17 |                11 |
      | CaptureObject_DataIndex_17      |                 0 |
      | CaptureObject_Unit_17           | W                 |
      | CaptureObject_ClassId_18        |                 3 |
      | CaptureObject_LogicalName_18    | 1.1.76.7.0.255    |
      | CaptureObject_AttributeIndex_18 |                11 |
      | CaptureObject_DataIndex_18      |                 0 |
      | CaptureObject_Unit_18           | W                 |
