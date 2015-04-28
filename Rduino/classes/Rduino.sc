
Rduino {
	classvar <pinMode = 0x80;
	classvar <digitalWrite = 0x90;
	classvar <writePort = 0xa0;
	classvar <analogWrite = 0xb0;
	classvar <digitalRead = 0xc0;
	classvar <analogRead = 0xd0;
	classvar <setClock = 0xe0;

	var <baudrate, <port, <sp;
	var <>bits, <>bitCallback; 
	var <>atods, <>atodCallback;
	var <>receiver;
	
	*new{ |aPort, aBaudrate = 115200|
		^super
			.new
			.init(aPort, aBaudrate)
			.atods_(Order.new)
			.bits_(Order.new)
			;
	}
	
	init{ |aPort, aBaudrate|
		port = aPort;
		baudrate = aBaudrate;		
		sp = SerialPort(port, baudrate, crtscts: true);
	}
	
	close{
		sp.close;
	}

	start {
		if (sp.isOpen.not) { 
			// Serial Port can get closed externally
			sp = SerialPort(port, baudrate, crtscts: true) 
		};
		if(receiver.isNil) { receiver = Routine{ loop { this.readAndParse} }; receiver.next }
	}
	
	stop {
		receiver.stop; receiver = nil;
	}
	
	mode_ { | pin, mode |
		sp.put(pinMode);
		sp.put(pin);
		sp.put(mode);
	}
	
	bit_ { | pin, val |
		sp.put(digitalWrite);
		sp.put(pin);
		sp.put(val);
	}

	port_ { | port, val |
		sp.put(writePort);
		sp.put(port);
		sp.put(val);
	}
	
/*
information on pwm clocks
For pins 6 and 5 (OC0A and OC0B):
  TCCR0B = xxxxx001, frequency is 64kHz
  TCCR0B = xxxxx010, frequency is 8 kHz
  TCCR0B = xxxxx011, frequency is 1kHz (this is the default from the Diecimila bootloader)
  TCCR0B = xxxxx100, frequency is 250z
  TCCR0B = xxxxx101, frequency is 62.5 Hz
For pins 9, 10, 11 and 3 (OC1A, OC1B, OC2A, OC2B):
  TCCRnB = xxxxx001, frequency is 32kHz
  TCCRnB = xxxxx010, frequency is 4 kHz
  TCCRnB = xxxxx011, frequency is 500Hz (this is the default from the Diecimila bootloader)
  TCCRnB = xxxxx100, frequency is 125Hz
  TCCRnB = xxxxx101, frequency is 31.25 Hz
*/
	pwClock_ { | pin, power |
		sp.put(setClock);
		sp.put(pin);
		sp.put(power);
	}
	
	pw_ { | pin, val|
		sp.put(analogWrite);
		sp.put(pin);
		sp.put(val);
	}
		
	readBit { | pin |
		sp.put(digitalRead);
		sp.put(pin);
	}
	
	readAnalog { | pin |
		sp.put(analogRead);
		sp.put(pin);
	}
	
		
	readAndParse{
		var message, portNum, pinNum, lsb, msb, value;
		var cmd, pin, val, val2;
		cmd = sp.read;
		switch( cmd,  
			digitalRead, { 
				pin = sp.read; val = sp.read; 
				if (bits.size > pin) {  
					bits[pin] = val;
					bitCallback.value(pin, val); 
				}
			},
			analogRead, { 
				pin = sp.read; val = sp.read; val2 = sp.read;
				if (atods.size > pin){  
					atods[pin] = val + (val2 << 7); 
					atodCallback.value(pin, val); 
				}
			}
		);				
	}
	
}
/*
SerialPort.devices
a = Rduino("/dev/tty.usbserial-A9007PbV", 115200);
(2..13).do{ | i | a.mode_(i,1) };
(2..13).do{ | i | a.bit_(i,0) };
a.bit_(13, 1);
a.bit_(9,1)
a.pw_(9,12)
a.pwClock_(9,5)

a.bit_(13, 0);
a.start
f = { | i, val | [i,val].postln };
a.atodCallbacks = Array.fill(a.atodCallbacks.size, f);
a.readAnalog(1)
a.atods

a.close

*/

/*		
(
a = Rduino("//dev/tty.usbserial-A8008Igr", 115200);
(0..69).do{ | i | a.mode_(i,1) };
(0..69).do{ | i | a.bit_(i,0) };

a.bit_(13, 1)
a.pw_(13, 127)		


a.port_(5, 255)

a.port_(1, 255)
a.port_(3, 255)
a.port_(12, 255)
a.port_(6, 255)
a.port_(11, 255)

		Arduino Mega port assignments:
		PE:		0 - 7
		PH5 - 6:	8 - 9
		PB4 - 7:	10 - 13
		
		PD3 - 0:	18 - 21
		
		PA: 		22 - 29
		PC7 - 0: 	30 - 37
		PD7:		38
		PG2 - 0:	39 - 41
		PL7 - 0:	42 - 49
		PB3 - 0:	50 - 53
		
		PF:		Analog0 - 7
		PK:		Analog8 - 15
		
		have 5 full ports: A, C, L, F, K
		  a  b  c  d  e  f  g  h  j    k   l
		[ 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12 ]
		
		~portOffsets = x 1, 3, 12, 6, 11];
		
*/
/*
ARDUINO CODE FOR CLASS ABOVE
/*
   Simple interface to Arduino.  Since the mapping of port pins to IO
   lines changes between cards, just use the Arduino library's functions:
       digitalRead (pin)
       analogRead(pin)
       digitalWrite(pin, val)
       analogWrite(pin, val)
       
   This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  See file LICENSE.txt for further informations on licensing terms.

  formatted using the GNU C formatting and indenting
*/


/* 
 * TODO: add Servo support using setPinModeCallback(pin, SERVO);
 * TODO: use Program Control to load stored profiles from EEPROM
 */

#include "HardwareSerial.h"

#define SET_PIN_MODE            0x80 // set a pin to INPUT/OUTPUT/PWM/etc
#define WRITE_DIGITAL           0x90 // set a bit
#define WRITE_ANALOG            0xB0 // set a pw
#define READ_DIGITAL            0xC0 // read a bit
#define READ_ANALOG             0xD0 // read an ATOD

int packetCount = 0, cmd, data, value;

/*==============================================================================
 * SETUP()
 *============================================================================*/
void setup() 
{
   Serial.begin(115200);
}

/*
  switch statements cannot be nested within a single function,
  so break the cases out into separate functions
*/

void handleFirstByte() {
   switch(cmd) {
     case READ_ANALOG:
         Serial.print(READ_ANALOG, BYTE);
         Serial.print(value, BYTE);
         value = analogRead(value);
         Serial.print(value & B01111111, BYTE); // LSB
         Serial.print(value >> 7 & B01111111, BYTE); // MSB
         packetCount = 0;
         break;

     case READ_DIGITAL:
         Serial.print(READ_DIGITAL, BYTE);
         Serial.print(value, BYTE);
         value = digitalRead(value);
         Serial.print(value, BYTE); 
         packetCount = 0;
         break;
     default:
         data = value;
         packetCount = 2;
   }
}

void handleSecondByte() {
     packetCount = 0;
      switch(cmd) { 
         case SET_PIN_MODE:
             if (data > 1) {
                 if (value == 0) {
                   pinMode(data,INPUT);
                 } {
                   pinMode(data,OUTPUT);
                 }
             }
             break;
        case WRITE_DIGITAL:
             if (data > 1) {
                 digitalWrite(data, value);
             }
             break;
        case WRITE_ANALOG:
              if (data > 1) {
                analogWrite(data, value << 1);
              }
              break;
       }
 }
/*==============================================================================
 * LOOP()
 *============================================================================*/
void loop()  {  
   while (Serial.available()) {
       value = Serial.read();
       switch(packetCount) {
         case 0:
           cmd = value;
           packetCount = 1; 
           break;
         case 1:
           handleFirstByte();
           break;
         case 2:
           handleSecondByte();
           break;
       }
    }
}

*/