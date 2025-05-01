# jFuncGen

jFuncGen is a Java GUI for the MHS-5200 and FY6900 function generators.
It should work with all operating systems with a Java 17 runtime (or newer) and is supported by [jSerialComm](https://fazecast.github.io/jSerialComm/).

## System requirements

Java 17 runtime or newer. Download [OpenJDK](http://adoptopenjdk.net/) or [Oracle JDK](http://java.oracle.com).

## Supported function generators

### MHS-5200
![jfunc5200](https://github.com/user-attachments/assets/dab51a97-57f2-4c5b-a644-3dbb2cb3db44)

### FY6900
![fy6900](https://github.com/user-attachments/assets/a89edf51-44c2-41a4-888a-a1b79b7982e7)

## Usage

### Configuration tab

![image](https://github.com/user-attachments/assets/a6b240b4-5e04-4431-aab3-585f67030e1a)

Select *Device type* and the *Serial port* it is connected to, and then press *Connect* and you should be ready.
The *Model*, *Product* and *Firmware* shows theese details when successfully connected.
If the device was not connected when jFuncGen was started, press *Refresh* to rescan USB for the device.
You can also change *Font size* and *Look* according to your personal preferences.

### General tab

![image](https://github.com/user-attachments/assets/429e61f4-23f8-4467-89f3-65d1c9231fed)

The basic usage details. *Wave form*, *Frequency* etc. The jog dial and the *Frequency* field supports using the mouse wheel to increase/decrease the frequency.
The - and + buttons near the jog dial increase/decrease how fst the mouse wheel modifies the value.

### Advanced Tab

![image](https://github.com/user-attachments/assets/53a0d576-35e9-4b3a-9aa3-08cf9a522f38)

The measure and sweep setup etc.

### Arbitrary tab

![image](https://github.com/user-attachments/assets/16fe096b-da81-4bdf-ba64-48100333f628)

The configuraton of arbitrary wave forms. A couple of preset functions are available as well as a expressions field.
I have not been able to find out how read the wave from from a FY6900 function generator. Writing to the function generator works though.

### Console tab

![image](https://github.com/user-attachments/assets/bd12c787-eabc-4c5e-9eeb-a3cacb4222c6)

The serial communication between the computer and the function generator. You can also enter and send commands.
