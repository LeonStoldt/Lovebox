package de.stoldt.lovebox.telegram;

import java.util.Arrays;
import java.util.Optional;

public enum Commands {
    REGISTER("/register@Box"),
    UNREGISTER("/unregisterBox"),
    SHUTDOWN("/shutdown"),
    RESTART("/restart"),
    UPDATE("/update");

    private final String command;

    Commands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static Optional<Commands> of(String text) {
        return Arrays
                .stream(values())
                .filter(command -> command.getCommand().equals(text))
                .findFirst();
    }
}
