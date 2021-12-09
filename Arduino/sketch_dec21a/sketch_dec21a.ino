#include <SPI.h>
#include <MFRC522.h>
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
#include "LedControl.h"
#define speaker 4
#define GLED 3
#define RLED 2
#define WemosControl_1 8
#define WemosControl_2 9
const byte InvalidValue = 100;
const byte ValidValue = 200;
const byte idleValue = 255;
LiquidCrystal_I2C lcd(0x27,16,2);
const byte RESET_PIN = A1;
const byte CS_PIN = 10;
MFRC522 mfrc522(CS_PIN,RESET_PIN);
LedControl lc=LedControl(5,7,6,1); // DIN CLK  CS
const byte Matrix[2][8] = {{0x30,0x60,0xC0,0x60,0x30,0x18,0x0C,0x06},
                           {0xC6,0xEE,0x7C,0x38,0x7C,0xEE,0xC6,0x00}};
const byte leetcode[4] = {0x8E,0x50,0x7D,0x89};

void setup() {
  // put your setup code here, to run once:
  lcd.init();
  lcd.backlight();
  pinMode(GLED,OUTPUT);
  pinMode(RLED,OUTPUT);
  digitalWrite(GLED,LOW);
  digitalWrite(RLED,LOW);
  
  pinMode(WemosControl_1,OUTPUT);
  pinMode(WemosControl_2,OUTPUT);
  digitalWrite(WemosControl_1,HIGH);
  digitalWrite(WemosControl_2,HIGH);
  
  Serial.begin(9600);
  SPI.begin();
  mfrc522.PCD_Init();

  lc.shutdown(0,false);
  /* Set the brightness to a medium values */
  lc.setIntensity(0,8);
  /* and clear the display */
  lc.clearDisplay(0);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(mfrc522.PICC_IsNewCardPresent() && mfrc522.PICC_ReadCardSerial()){
    byte *id = mfrc522.uid.uidByte;
    byte idSize = mfrc522.uid.size;
    byte sak = mfrc522.uid.sak;

    Serial.print("PICC Type: ");
    MFRC522::PICC_Type type = mfrc522.PICC_GetType(sak);
    Serial.println(mfrc522.PICC_GetTypeName(type));
    Serial.print("UID : ");
    int con = 1;
    for(byte i = 0;i < idSize;i++){
      Serial.print(id[i],HEX);
      Serial.print(" ");
      if(id[i] != leetcode[i]) con = 0;
    }
    Serial.println("");
    if(con){
      Serial.println("This Card is valid, Welcome");
      lcd.setCursor(0,0);
      lcd.print("Welcome Home!");
      digitalWrite(GLED,HIGH);
      tone(speaker,600,500);
      for(int i = 0;i < 8;i++){
        lc.setRow(0,i,Matrix[0][i]);
      }
      digitalWrite(WemosControl_1,HIGH);
      digitalWrite(WemosControl_2,LOW);
      delay(500);
      digitalWrite(WemosControl_1,HIGH);
      digitalWrite(WemosControl_2,HIGH);
      delay(1500);
      noTone(speaker);
      digitalWrite(GLED,LOW);
      lc.clearDisplay(0);
      lcd.init();
      lcd.backlight();
    }
    else{
      Serial.println("This Card is invalid,Try again");
      lcd.setCursor(0,0);
      lcd.print("This card is");
      lcd.setCursor(0,1);
      lcd.print("Invalid card");
      digitalWrite(RLED,HIGH);
      for(int i = 0;i < 8;i++){
        lc.setRow(0,i,Matrix[1][i]);
      }
      for(int i = 0;i < 5;i++){ //1s
        tone(speaker,800);
        delay(100);
        noTone(speaker);
        delay(100);
      }
      digitalWrite(WemosControl_1,LOW);
      digitalWrite(WemosControl_2,LOW);
      delay(500);
      digitalWrite(WemosControl_1,HIGH);
      digitalWrite(WemosControl_2,HIGH);
      digitalWrite(RLED,LOW);
      lc.clearDisplay(0);
      lcd.init();
      lcd.backlight();
    }
       
     mfrc522.PICC_HaltA();
  }
  
  
}


//0x30,0x18,0x0C,0xFE,0xFE,0x0C,0x18,0x30, 通過
//0xC6,0xEE,0x7C,0x38,0x7C,0xEE,0xC6,0x00, 未通過
