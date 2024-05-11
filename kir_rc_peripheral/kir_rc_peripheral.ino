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

    // For `x`, 1 means "turn right" while -1 means "turn left".
    // For `y`, 1 means "forward", while -1 means... I don't know, that'll probably be refactored.
    int x, y;
    switch(tx) {
        case 0b01: x = 1; break;
        case 0b10: x = -1; break;
        default: x = 0;
    }

    switch(ty) {
        case 0b01: y = 1; break;
        case 0b10: y = -1; break;
        default: y = 0;
    }

    Serial.print("(x: ");
    Serial.print(x);
    Serial.print(", y: ");
    Serial.print(y);
    Serial.println(")");
}
