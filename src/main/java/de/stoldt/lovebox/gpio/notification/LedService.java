package de.stoldt.lovebox.gpio.notification;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class LedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LedService.class);
    private static final Pin GPIO_PIN = RaspiPin.GPIO_01;
    private static final int RANGE_LED_ON = 100;
    private static final int DELAY = 10;
    private static final int RANGE_LED_OFF = 0;
    public static final long LED_ON_TIME = 500L;
    public static final long LED_OFF_TIME = 500L;

    private final GpioPinPwmOutput leds;
    private Thread pwmThread;
    private final AtomicBoolean running;

    public LedService(GpioController gpio) {
        running = new AtomicBoolean(false);
        this.leds = gpio.provisionPwmOutputPin(GPIO_PIN);
        leds.setPwmRange(RANGE_LED_ON);
        leds.setShutdownOptions(true, PinState.LOW);
    }

    public void startPulsing() {
        LOGGER.info("Start pulsing Pin {} with PWM", GPIO_PIN.getName());
        pwmThread = new Thread(this::pulse, "LED Pulse Thread");
        pwmThread.start();
        running.set(true);
    }

    private void pulse() {
        while (running.get()) {
            try {
                turnLedOn();
                Thread.sleep(LED_ON_TIME);
                turnLedOff();
                Thread.sleep(LED_OFF_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.debug("caught InterruptedException while pulsing LEDs", e);
            }
        }
    }

    private void turnLedOff() throws InterruptedException {
        for (int value = RANGE_LED_ON; value >= RANGE_LED_OFF; value--) {
            leds.setPwm(value);
            Thread.sleep(DELAY);
        }
    }

    private void turnLedOn() throws InterruptedException {
        for (int value = RANGE_LED_OFF; value <= RANGE_LED_ON; value++) {
            leds.setPwm(value);
            Thread.sleep(DELAY);
        }
    }

    public void stopPulsing() {
        LOGGER.info("Stop pulsing Pin {} with PWM", GPIO_PIN.getName());
        running.set(false);
        leds.setPwm(RANGE_LED_OFF);
    }

    public boolean isActive() {
        return pwmThread != null && pwmThread.isAlive();
    }
}
