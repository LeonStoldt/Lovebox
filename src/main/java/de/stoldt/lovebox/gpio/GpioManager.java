package de.stoldt.lovebox.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import de.stoldt.lovebox.gpio.notification.LedService;
import de.stoldt.lovebox.gpio.reed.ReedService;
import de.stoldt.lovebox.telegram.MessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("!dev")
@Service
public class GpioManager implements GpioCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpioManager.class);

    private final LedService leds;
    private final ReedService reed;
    private final BashCallback bashCallback;
    private MessageCallback messageCallback;
    private boolean hasUnreadMessages;

    @Autowired
    public GpioManager(BashCallback bashCallback) {
        GpioController controller = GpioFactory.getInstance();
        this.leds = new LedService(controller);
        this.reed = new ReedService(controller, this);
        this.bashCallback = bashCallback;
        this.hasUnreadMessages = false;
    }

    @Override
    public void setMessageCallback(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    @Override
    public void updateBoxState() {
        if (reed.isClosed()) {
            if (hasUnreadMessages()) {
                LOGGER.info("Starting Leds in view of unread messages...");
                notifyLeds();
            }
            bashCallback.stopMediaPlayer();
            LOGGER.info("Turning off Display...");
            bashCallback.stopDisplay();
        } else {
            LOGGER.info("Refreshing Page...");
            bashCallback.refreshPage();
            leds.stopPulsing();
            messageCallback.sendConfirmation();
            LOGGER.info("Starting Display...");
            bashCallback.startDisplay();
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
