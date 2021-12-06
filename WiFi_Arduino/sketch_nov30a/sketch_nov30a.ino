#include <ESP8266WiFi.h>

#include <Wire.h>
#include <LiquidCrystal_I2C.h>

#define LED1_Pin 12 //D8 15
#define LED2_Pin 14 //D7 13
#define LED3_Pin 13 //D6 12
#define LED4_Pin 15 //D5 14

#define clockwise 0//3
#define counterclockwise 16//2
#define delay_ms 100

#define Door_con_c 0 //D3
#define Door_con_i 16//D3

#define Door_alarm 2 //D4

#define doorDelay 50 //50ms

#define arduinoResPin A0
const char* ssid = "arduino";// type in the SSID name which you want to connect
const char* password = "t108360121";// type the password of the WiFi AP you choose

LiquidCrystal_I2C lcd(0x27, 16, 2);

int LED1 = 0, LED2 = 0, LED3 = 0, LED4 = 0;
int door = 0, door_tmp = 0;

WiFiServer server(80);
void setup() {
  Serial.begin(74880);
  delay(10);
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  server.begin();
  Serial.println("Server started");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  pinMode(LED1_Pin, OUTPUT);
  pinMode(LED2_Pin, OUTPUT);
  pinMode(LED3_Pin, OUTPUT);
  pinMode(LED4_Pin, OUTPUT);
  pinMode(clockwise, OUTPUT);
  pinMode(counterclockwise, OUTPUT);
  pinMode(arduinoResPin, INPUT);

  digitalWrite(clockwise, LOW);
  digitalWrite(counterclockwise, LOW);
  digitalWrite(LED1_Pin, LOW);
  digitalWrite(LED2_Pin, LOW);
  digitalWrite(LED3_Pin, LOW);
  digitalWrite(LED4_Pin, LOW);

  pinMode(Door_alarm, INPUT_PULLUP);
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("IP:");
  lcd.setCursor(0, 1);
  lcd.print(WiFi.localIP());
}
void loop() {
  WiFiClient client = server.available();
  if (client) {
    //-----------------------以下-----WiFi-----------------------------------------------
    //等待訊息傳到這個伺服器
    Serial.println("new client");
    while (!client.available()) {
      delay(1);
    }
    //收到傳過來的訊息之後，將收到的訊息存成req
    String req = client.readStringUntil('\r');
    Serial.println(req);
    client.flush();

    int val;
    if (req.indexOf("/test") != -1) {
      String s = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\nConnected\n";
      client.print(s);
    }
    else if (req.indexOf("/getvalue") != -1) {

      String s = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\nGetValue/LED1";
      s += (LED1) ? "high" : "low";
      s += "/LED2";
      s += (LED2) ? "high" : "low";
      s += "/LED3";
      s += (LED3) ? "high" : "low";
      s += "/LED4";
      s += (LED4) ? "high" : "low";
      s += "/door";
      s += (door) ? "open" : "close";
      s += "/WarningResult";
      s += (digitalRead(Door_alarm)) ? "true" : "false";

      s += "/RFIDresponse";
      int analognum = analogRead(arduinoResPin);
      if (analognum > 767)
        s += "Connected";
      else if (analognum > 511) {
        s += "Valid";
        if (door == 0) {
          LED1 = 1; LED2 = 1; LED3 = 1; LED4 = 1; door = 1;
          s += "Entry";
        }
        else {
          LED1 = 0; LED2 = 0; LED3 = 0; LED4 = 0; door = 0;
          s += "Leave";
        }
      }
      else
        s += "Invalid";
      Serial.print("analogNum is ");
      Serial.println(analognum);
      client.print(s);
      GPIO();
    }
    else if (req.indexOf("/gpio/") != -1) {
      if (req.indexOf("/LED/1/high") != -1)
        LED1 = 1;
      if (req.indexOf("/LED/1/low") != -1)
        LED1 = 0;
      if (req.indexOf("/LED/2/high") != -1)
        LED2 = 1;
      if (req.indexOf("/LED/2/low") != -1)
        LED2 = 0;
      if (req.indexOf("/LED/3/high") != -1)
        LED3 = 1;
      if (req.indexOf("/LED/3/low") != -1)
        LED3 = 0;
      if (req.indexOf("/LED/4/high") != -1)
        LED4 = 1;
      if (req.indexOf("/LED/4/low") != -1)
        LED4 = 0;
      if (req.indexOf("/door/1/open") != -1)
        door = 1;
      if (req.indexOf("/door/1/close") != -1)
        door = 0;
      String s = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<!DOCTYPE HTML>\r\nCompleted\r\n</html>\n";
      client.print(s);
      GPIO();

    }
    else if (req.indexOf("/WarningCheck") != -1) {
      String s = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\nWarningResult";
      if (digitalRead(Door_alarm)) {
        s += "true";
        client.print(s);
        client.flush();
        /*   while(digitalRead(Door_alarm)){
           }*/
      }
      else {
        s += "false";
        client.print(s);
        client.flush();
      }
    }

    else {
      Serial.println("invalid request");
      client.flush();
      client.stop();
      return;
    }
    client.flush();
    client.flush();
    client.flush();
    client.flush();
    client.flush();
    delay(1);
    Serial.println("client disconnected");
    //---------------------以上----WiFi--------------------------------------------------
  }
  /* if(!digitalRead(Door_alarm)){
     String s = "Warning";
     client.print(s);
     client.flush();
     while(!digitalRead(Door_alarm)){

     }
    }1*/
}
void GPIO() {
  digitalWrite(LED1_Pin, LED1);
  digitalWrite(LED2_Pin, LED2);
  digitalWrite(LED3_Pin, LED3);
  digitalWrite(LED4_Pin, LED4);
  if (door != door_tmp) {
    if (door) {
      digitalWrite(counterclockwise, HIGH);
      delay(delay_ms);
      digitalWrite(counterclockwise, LOW);
    }
    else {
      digitalWrite(clockwise, HIGH);
      delay(delay_ms);
      digitalWrite(clockwise, LOW);
    }
    door_tmp = door;
  }
}
