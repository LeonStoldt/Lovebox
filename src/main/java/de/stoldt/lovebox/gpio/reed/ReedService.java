package de.stoldt.lovebox.gpio.reed;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.stoldt.lovebox.gpio.GpioCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ReedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReedService.class);
    private final GpioPinDigitalInput reedContact;

    public ReedService(GpioController gpio, GpioCallback callback) {
        this.reedContact = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "Reed Contact");
        reedContact.addListener(getStateChangeListener(callback));
    }

    private GpioPinListenerDigital getStateChangeListener(GpioCallback callback) {
        return event -> {
            Runtime runtime = Runtime.getRuntime();
            boolean isBoxOpen = event.getState().isLow();

            if (isBoxOpen) {
                stopBlinkingAndShowDisplay(callback, runtime);
            } else {
                turnOffDisplay(runtime);
            }
        };
    }

    private void stopBlinkingAndShowDisplay(GpioCallback callback, Runtime runtime) {
        // -> leds off
        callback.stopLeds();
        try {
            // start display
            runtime.exec("xset -display :0.0 dpms force on ");
            // refresh page
            runtime.exec("midori -e Reload");
        } catch (IOException e) {
            LOGGER.error("Could not execute shell commands", e);
        }
    }

    private void turnOffDisplay(Runtime runtime) {
        try {
            // stop display
            runtime.exec("sleep 1 && xset -display :0.0 dpms force off ");
        } catch (IOException e) {
            LOGGER.error("Could not turn display off", e);
        }
    }
}
