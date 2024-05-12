#include <SoftwareSerial.h>

enum Pins {
    BT_RECEIVE = 2,
    BT_TRANSMIT = 3,

    RIGHT_BACKWARD = 4,
    RIGHT_FORWARD = 5,
    LEFT_FORWARD = 6,
    LEFT_BACKWARD = 7
};

struct Config {
    static Config configs[9][2];

    enum Direction {
        FORWARD = 0,
        BACKWARD = 1
    } dir;

    enum Speed {
        NONE = 0,
        SLOW = 127,
        FAST = 255
    } speed;

    void flush(Pins forward, Pins backward) const {
        if(dir == BACKWARD) {
            // Swap pins if this configuration is backwards.
            Pins tmp = forward;
            forward = backward;
            backward = tmp;
        }

        analogWrite(forward, speed);
        analogWrite(backward, NONE);
    }
};

static Config Config::configs[9][2] = {
    // (0, 0): Stay still.
    {{FORWARD, NONE}, {FORWARD, NONE}},
    // (0, 1): Forward.
    {{FORWARD, FAST}, {FORWARD, FAST}},
    // (0, 2): Backward.
    {{BACKWARD, FAST}, {BACKWARD, FAST}},
    // (1, 0): Turn right completely.
    {{FORWARD, FAST}, {FORWARD, NONE}},
    // (1, 1): TUrn right slightly.
    {{FORWARD, FAST}, {FORWARD, SLOW}},
    // (1, 2): Turn right backwards.
    {{BACKWARD, FAST}, {BACKWARD, SLOW}},
    // (2, 0): Turn left completely.
    {{FORWARD, NONE}, {FORWARD, FAST}},
    // (2, 1): Turn left slightly.
    {{FORWARD, SLOW}, {FORWARD, FAST}},
    // (2, 2): Turn left backwards.
    {{BACKWARD, SLOW}, {BACKWARD, FAST}}
};

SoftwareSerial BtSerial(BT_RECEIVE, BT_TRANSMIT);

void setup() {
    BtSerial.begin(38400);
    Serial.begin(9600);

    pinMode(LEFT_FORWARD, OUTPUT);
    pinMode(LEFT_BACKWARD, OUTPUT);
    pinMode(RIGHT_FORWARD, OUTPUT);
    pinMode(RIGHT_BACKWARD, OUTPUT);
}

void loop() {
    if(BtSerial.available() < 1) return;

    // For `x`, 0b01 means "turn right" while 0b10 means "turn left".
    // For `y`, 0b01 means "forward", while 0b10 means "backward".
    int packet = BtSerial.read();
    int tx = packet & 0b0011, ty = (packet & 0b1100) >> 2;
    
    Config *pair = Config::configs[tx * 3 + ty];
    pair[0].flush(LEFT_FORWARD, LEFT_BACKWARD);
    pair[1].flush(RIGHT_FORWARD, RIGHT_BACKWARD);
}
