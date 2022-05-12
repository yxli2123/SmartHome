/*
 * switch.ino
 *
 *  Created on: 2020-05-15
 *      Author: Mixiaoxiao (Wang Bin)
 *  Modified on: 2021-05-17
 *      Modifier: Josh Spicer <hello@joshspicer.com>
 */

#include <Arduino.h>
#include <arduino_homekit_server.h>
#include "wifi_info.h"

#define LOG_D(fmt, ...)   printf_P(PSTR(fmt "\n") , ##__VA_ARGS__);

void setup() {
  Serial.begin(115200);
  wifi_connect(); // in wifi_info.h
  //homekit_storage_reset(); // to remove the previous HomeKit pairing storage when you first run this new HomeKit example
  my_homekit_setup();
}

void loop() {
  my_homekit_loop();
  delay(10);
}

//==============================
// HomeKit setup and loop
//==============================

// access your HomeKit characteristics defined in my_accessory.c
extern "C" homekit_server_config_t config;

extern "C" homekit_characteristic_t red_led;
extern "C" homekit_characteristic_t green_led;
extern "C" homekit_characteristic_t yellow_led;

static uint32_t next_heap_millis = 0;

#define PIN_SWITCH 2
#define LED_RED 15
#define LED_YELLOW 3
#define LED_GREEN 1

//Called when the switch value is changed by iOS Home APP
void green_led_setter(const homekit_value_t value) {
  bool on = value.bool_value;
  green_led.value.bool_value = on;  //sync the value
  LOG_D("Green Switch: %s", on ? "ON" : "OFF");
  digitalWrite(LED_GREEN, on ? HIGH : LOW);
}

//Called when the switch value is changed by iOS Home APP
void red_led_setter(const homekit_value_t value) {
  bool on = value.bool_value;
  red_led.value.bool_value = on;  //sync the value
  LOG_D("Red Switch: %s", on ? "ON" : "OFF");
  digitalWrite(LED_RED, on ? HIGH : LOW);
}

//Called when the switch value is changed by iOS Home APP
void yellow_led_setter(const homekit_value_t value) {
  bool on = value.bool_value;
  red_led.value.bool_value = on;  //sync the value
  LOG_D("Yellow Switch: %s", on ? "ON" : "OFF");
  digitalWrite(LED_YELLOW, on ? HIGH : LOW);
}

void my_homekit_setup() {
  pinMode(PIN_SWITCH, OUTPUT);
  digitalWrite(PIN_SWITCH, HIGH);

  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);
  pinMode(LED_RED, OUTPUT);

  red_led.setter = red_led_setter;
  yellow_led.setter = yellow_led_setter;
  green_led.setter = green_led_setter;
  arduino_homekit_setup(&config);
}

void my_homekit_loop() {
  arduino_homekit_loop();
  const uint32_t t = millis();
  if (t > next_heap_millis) {
    // show heap info every 5 seconds
    next_heap_millis = t + 5 * 1000;
    LOG_D("Free heap: %d, HomeKit clients: %d",
        ESP.getFreeHeap(), arduino_homekit_connected_clients_count());

  }
}
