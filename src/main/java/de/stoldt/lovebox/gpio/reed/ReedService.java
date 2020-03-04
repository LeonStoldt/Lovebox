package de.stoldt.lovebox.gpio.reed;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.stoldt.lovebox.gpio.GpioCallback;

import java.io.IOException;

public class ReedService {

    private final GpioPinDigitalInput reedContact;

    public ReedService(GpioController gpio, GpioCallback callback) {
        this.reedContact = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "Reed Contact");
        reedContact.addListener(getStateChangeListener(callback));
    }

    private GpioPinListenerDigital getStateChangeListener(GpioCallback callback) {
        return event -> {
            if (event.getState().isLow()) {
                // -> leds off
                callback.stopLeds();
                // -> start display
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("");
                } catch (IOException e) {
                    //log case
                }
                // -> refresh page


            } else {
                // -> stop display
            }
        };
    }
}
