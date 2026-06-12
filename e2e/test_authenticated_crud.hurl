# Test CRUD operations with authenticated user

# Create a device via PATCH
PATCH http://localhost:8080/devices/e2e-test-device
Authorization: Bearer {{TOKEN}}
Content-Type: application/json
{
  "ownership": {
    "operator": "E2E-Test-Operator",
    "provider": "e2e-provider"
  },
  "location": {
    "street": "Testowa",
    "houseNumber": "1",
    "city": "Warszawa",
    "postalCode": "00-001",
    "country": "POL",
    "coordinates": {
      "longitude": 21.0122,
      "latitude": 52.2297
    }
  },
  "openingHours": {
    "alwaysOpen": true
  },
  "settings": {
    "autoStart": true,
    "remoteControl": false,
    "showOnMap": true,
    "publicAccess": true
  }
}
HTTP 200
[Asserts]
jsonpath "$.deviceId" == "e2e-test-device"
jsonpath "$.ownership.operator" == "E2E-Test-Operator"
jsonpath "$.ownership.provider" == "e2e-provider"
jsonpath "$.openingHours.alwaysOpen" == true
jsonpath "$.visibility.roamingEnabled" == true

# Read the device details
GET http://localhost:8080/devices/e2e-test-device
Authorization: Bearer {{TOKEN}}
HTTP 200
[Asserts]
jsonpath "$.deviceId" == "e2e-test-device"

# Read device summary list
GET http://localhost:8080/devices?page=0&size=10
Authorization: Bearer {{TOKEN}}
Accept: application/vnd.device.summary+json
HTTP 200
[Asserts]
jsonpath "$.totalElements" >= 1

# Update device settings
PATCH http://localhost:8080/devices/e2e-test-device
Authorization: Bearer {{TOKEN}}
Content-Type: application/json
{
  "settings": {
    "autoStart": false,
    "remoteControl": true
  }
}
HTTP 200
[Asserts]
jsonpath "$.settings.autoStart" == false
jsonpath "$.settings.remoteControl" == true
jsonpath "$.settings.showOnMap" == true
