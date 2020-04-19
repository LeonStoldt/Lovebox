package de.stoldt.lovebox.mocks;

import de.stoldt.lovebox.gpio.GpioCallback;
import de.stoldt.lovebox.telegram.MessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("dev")
@Service
public class GpioManagerMock implements GpioCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpioManagerMock.class);

    private boolean hasUnreadMessages;

    @Override
    public void setMessageCallback(MessageCallback messageCallback) {
        // not needed in mock
    }

    @Override
    public void updateBoxState() {
        LOGGER.info("updating Box state");
    }

    @Override
    public void notifyLeds() {
        LOGGER.info("notifying leds");
    }

    @Override
    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    @Override
    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }
}
