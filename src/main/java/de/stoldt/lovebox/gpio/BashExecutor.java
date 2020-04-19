package de.stoldt.lovebox.gpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Profile("!dev")
@Component
public class BashExecutor implements BashCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(BashExecutor.class);

    private Process mediaProcess;

    public void startDisplay() {
        setDisplay(true);
    }

    public void stopDisplay() {
        setDisplay(false);
    }

    private void setDisplay(boolean displayOn) {
        String status = displayOn ? "on" : "off";
        try {
            Process waitingProcess = executeCommand("sleep", "1");
            waitingProcess.waitFor();
            executeCommand("xset", "dpms", "force", status);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Could not turn off display", e);
        }
    }

    public void refreshPage() {
        try {
            executeCommand("xdotool", "key", "F5");
        } catch (IOException e) {
            LOGGER.warn("Could not refresh Page:", e);
        }
    }

    @Override
    public void startMediaPlayer(String fileUrl) {
        try {
            mediaProcess = executeCommand("omxplayer", "--loop", "-p", "-o", "hdmi", fileUrl);
        } catch (IOException e) {
            LOGGER.warn("Could not Play media by using omxplayer", e);
        }
    }

    @Override
    public void stopMediaPlayer() {
        if (mediaProcess != null && mediaProcess.isAlive()) {
            try {
                executeCommand("pkill", "omxplayer");
            } catch (IOException e) {
                LOGGER.warn("Could not kill omxplayer:", e);
            }
        }
    }

    public void upgradePackages() {
        try {
            executeCommand("apt-get", "update");
            executeCommand("apt-get", "upgrade -y");
        } catch (IOException e) {
            LOGGER.warn("Could not upgrade packages:", e);
        }
    }

    public void rebootSystem() {
        try {
            executeCommand("reboot");
        } catch (IOException e) {
            LOGGER.warn("Could not reboot system:", e);
        }
    }

    public void shutdownSystem() {
        try {
            executeCommand("shutdown");
        } catch (IOException e) {
            LOGGER.warn("Could not shutdown system:", e);
        }
    }

    public void disableScreenSaver() {
        try {
            executeCommand("xset", "-dpms");
            executeCommand("xset", "s", "off");
            executeCommand("xset", "s", "noblank");
        } catch (IOException e) {
            LOGGER.warn("Could not disable screen saver features:", e);
        }
    }

    private Process executeCommand(String... commandAndArguments) throws IOException {
        String command = String.join(" ", commandAndArguments);
        LOGGER.info("Executing command: {}", command);

        ProcessBuilder processBuilder = new ProcessBuilder(commandAndArguments);
        processBuilder.environment().putIfAbsent("DISPLAY", ":0");
        processBuilder.environment().putIfAbsent("XAUTHORITY", "/root/.Xauthority");
        return processBuilder.start();
    }
}
