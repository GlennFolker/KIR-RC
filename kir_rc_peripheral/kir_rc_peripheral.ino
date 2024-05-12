#include <SoftwareSerial.h>

enum Pins {
    BT_TRANSMIT = 5,
    BT_RECEIVE = 3
};

SoftwareSerial BtSerial(Pins::BT_RECEIVE, Pins::BT_TRANSMIT);

void setup() {
    BtSerial.begin(38400);
    Serial.begin(9600);
}

void loop() {
    if(BtSerial.available() < 1) return;

    int packet = BtSerial.read();
    int tx = packet & 0b0011, ty = (packet & 0b1100) >> 2;

    // For `x`, 0b01 means "turn right" while 0b10 means "turn left".
    // For `y`, 0b01 means "forward", while 0b10 means "backward".
}
