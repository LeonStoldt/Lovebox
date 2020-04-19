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

    private Process videoProcess;

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
    public void startVideoPlayer(String fileUrl) {
        try {
            videoProcess = executeCommand("omxplayer", "--loop", "-p", "-o", "hdmi", fileUrl);
        } catch (IOException e) {
            LOGGER.warn("Could not Play video by using omxplayer", e);
        }
    }

    @Override
    public void stopVideoPlayer() {
        if (videoProcess != null && videoProcess.isAlive()) {
            videoProcess.destroy();
        }
    }

    public void upgradePackages() {
        try {
            executeCommand("sudo", "apt-get update");
            executeCommand("sudo", "apt-get upgrade -y");
        } catch (IOException e) {
            LOGGER.warn("Could not upgrade packages:", e);
        }
    }

    public void rebootSystem() {
        try {
            executeCommand("sudo", "reboot");
        } catch (IOException e) {
            LOGGER.warn("Could not reboot system:", e);
        }
    }

    public void shutdownSystem() {
        try {
            executeCommand("sudo", "shutdown now");
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
