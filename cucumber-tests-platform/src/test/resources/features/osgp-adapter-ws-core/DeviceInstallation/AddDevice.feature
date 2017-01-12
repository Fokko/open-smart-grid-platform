Feature: Add Device
  As a ...
  I want to ...
  In order to ...

  Scenario Outline: Add New Device
    Given a device model
      | ModelCode | <ModelCode> |
      | Metered   | <Metered>   |
    When receiving an add device request
      | DeviceUid             | <DeviceUid>             |
      | DeviceIdentification  | <DeviceIdentification>  |
      | alias                 | <Alias>                 |
      | Owner                 | <Owner>                 |
      | containerPostalCode   | <ContainerPostalCode>   |
      | containerCity         | <ContainerCity>         |
      | containerStreet       | <ContainerStreet>       |
      | containerNumber       | <ContainerNumber>       |
      | containerMunicipality | <ContainerMunicipality> |
      | gpsLatitude           | <GpsLatitude>           |
      | gpsLongitude          | <GpsLongitude>          |
      | IsActivated           | <Activated>             |
      | HasSchedule           | <HasSchedule>           |
      | PublicKeyPresent      | <PublicKeyPresent>      |
      | Manufacturer          | <Manufacturer>          |
      | ModelCode             | <ModelCode>             |
      | Description           | <Description>           |
      | Metered               | <Metered>               |
    Then the add device response is successful
    # 'Activated' is altijd 'false' wanneer een nieuwe device wordt aangemaakt.
    # Om deze stap volledig succesvol te laten verlopen moet de value van 'Activated' 'false' zijn.
    And the device exists
      | DeviceIdentification       | <DeviceIdentification>  |
      | alias                      | <Alias>                 |
      | OrganizationIdentification | <Owner>                 |
      | containerPostalCode        | <ContainerPostalCode>   |
      | containerCity              | <ContainerCity>         |
      | containerStreet            | <ContainerStreet>       |
      | containerNumber            | <ContainerNumber>       |
      | containerMunicipality      | <ContainerMunicipality> |
      | gpsLatitude                | <GpsLatitude>           |
      | gpsLongitude               | <GpsLongitude>          |
      | Activated                  | <Activated>             |
      | Active                     | false                   |
      | HasSchedule                | <HasSchedule>           |
      | PublicKeyPresent           | <PublicKeyPresent>      |
      | DeviceModel                | <ModelCode>             |

    Examples: 
      | DeviceUid  | DeviceIdentification                     | Alias | Owner    | ContainerPostalCode | ContainerCity | ContainerStreet | ContainerNumber | ContainerMunicipality | GpsLatitude | GpsLongitude | Activated | HasSchedule | PublicKeyPresent | Manufacturer | ModelCode  | Description | Metered |
      | 1234567890 | TEST1024000000001                        |       | test-org | 1234AA              | Maastricht    | Stationsstraat  |              12 |                       |           0 |            0 | true      | false       | false            | Test         | Test Model | Test        | true    |
      | 3456789012 | 0123456789012345678901234567890123456789 |       | test-org | 1234AA              | Maastricht    | Stationsstraat  |              12 |                       |           0 |            0 | true      | false       | false            | Test         | Test Model | Test        | true    |

  Scenario Outline: Add a device with an incorrect device identification
    Given a device model
      | ModelCode | <ModelCode> |
      | Metered   | <Metered>   |
    When receiving an add device request
      | DeviceUid               | <DeviceUid>             |
      | DeviceIdentification    | <DeviceIdentification>  |
      | alias                   | <Alias>                 |
      | owner                   | <Owner>                 |
      | containerPostalCode     | <ContainerPostalCode>   |
      | containerCity           | <ContainerCity>         |
      | containerStreet         | <ContainerStreet>       |
      | containerNumber         | <ContainerNumber>       |
      | containerMunicipality   | <ContainerMunicipality> |
      | gpsLatitude             | <GpsLatitude>           |
      | gpsLongitude            | <GpsLongitude>          |
      | Activated               | <Activated>             |
      | HasSchedule             | <HasSchedule>           |
      | PublicKeyPresent        | <PublicKeyPresent>      |
      | DeviceModelManufacturer | <Manufacturer>          |
      | DeviceModelModelCode    | <ModelCode>             |
      | DeviceModelDescription  | <Description>           |
      | DeviceModelMetered      | <Metered>               |
    Then the add device response contains soap fault
      | FaultCode        | SOAP-ENV:Client                                                                                                                                                                                                                                          |
      | FaultString      | Validation error                                                                                                                                                                                                                                         |
      | ValidationErrors | cvc-minLength-valid: Value '<DeviceIdentification>' with length = '0' is not facet-valid with respect to minLength '1' for type 'Identification'.;cvc-type.3.1.3: The value '<DeviceIdentification>' of element 'ns2:DeviceIdentification' is not valid. |

    # Note: The validation errors are ; separated if there are multiple.
    Examples: 
      | DeviceUid  | DeviceIdentification | Alias       | Owner    | ContainerPostalCode | ContainerCity | ContainerStreet | ContainerNumber | ContainerMunicipality | GpsLatitude | GpsLongitude | Activated | HasSchedule | PublicKeyPresent | Manufacturer | ModelCode  | Description | Metered |
      | 2345678901 |                      | Test device | test-org | 1234AA              | Maastricht    | Stationsstraat  |              12 |                       |           0 |            0 | true      | false       | false            | Test         | Test Model | Test        | true    |

  Scenario Outline: Add a device with incorrect data
    Given a device model
      | ModelCode | <ModelCode> |
      | Metered   | <Metered>   |
    When receiving an add device request
      | DeviceUid               | <DeviceUid>             |
      | DeviceIdentification    | <DeviceIdentification>  |
      | alias                   | <Alias>                 |
      | owner                   | <Owner>                 |
      | containerPostalCode     | <ContainerPostalCode>   |
      | containerCity           | <ContainerCity>         |
      | containerStreet         | <ContainerStreet>       |
      | containerNumber         | <ContainerNumber>       |
      | containerMunicipality   | <ContainerMunicipality> |
      | gpsLatitude             | <GpsLatitude>           |
      | gpsLongitude            | <GpsLongitude>          |
      | Activated               | <Activated>             |
      | HasSchedule             | <HasSchedule>           |
      | PublicKeyPresent        | <PublicKeyPresent>      |
      | DeviceModelManufacturer | <Manufacturer>          |
      | DeviceModelModelCode    | <ModelCode>             |
      | DeviceModelDescription  | <Description>           |
      | DeviceModelMetered      | <Metered>               |
    Then the add device response contains soap fault
      | FaultCode         | SOAP-ENV:Client                                                                                                                                                                                                                             |
      | FaultString       | Validation error                                                                                                                                                                                                                            |
      | Validation Errors | cvc-pattern-valid: Value '<DeviceIdentification>' is not facet-valid with respect to pattern '[^ ]{0,40}' for type 'Identification'.;cvc-type.3.1.3: The value '<DeviceIdentification>' of element 'ns2:DeviceIdentification' is not valid. |

    Examples: 
      # TODO, deviceidentification with 40 characters.
      # Empty owner, is defaulted
      # Unknown is also default as I am requesting with test-org in the headers.
      | DeviceUid  | DeviceIdentification                                | Alias       | Owner | ContainerPostalCode | ContainerCity | ContainerStreet | ContainerNumber | ContainerMunicipality | GpsLatitude | GpsLongitude | Activated | HasSchedule | PublicKeyPresent | Manufacturer | ModelCode  | Description | Metered |
      | 5678901234 | TEST1024000000001TEST1024000000001TEST1024000000001 | Test device |       | 1234AA              | Maastricht    | Stationsstraat  |              12 |                       |           0 |            0 | true      | false       | false            | Test         | Test Model | Test        | true    |

  Scenario Outline: Add new device with only spaces as device identification
    Given a device model
      | ModelCode | <ModelCode> |
      | Metered   | <Metered>   |
    When receiving an add device request
      | DeviceUid             | <DeviceUid>             |
      | DeviceIdentification  | <DeviceIdentification>  |
      | alias                 | <Alias>                 |
      | Owner                 | <Owner>                 |
      | containerPostalCode   | <ContainerPostalCode>   |
      | containerCity         | <ContainerCity>         |
      | containerStreet       | <ContainerStreet>       |
      | containerNumber       | <ContainerNumber>       |
      | containerMunicipality | <ContainerMunicipality> |
      | gpsLatitude           | <GpsLatitude>           |
      | gpsLongitude          | <GpsLongitude>          |
      | Activated             | <Activated>             |
      | HasSchedule           | <HasSchedule>           |
      | PublicKeyPresent      | <PublicKeyPresent>      |
      | Manufacturer          | <Manufacturer>          |
      | ModelCode             | <ModelCode>             |
      | Description           | <Description>           |
      | Metered               | <Metered>               |
    Then the add device response contains soap fault
      | FaultCode        | SOAP-ENV:Client                                                                                                                                                                                                                             |
      | FaultString      | Validation error                                                                                                                                                                                                                            |
      | ValidationErrors | cvc-pattern-valid: Value '<DeviceIdentification>' is not facet-valid with respect to pattern '[^ ]{0,40}' for type 'Identification'.;cvc-type.3.1.3: The value '<DeviceIdentification>' of element 'ns2:DeviceIdentification' is not valid. |

    Examples: 
      | DeviceUid  | DeviceIdentification   | Alias | Owner    | ContainerPostalCode | ContainerCity | ContainerStreet | ContainerNumber | ContainerMunicipality | GpsLatitude | GpsLongitude | Activated | HasSchedule | PublicKeyPresent | Manufacturer | ModelCode  | Description | Metered |
      | 1234567890 | "                    " |       | test-org | 1234AA              | Maastricht    | Stationsstraat  |              12 |                       |           0 |            0 | false     | false       | false            | Test         | Test Model | Test        | true    |

  Scenario: Add New Device With Unknown Owner Organization
    Given a device model
      | ModelCode | Test Model |
      | Metered   | true       |
    When receiving an add device request with an unknown organization
      | DeviceIdentification | TEST1024000000001 |
      | Owner                | org-test          |
    Then the add device response contains soap fault
      | FaultCode      | SOAP-ENV:Server                                                  |
      | FaultString    | UNKNOWN_ORGANISATION                                             |
      | InnerException | com.alliander.osgp.domain.core.exceptions.UnknownEntityException |
      | InnerMessage   | Organisation with id "unknown-organization" could not be found.  |

  Scenario: Adding a device which already exists
    Given a device
      | DeviceIdentification | TEST1024000000001 |
    When receiving an add device request
      | DeviceIdentification | TEST1024000000001 |
    Then the add device response contains soap fault
      | Message | EXISTING_DEVICE |
