package de.stoldt.lovebox.gpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BashExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BashExecutor.class);

    public void startDisplay() {
        try {
            Process waitingProcess = executeCommand("sleep", "2");
            waitingProcess.waitFor();
            executeCommand("xset", "dpms", "force", "on");
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Could not start Display:", e);
        }
    }

    public void stopDisplay() {
        try {
            Process waitingProcess = executeCommand("sleep", "1");
            waitingProcess.waitFor();
            executeCommand("xset", "dpms", "force", "off");
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

    public void startBrowser() {
        try {
            executeCommand("startx");
        } catch (IOException e) {
            LOGGER.warn("Could not start xserver by calling .xinitrc file:", e);
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

    private Process executeCommand(String... commandAndArguments) throws IOException {
        String command = String.join(" ", commandAndArguments);
        LOGGER.info("Executing command: {}", command);

        ProcessBuilder processBuilder = new ProcessBuilder(commandAndArguments);
        processBuilder.environment().putIfAbsent("DISPLAY", ":0");
        processBuilder.environment().putIfAbsent("XAUTHORITY", "/root/.Xauthority");
        return processBuilder.start();
    }
}
