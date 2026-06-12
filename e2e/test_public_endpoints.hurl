# Test that protocol (boot notification) endpoints work without auth
# IoT16
POST http://localhost:8080/protocols/iot16/bootnotification/device-noauth-001
Content-Type: application/json
{
  "vendor": "TestVendor",
  "model": "TestModel",
  "serial": "SN001",
  "firmware": "1.0",
  "protocol": "IoT16"
}
HTTP 200

# IoT20
POST http://localhost:8080/protocols/iot20/bootnotification/device-noauth-002
Content-Type: application/json
{
  "vendor": "TestVendor",
  "model": "TestModel",
  "serial": "SN002",
  "firmware": "1.0",
  "protocol": "IoT20"
}
HTTP 200

# Health endpoint also public
GET http://localhost:8080/actuator/health
HTTP 200
