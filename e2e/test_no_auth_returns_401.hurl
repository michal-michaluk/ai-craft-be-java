# Test that protected endpoints return 401 without auth
GET http://localhost:8080/devices/unknown-id
HTTP 401

GET http://localhost:8080/devices?page=0&size=10
HTTP 401

PATCH http://localhost:8080/devices/test-device-001
Content-Type: application/json
{}
HTTP 401

GET http://localhost:8080/configs/IntervalRules
HTTP 401

PUT http://localhost:8080/configs/IntervalRules
Content-Type: application/json
{}
HTTP 401
