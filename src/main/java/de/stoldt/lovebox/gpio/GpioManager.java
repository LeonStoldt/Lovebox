package de.stoldt.lovebox.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import de.stoldt.lovebox.gpio.notification.LedService;
import de.stoldt.lovebox.gpio.reed.ReedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class GpioManager implements GpioCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpioManager.class);

    private final LedService leds;
    private final ReedService reed;
    private final BashExecutor bashExecutor;
    private boolean hasUnreadMessages;

    @Autowired
    public GpioManager(BashExecutor bashExecutor) {
        GpioController controller = GpioFactory.getInstance();
        this.leds = new LedService(controller);
        this.reed = new ReedService(controller, this);
        this.bashExecutor = bashExecutor;
        this.hasUnreadMessages = false;
    }

    @Override
    public void updateBoxState(boolean refreshPage) {
        if (reed.isClosed()) {
            if (hasUnreadMessages()) {
                LOGGER.info("Starting Leds in view of unread messages...");
                notifyLeds();
            }
            LOGGER.info("Turning off Display...");
            bashExecutor.stopDisplay();
        } else {
            leds.stopBlinking();
            LOGGER.info("Starting Display...");
            bashExecutor.startDisplay();
            if (refreshPage) {
                LOGGER.info("Refreshing Page...");
                bashExecutor.refreshPage();
            }
        }
    }

    @Override
    public void notifyLeds() {
        if (!leds.isActive() && reed.isClosed()) {
            leds.startBlinking();
        }
    }

    @Override
    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }
}
