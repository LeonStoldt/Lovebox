package de.stoldt.lovebox.gpio.notification;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.util.concurrent.TimeUnit;

public class LedService {

    private final GpioPinDigitalOutput leds;
    private boolean active;

    public LedService(GpioController gpio) {
        this.active = false;
        this.leds = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Notification LEDs", PinState.LOW);
        leds.setShutdownOptions(true, PinState.LOW);
    }

    public void startBlinking() {
        active = true;
        leds.blink(3, 3, TimeUnit.SECONDS);
    }

    public void stopBlinking() {
        active = false;
        leds.low();
    }

    public boolean isActive() {
        return active;
    }

    public GpioPinDigitalOutput getLeds() {
        return leds;
    }
}
