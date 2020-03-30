package de.stoldt.lovebox.gpio.reed;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.stoldt.lovebox.gpio.GpioCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReedService.class);

    private final GpioPinDigitalInput reedContact;
    private final GpioCallback gpioCallback;

    public ReedService(GpioController gpio, GpioCallback callback) {
        this.reedContact = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "Reed Contact");
        this.gpioCallback = callback;
        reedContact.addListener(getStateChangeListener());
    }

    public boolean isClosed() {
        return reedContact.isHigh();
    }

    private GpioPinListenerDigital getStateChangeListener() {
        return event -> {
            LOGGER.info("State on Pin {} changed to {} - Box is {}",
                    event.getPin(),
                    event.getState().getName(),
                    event.getState() == PinState.HIGH ? "CLOSED" : "OPENED");
            gpioCallback.updateBoxState();
        };
    }
}
