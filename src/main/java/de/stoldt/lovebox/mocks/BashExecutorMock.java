package de.stoldt.lovebox.mocks;

import de.stoldt.lovebox.gpio.BashCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class BashExecutorMock implements BashCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(BashExecutorMock.class);

    @Override
    public void startDisplay() {
        LOGGER.info("starting display");
    }

    @Override
    public void stopDisplay() {
        LOGGER.info("stopping display");
    }

    @Override
    public void refreshPage() {
        LOGGER.info("refreshing page");
    }

    @Override
    public void startMediaPlayer(String fileUrl) {
        LOGGER.info("starting video: {}", fileUrl);
    }

    @Override
    public void stopMediaPlayer() {
        LOGGER.info("stopped video, if playing");
    }

    @Override
    public void upgradePackages() {
        LOGGER.info("upgrading packages");
    }

    @Override
    public void rebootSystem() {
        LOGGER.info("rebooting system");
    }

    @Override
    public void shutdownSystem() {
        LOGGER.info("shutdown system");
    }

    @Override
    public void disableScreenSaver() {
        LOGGER.info("starting display");
    }
}
