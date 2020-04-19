package de.stoldt.lovebox.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import de.stoldt.lovebox.gpio.notification.LedService;
import de.stoldt.lovebox.gpio.reed.ReedService;
import de.stoldt.lovebox.telegram.MessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

public class GpioManager implements GpioCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpioManager.class);

    private final LedService leds;
    private final ReedService reed;
    private final BashExecutor bashExecutor;
    private final MessageCallback messageCallback;
    private boolean hasUnreadMessages;

    public GpioManager(@NotNull BashExecutor bashExecutor, @NotNull MessageCallback messageCallback) {
        GpioController controller = GpioFactory.getInstance();
        this.leds = new LedService(controller);
        this.reed = new ReedService(controller, this);
        this.bashExecutor = bashExecutor;
        this.messageCallback = messageCallback;
        this.hasUnreadMessages = false;
    }

    @Override
    public void updateBoxState() {
        if (reed.isClosed()) {
            if (hasUnreadMessages()) {
                LOGGER.info("Starting Leds in view of unread messages...");
                notifyLeds();
            }
            LOGGER.info("Turning off Display...");
            bashExecutor.stopDisplay();
        } else {
            LOGGER.info("Refreshing Page...");
            bashExecutor.refreshPage();
            leds.stopPulsing();
            messageCallback.sendConfirmation();
            LOGGER.info("Starting Display...");
            bashExecutor.startDisplay();
        }
    }

    @Override
    public void notifyLeds() {
        if (!leds.isActive() && reed.isClosed()) {
            leds.startPulsing();
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
