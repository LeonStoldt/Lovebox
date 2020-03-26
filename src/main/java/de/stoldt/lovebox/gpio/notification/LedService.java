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
    private static final int PWM_ON = 50;
    private static final int PWM_OFF = 0;

    private final GpioPinPwmOutput leds;

    public LedService(GpioController gpio) {
        this.leds = gpio.provisionPwmOutputPin(GPIO_PIN, "Notification LEDs", 0);
        leds.setShutdownOptions(true, PinState.LOW);
    }

    public void startBlinking() {
        LOGGER.info("Set LED State on PWM Pin {} to {}", GPIO_PIN.getName(), PWM_ON);
        leds.setPwm(PWM_ON);
    }

    public void stopBlinking() {
        LOGGER.info("Set LED State on PWM Pin {} to {}", GPIO_PIN.getName(), PWM_OFF);
        leds.setPwm(PWM_OFF);
    }

    public boolean isActive() {
        return leds.getPwm() > PWM_OFF;
    }

    public GpioPinPwmOutput getLeds() {
        return leds;
    }
}
