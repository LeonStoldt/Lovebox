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
    private boolean hasUnreadMessages;

    public GpioManager(BashExecutor bashExecutor) {
        GpioController controller = GpioFactory.getInstance();
        this.leds = new LedService(controller);
        this.reed = new ReedService(bashExecutor, controller, this);
        this.hasUnreadMessages = false;
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

    @Override
    public boolean isBoxClosed() {
        return reed.isClosed();
    }

    @Override
    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }
}
