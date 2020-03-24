package de.stoldt.lovebox.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import de.stoldt.lovebox.gpio.notification.LedService;
import de.stoldt.lovebox.gpio.reed.ReedService;
import org.springframework.stereotype.Controller;

@Controller
public class GpioManager implements GpioCallback {

    private final LedService leds;
    private final ReedService reed;

    public GpioManager() {
        GpioController controller = GpioFactory.getInstance();
        this.leds = new LedService(controller);
        this.reed = new ReedService(controller, this);
    }

    @Override
    public boolean ledsAreActive() {
        return leds.isActive();
    }

    @Override
    public void startLeds() {
        leds.startBlinking();
    }

    @Override
    public void stopLeds() {
        leds.stopBlinking();
    }
}
