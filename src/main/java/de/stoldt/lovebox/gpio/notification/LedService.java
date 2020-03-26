package de.stoldt.lovebox.gpio.notification;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LedService.class);
    private static final Pin GPIO_PIN = RaspiPin.GPIO_01;

    private final GpioPinPwmOutput leds;

    public LedService(GpioController gpio) {
        this.leds = gpio.provisionPwmOutputPin(GPIO_PIN, "Notification LEDs", 0);
        leds.setShutdownOptions(true, PinState.LOW);
    }

    public void startBlinking() {
        LOGGER.info("Set LED State on Pin {} to HIGH", GPIO_PIN.getName());
        leds.setPwm(500);
    }

    public void stopBlinking() {
        LOGGER.info("Set LED State on Pin {} to LOW", GPIO_PIN.getName());
        leds.setPwm(0);
    }

    public boolean isActive() {
        return leds.getPwm() > 0;
    }

    public GpioPinPwmOutput getLeds() {
        return leds;
    }
}
