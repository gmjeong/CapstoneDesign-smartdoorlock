#include <Servo.h>
#include <SoftwareSerial.h>
#include <EEPROM.h>
#include <string.h>
#include <AESLib.h>
#include <math.h>

#define TB_GET_AES 0
#define TB_INIT 1
#define TB_OPEN 2
#define TB_OPEN_ANS 3
#define TB_RESET_PASSWORD 4
#define TB_RESET_PASSWORD_ANS 5
#define TB_GET_CODE 6

Servo servo;
SoftwareSerial btSerial(12, 13); // RX, TX

int servo_pin = 7;  //Digital pin for servo motor
uint8_t aes_keys[16] = {0,};
long rsa_keys[2] = {0, 65537};

bool hasPassword = false;
byte password[6];
byte secure_key[32];
bool hasSecureKey = false;

bool receivedAllData = false;

byte data[100] = {0, };
int index = 0;
int len = -1;

byte myData[100] = {0,};
int mylen = -1;
int myseq = -1;

byte q1;
byte q2;
byte q3;



long byteToInt(uint8_t *hexNum) {
  long data = 0;
  long temp = 0;
  int i = 0;
  while (i != 4) {
    temp = hexNum[i];
    temp <<= (8 * i);
    i++;
    data += temp;
  }
  return data;
}


uint8_t* intToByte(uint32_t quotient, uint8_t rtn[4]) {
  byte temp;
  uint8_t hexNum[4] = {0,};
  int i = 0;
  while (quotient != 0) {
    temp = quotient & 0xFF;
    hexNum[i++] = temp;
    quotient >>= 8;
  }
  for (int i = 0; i < 4; i++) {
    rtn[i] = hexNum[i];
  }

  return hexNum;
}

uint64_t raiseto_mod(uint8_t a, long b, long c) {
  uint64_t temp = 1;
  for (long i = 0; i < b; ++i) {
    temp = (temp * a) % c;
  }
  return temp;
}

long rsa_encrypt(uint8_t plaintext, long* key) {
  return raiseto_mod(plaintext, key[1], key[0]);
}

uint8_t generate_key(int lowerbound, int upperbound) {  //generate aes key
  uint8_t result;
  result = random(0, 255);
  return result;
}

void preCheck() { //check if aes key, password, secure key is available
  byte value = EEPROM.read(0);

  if (value == 0) {
    for (int i = 0; i < 16; i++) {
      aes_keys[i] = generate_key(1, 255);
      EEPROM.write(i, aes_keys[i]);
    }
  }
  else {
    for (int i = 0; i < 16; i++) {
      aes_keys[i] = EEPROM.read(i);
    }
  }

  value = EEPROM.read(16); //check for the password
  if (value == 0) hasPassword = false;
  else {
    hasPassword = true;
    for (int i = 0; i < 6; i++) {
      password[i] = EEPROM.read(i + 17);
    }
  }

  value = EEPROM.read(23);  //check for the Secure Key
  if (value == 0) hasSecureKey = false;
  else {
    hasSecureKey = true;
    for (int i = 0; i < 32; i++) {
      secure_key[i] = EEPROM.read(i + 24);
    }
  }
}

void ClearMyData() {
  for (int i = 0; i < sizeof(myData); i++) {
    myData[i] = 0;
  }
}

void DecryptAES(int num, int buflen) {
  int i = num;
  if (buflen > 16) {
    while (i + 16 < buflen ) {
      aes128_dec_single(aes_keys, &data[i]);
      i += 16;
    }
  }
  aes128_dec_single(aes_keys, &data[i]);
}

void EncryptAES(int num, int buflen) {
  int i = num;
  if (buflen > 16) {
    while (i + 16 < buflen ) {
      aes128_enc_single(aes_keys, &myData[i]);
      i += 16;
    }
  }
  aes128_enc_single(aes_keys, &myData[i]);
}

void ServoOnOff(int angle) {  //turn on and off the servo motor to save energy

  servo.attach(servo_pin);
  servo.write(angle);
  delay(6000);
  servo.write(45);
  servo.detach();

}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  btSerial.begin(9600);//AT모드에선 38400으로 바꿔야함

  ServoOnOff(45);
  randomSeed(analogRead(0));

  preCheck();


}

void loop() {
  if (btSerial.available()) {
    //    btSerial.flush()
    byte value = btSerial.read();
    if (int(value) == 255) return;  // ignoring rubbish data
    Serial.println(int(value));
    data[index] = value;
    if (index == 0) {
      len = data[0];
    }
    index++;
    if (index == len) {
      receivedAllData = true;
    }
  }

  if (receivedAllData) {  //after receiving all data
    for (int i = 0; i < index; i++) {
      Serial.print(int(data[i])); Serial.print(" ");
    }
    Serial.println();
    int cmd = data[1];
    if (cmd != TB_GET_AES) {
      DecryptAES(2, len - 2);
    }
    byte seq = data[2];
    Serial.print("seq: "); Serial.println(seq);
    for (int i = 0; i < index; i++) {
      Serial.print(int(data[i])); Serial.print(" ");
    }
    Serial.println();
    myseq = seq + 1;
    ClearMyData();

    if (cmd == TB_GET_AES) {
      Serial.println("TB_GET_AES!!!!!!!!!");
      uint8_t bytekey[4];
      for (int i = 0; i < 4; i++) {
        bytekey[i] = data[i + 3];
      }
      rsa_keys[0] = byteToInt(bytekey);
      rsa_keys[1] = 65537;  //generates same key in android system
      Serial.println(rsa_keys[0]);
      myData[0] = 67;
      myData[1] = cmd;
      myData[2] = myseq;
      for (int i = 0; i < 16; i++) {
        long q = rsa_encrypt(aes_keys[i], rsa_keys);
        Serial.println(q);
        uint8_t temp[4];
        intToByte(q, temp);
        for (int j = 0; j < 4; j++) {
          myData[(4 * i) + j + 3] = temp[j];
        }
      }
      btSerial.write(myData, 67);
    }


    else if (cmd == TB_INIT) {
      Serial.println("TB_INIT!!!!!!!!!");
      bool same_password = true;
      if (hasPassword) {
        for (int i = 0; i < 6; i++) {
          if (password[i] != data[i + 3]) {
            same_password = false;
            myData[3] = 0;
            break;
          }
        }
        if (same_password) {
          for (int i = 0; i < 32; i++) {
            EEPROM.write(i + 24, data[9 + i]);
          }
          hasPassword = true;
          hasSecureKey = true;
          EEPROM.write(16, 1);
          EEPROM.write(23, 1);
          myData[3] = 1;
        }
      }
      else {
        for (int i = 0; i < 6; i++) {
          password[i] = data[i + 3];
          EEPROM.write(i + 17, data[3 + i]);
        }
        for (int i = 0; i < 32; i++) {
          EEPROM.write(i + 24, data[9 + i]);
        }
        hasPassword = true;
        hasSecureKey = true;
        EEPROM.write(16, 1);
        EEPROM.write(23, 1);
        myData[3] = 1;
      }
      preCheck();
      myData[0] = 18;
      myData[1] = cmd;
      myData[2] = myseq;
      EncryptAES(2, 16);
      for (int i = 0; i < 18; i++) {
        Serial.print(myData[i]); Serial.print(" ");
      }
      btSerial.write(myData, 18);

    }

    else if (cmd == TB_RESET_PASSWORD) {
      Serial.println("TB_RESET_PASSWORD!!!!!!!!!");
      if (hasSecureKey) {
        q1 = random(0, 31);
        q2 = random(0, 31);
        q3 = random(0, 31);
        myData[0] = 18;
        myData[1] = cmd;
        myData[2] = myseq;
        myData[3] = q1;
        myData[4] = q2;
        myData[5] = q3;

        EncryptAES(2, 16);
        for (int i = 0; i < 18; i++) {
          Serial.print(myData[i]); Serial.print(" ");
        }
        btSerial.write(myData, 18);
      }
    }

    else if (cmd == TB_RESET_PASSWORD_ANS) {
      Serial.println("TB_RESET_PASSWORD_ANS!!!!!!!!!");
      myData[3] = 0;
      if ((secure_key[q1] == data[3]) && (secure_key[q2] == data[4]) && (secure_key[q3] == data[5])) {
        Serial.println("Right Secure Key ");
        for (int i = 0; i < 6; i++) {
          password[i] = data[i + 3];
          EEPROM.write(i + 17, data[3 + i]);
        }
        myData[3] = 1;
      }
      myData[0] = 18;
      myData[1] = cmd;
      myData[2] = myseq;

      EncryptAES(2, 16);
      for (int i = 0; i < 18; i++) {
        Serial.print(myData[i]); Serial.print(" ");
      }
      btSerial.write(myData, 18);
    }

    else if (cmd == TB_GET_CODE) {
      Serial.println("TB_GET_CODE!!!!!!!!!");
      bool same_password = true;
      if (hasPassword) {
        for (int i = 0; i < 6; i++) {
          if (password[i] != data[i + 3]) {
            same_password = false;
            myData[0] = 18;
            myData[3] = 0;
            break;
          }
        }
        if (same_password) {
          for (int i = 0; i < 32; i++) {
            myData[i + 3] = secure_key[i];
          }
          myData[0] = 34;
        }
      }
      else {
        myData[0] = 18;
        myData[3] = 0;
      }
      myData[1] = cmd;
      myData[2] = myseq;
      if (hasPassword && same_password) {
        EncryptAES(2, 32);
        for (int i = 0; i < 34; i++) {
          Serial.print(myData[i]); Serial.print(" ");
        }
        btSerial.write(myData, 34);
      }
      else {
        EncryptAES(2, 16);
        for (int i = 0; i < 18; i++) {
          Serial.print(myData[i]); Serial.print(" ");
        }
        btSerial.write(myData, 18);
      }
    }

    else if (cmd == TB_OPEN) {
      Serial.println("TB_OPEN!!!!!!!!!");
      if (hasSecureKey) {
        q1 = random(0, 31);
        q2 = random(0, 31);
        q3 = random(0, 31);
        myData[0] = 18;
        myData[1] = cmd;
        myData[2] = myseq;
        myData[3] = q1;
        myData[4] = q2;
        myData[5] = q3;

        EncryptAES(2, 16);
        for (int i = 0; i < 18; i++) {
          Serial.print(myData[i]); Serial.print(" ");
        }

        Serial.println();
        Serial.print("SWSerial: ");
        delay(1000);
        Serial.println(btSerial.write(myData, 18));
        delay(500);
      }
    }

    else if (cmd == TB_OPEN_ANS) {
      Serial.println("TB_OPEN_ANS!!!!!!!!!");
      myData[3] = 0;
      if ((secure_key[q1] == data[3]) && (secure_key[q2] == data[4]) && (secure_key[q3] == data[5])) {
        Serial.println("Right Secure Key ");
        ServoOnOff(135);
        myData[3] = 1;
      }
      myData[0] = 18;
      myData[1] = cmd;
      myData[2] = myseq;
      EncryptAES(2, 16);
      for (int i = 0; i < 18; i++) {
        Serial.print(myData[i]); Serial.print(" ");
      }
      Serial.println();
      Serial.print("SWSerial: ");
      Serial.println(btSerial.write(myData, 18));
    }

    else {
      Serial.println("WRONG DATA");
    }

    receivedAllData = false;
    len = -1;
    for (int j = 0; j < index; j++) {
      data[j] = 0;
    }
    index = 0;

  }


  if (Serial.available()) {
    btSerial.write(Serial.read());

  }

}
