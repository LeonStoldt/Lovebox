package de.stoldt.lovebox.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import de.stoldt.lovebox.gpio.notification.LedService;
import de.stoldt.lovebox.gpio.reed.ReedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class GpioManager implements GpioCallback {

    private final Logger LOGGER = LoggerFactory.getLogger(GpioManager.class);

    private final LedService leds;
    private final ReedService reed;

    public GpioManager(BashExecutor bashExecutor) {
        GpioController controller = GpioFactory.getInstance();
        this.leds = new LedService(controller);
        this.reed = new ReedService(bashExecutor, controller, this);
    }

    @Override
    public boolean ledsAreActive() {
        return leds.isActive();
    }

    @Override
    public void startLeds() {
        LOGGER.info("Starting LEDs...");
        leds.startBlinking();
    }

    @Override
    public void stopLeds() {
        LOGGER.info("Stopping LEDs...");
        leds.stopBlinking();
    }
}
