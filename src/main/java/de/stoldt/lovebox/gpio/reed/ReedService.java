package de.stoldt.lovebox.gpio.reed;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.stoldt.lovebox.gpio.BashExecutor;
import de.stoldt.lovebox.gpio.GpioCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReedService.class);

    private final BashExecutor bashExecutor;
    private final GpioPinDigitalInput reedContact;

    public ReedService(BashExecutor bashExecutor, GpioController gpio, GpioCallback callback) {
        this.bashExecutor = bashExecutor;
        reedContact = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "Reed Contact");
        reedContact.addListener(getStateChangeListener(callback));
    }

    public Boolean isClosed() {
        return reedContact.isHigh();
    }

    private GpioPinListenerDigital getStateChangeListener(GpioCallback callback) {
        return event -> {
            boolean isBoxOpen = event.getState().isLow();
            LOGGER.info("State on Pin {} changed to {} - Box is {}",
                    event.getPin(),
                    event.getState().getName(),
                    event.getState() == PinState.HIGH
                            ? "CLOSED"
                            : "OPENED");
            if (isBoxOpen) {
                stopBlinkingAndShowDisplay(callback);
            } else {
                turnOffDisplay();
            }
        };
    }

    private void stopBlinkingAndShowDisplay(GpioCallback callback) {
        callback.stopLeds();
        LOGGER.info("Starting Display...");
        bashExecutor.startDisplay();
        LOGGER.info("Refreshing Page...");
        bashExecutor.refreshPage();
    }

    private void turnOffDisplay() {
        LOGGER.info("Turning off Display...");
        bashExecutor.stopDisplay();
    }
}
