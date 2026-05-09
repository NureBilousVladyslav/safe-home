#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// Wi-Fi credentials
const char* ssid = "Vlad";
const char* wifiPassword = "********";

// Flask server URL.
// Your PC and ESP32-S3 must be connected to the same Wi-Fi network.
const char* serverUrl = "http://192.168.50.192:5000/iot/send_sensor_status";

// HC-SR04 pins for ESP32-S3
const int trigPin = 21;
const int echoPin = 19;

// User credentials and sensor data
String email = "vladyslav.bilous3@nure.ua";
String userPassword = "11111111";
String sensorID = "000003";

bool currentDoorState = false;
bool previousDoorState = false;
bool hasPreviousDoorState = false;

void setup_wifi() {
  Serial.println("Connecting to Wi-Fi...");
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, wifiPassword);

  unsigned long startAttemptTime = millis();

  while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < 20000) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\nFailed to connect to Wi-Fi. Restarting...");
    delay(3000);
    ESP.restart();
  }

  Serial.println("\nWi-Fi connected");
  Serial.print("ESP32-S3 IP address: ");
  Serial.println(WiFi.localIP());
}

long readDistanceCm() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);

  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  long duration = pulseIn(echoPin, HIGH, 30000);

  if (duration == 0) {
    return -1;
  }

  return (duration * 0.0343) / 2;
}

void sendData(bool newStatus) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Wi-Fi disconnected. Reconnecting...");
    setup_wifi();
  }

  WiFiClient client;
  HTTPClient http;

  http.setTimeout(10000);

  Serial.print("Sending data to: ");
  Serial.println(serverUrl);

  if (!http.begin(client, serverUrl)) {
    Serial.println("HTTP begin failed");
    return;
  }

  http.addHeader("Content-Type", "application/json");

  DynamicJsonDocument doc(1024);
  doc["email"] = email;
  doc["password"] = userPassword;
  doc["sensor_id"] = sensorID;
  doc["is_closed"] = newStatus;

  String requestBody;
  serializeJson(doc, requestBody);

  Serial.print("Request Body: ");
  Serial.println(requestBody);

  int httpCode = http.PUT(requestBody);

  Serial.print("HTTP Response code: ");
  Serial.println(httpCode);

  if (httpCode > 0) {
    String response = http.getString();
    Serial.print("Response: ");
    Serial.println(response);
  } else {
    Serial.print("HTTP error: ");
    Serial.println(http.errorToString(httpCode));
  }

  http.end();
  Serial.println("--------------------");
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

  setup_wifi();

  Serial.println("System started");
}

void loop() {
  long distance = readDistanceCm();

  if (distance < 0) {
    Serial.println("Distance read failed");
    delay(500);
    return;
  }

  Serial.print("Distance: ");
  Serial.print(distance);
  Serial.println(" cm");

  currentDoorState = distance <= 40;

  if (!hasPreviousDoorState || currentDoorState != previousDoorState) {
    sendData(currentDoorState);
    previousDoorState = currentDoorState;
    hasPreviousDoorState = true;
  }

  delay(500);
}
