package de.stoldt.lovebox.gpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class BashExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BashExecutor.class);

    public void startDisplay() {
        try {
            executeCommand("xset -display :0.0", "dpms force on");
        } catch (IOException e) {
            LOGGER.warn("Could not start Display:", e);
        }
    }

    public void stopDisplay() {
        try {
            executeCommand("sleep", "1");
            executeCommand("xset -display :0.0", "dpms force off");
        } catch (IOException e) {
            LOGGER.error("Could not turn off display", e);
        }
    }

    public void refreshPage() {
        try {
            String windowId = executeCommand("xdotool", "getactivewindow").get(0);
            executeCommand("xdotool", "key", "F5", "--window", windowId);
        } catch (IOException e) {
            LOGGER.warn("Could not refresh Page:", e);
        }
    }

    public void startBrowser() {
        try {
            executeCommand("startx");
        } catch (IOException e) {
            LOGGER.warn("Could not start browser by calling .xinitrc file:", e);
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

    private List<String> executeCommand(String... commandAndArguments) throws IOException {
        List<String> result = new ArrayList<>();
        String command = String.join(" ", commandAndArguments);
        LOGGER.info("Executing command: {}", command);

        ProcessBuilder processBuilder = new ProcessBuilder(commandAndArguments);
        processBuilder.environment().putIfAbsent("DISPLAY", ":0.0");
        processBuilder.environment().putIfAbsent("XAUTHORITY", "/root/.Xauthority");
        Process process = processBuilder.start();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        LOGGER.info("Std Input Log:");
        String line;
        while ((line = stdInput.readLine()) != null) {
            result.add(line);
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
        return result;
    }
}
