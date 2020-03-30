package de.stoldt.lovebox.gpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class BashExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BashExecutor.class);

    private final Runtime runtime;

    public BashExecutor() {
        this.runtime = Runtime.getRuntime();
    }

    public void startDisplay() {
        try {
            executeCommand("xset -display :0.0 dpms force on");
        } catch (IOException e) {
            LOGGER.warn("Could not start Display:", e);
        }
    }

    public void stopDisplay() {
        try {
            executeCommand("sleep 1 && xset -display :0.0 dpms force off");
        } catch (IOException e) {
            LOGGER.error("Could not turn off display", e);
        }
    }

    public void refreshPage() {
        try {
            executeCommand("xdotool key F5 --window $(xdotool getactivewindow)");
        } catch (IOException e) {
            LOGGER.warn("Could not refresh Page:", e);
        }
    }

    public void upgradePackages() {
        try {
            executeCommand("sudo apt-get update");
            executeCommand("sudo apt-get upgrade -y");
        } catch (IOException e) {
            LOGGER.warn("Could not upgrade packages:", e);
        }
    }

    public void rebootSystem() {
        try {
            executeCommand("sudo reboot");
        } catch (IOException e) {
            LOGGER.warn("Could not reboot system:", e);
        }
    }

    public void shutdownSystem() {
        try {
            executeCommand("sudo shutdown now");
        } catch (IOException e) {
            LOGGER.warn("Could not shutdown system:", e);
        }
    }

    private void executeCommand(String command) throws IOException {
        LOGGER.info("Executing command: {}", command);
        Process process = runtime.exec(command);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        LOGGER.info("Std Input Log:");
        String line;
        while ((line = stdInput.readLine()) != null) {
            LOGGER.info(line);
        }
        while ((line = stdError.readLine()) != null) {
            LOGGER.info(line);
        }
        LOGGER.info("Std Error Log:");

        try {
            int exitCode = process.waitFor();
            LOGGER.info("Finished command {} with exit code {}", command, exitCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Waiting for exit code of command {} threw:", command, e);
        }
    }
}
