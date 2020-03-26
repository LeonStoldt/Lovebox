package de.stoldt.lovebox.gpio.notification;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LedService.class);

    private final GpioPinDigitalOutput leds;

    public LedService(GpioController gpio) {
        this.leds = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Notification LEDs", PinState.LOW);
        leds.setShutdownOptions(true, PinState.LOW);
    }

    public void startBlinking() {
        LOGGER.info("Set LED State to HIGH");
        leds.high();
    }

    public void stopBlinking() {
        LOGGER.info("Set LED State to LOW");
        leds.low();
    }

    public boolean isActive() {
        return leds.isHigh();
    }

    public GpioPinDigitalOutput getLeds() {
        return leds;
    }
}
