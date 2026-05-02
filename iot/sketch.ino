#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// WiFi credentials
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// Server URL
const char* serverUrl = "https://safe-home-backend-d2f2atb3d0eee9ay.northeurope-01.azurewebsites.net/iot/send_sensor_status";

// HC-SR04 pins
const int trig = 2;  // Adjusted to D2 for Wokwi
const int echo = 4;  // Adjusted to D4 for Wokwi

// User credentials and sensor data
String email = "";
String userPassword = "";
String sensorID = "";
bool currentDoorState = false;
bool previousDoorState = false;

// Function to connect to Wi-Fi
void setup_wifi() {
  Serial.println("Connecting to WiFi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

// Function to send data to the server
void sendData(bool newStatus) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;

    Serial.print("Sending data to: ");
    Serial.println(serverUrl);

    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");

    // Create JSON document
    DynamicJsonDocument doc(1024);
    doc["email"] = email;
    doc["password"] = userPassword;
    doc["sensor_id"] = sensorID;
    doc["is_closed"] = newStatus ? "true" : "false";

    String requestBody;
    serializeJson(doc, requestBody);

    Serial.print("Request Body: ");
    Serial.println(requestBody);

    int httpCode = http.PUT(requestBody);
    Serial.print("HTTP Response code: ");
    Serial.println(httpCode);

    if (httpCode == HTTP_CODE_OK) {
      String response = http.getString();
      Serial.print("Response: ");
      Serial.println(response);
    } else {
      Serial.print("Error sending PUT: ");
      Serial.println(httpCode);
    }

    http.end();
    Serial.println("--------------------");
  }
}

void setup() {
  // Initialize serial communication
  Serial.begin(115200);

  // Initialize WiFi
  setup_wifi();

  // Prompt the user for credentials and sensor ID
  Serial.println("Enter email:");
  while (Serial.available() == 0) {
    delay(100);
  }
  email = Serial.readStringUntil('\n');
  email.trim();  // Remove whitespace

  Serial.println("Enter password:");
  while (Serial.available() == 0) {
    delay(100);
  }
  userPassword = Serial.readStringUntil('\n');
  userPassword.trim();  // Remove whitespace

  Serial.println("Enter sensor ID:");
  Serial.println("Enter password:");
  while (Serial.available() == 0) {
    delay(100);
  }
  sensorID = Serial.readStringUntil('\n');
  sensorID.trim();  // Remove whitespace


  // Debug print the entered values
  Serial.print("Email: ");
  Serial.println(email);
  Serial.print("Password: ");
  Serial.println(userPassword);
  Serial.print("Sensor ID: ");
  Serial.println(sensorID);

  // Initialize HC-SR04 pins
  pinMode(trig, OUTPUT);
  pinMode(echo, INPUT);
}

void loop() {
  // Clear the TRIG pin by setting it low
  digitalWrite(trig, LOW);
  delayMicroseconds(2);
  // Send a 10 microsecond pulse to TRIG to start the measurement
  digitalWrite(trig, HIGH);
  delayMicroseconds(10);
  digitalWrite(trig, LOW);

  // Measure the time for the echo to return
  long duration = pulseIn(echo, HIGH);

  // Calculate distance in centimeters
  long distance = (duration * 0.034) / 2;

  // Determine door state based on the distance
  if (distance > 40) {
    currentDoorState = false;  // Door is open
  } else {
    currentDoorState = true;  // Door is closed
  }

  // Send data if the door state has changed
  if (currentDoorState != previousDoorState) {
    sendData(currentDoorState);
    previousDoorState = currentDoorState;
  }

  delay(500);  // Delay for next reading
}