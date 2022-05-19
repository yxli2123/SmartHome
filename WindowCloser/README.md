# Window Closer

We propose a window closer that can close the window remotely through HomeKit.

## Usage
- Change the SSID and the password in `WindowCloser/wifi_info.h` and load the code to the ESP8266, or change the Wi-Fi SSID to `HomeTest` and the password to `hometest123456`
- Pull the gear on the master suction cup to stick on a window. Stick the slaver suction on another window.
- Turn on the window closer

## Introduction to the Project
It has three parts: Circuit and PCB Design, Shell Design, and MCU Program

Circuit and PCB Design shows the most magical details of this device. It involes a motor, a dirve for the moter, a power manager, and the ESP8266 MCU.

Shell Design shows how we organize the suction cup, the pulley, and the circuit.

MCU Program shows how to HomeKit can be used to control the device.
