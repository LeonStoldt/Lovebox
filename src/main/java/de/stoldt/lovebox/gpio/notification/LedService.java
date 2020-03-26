package de.stoldt.lovebox.gpio.notification;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LedService.class);
    private static final Pin GPIO_PIN = RaspiPin.GPIO_01;

    private final GpioPinDigitalOutput leds;

    public LedService(GpioController gpio) {
        this.leds = gpio.provisionDigitalOutputPin(GPIO_PIN, "Notification LEDs", PinState.LOW);
        leds.setShutdownOptions(true, PinState.LOW);

    }

    public void startBlinking() {
        LOGGER.info("Set LED State on Pin {} to PULSE", GPIO_PIN.getName());
        leds.blink(1000L, 3000L);
    }

    public void stopBlinking() {
        LOGGER.info("Set LED State on Pin {} to LOW", GPIO_PIN.getName());
        leds.low();
    }

    public boolean isActive() {
        return leds.isHigh();
    }

    public GpioPinDigitalOutput getLeds() {
        return leds;
    }
}
