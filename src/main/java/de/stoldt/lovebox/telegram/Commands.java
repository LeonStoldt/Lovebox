package de.stoldt.lovebox.telegram;

import java.util.Arrays;

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

    public static boolean isKnownCommand(String command) {
        return Arrays
                .stream(values())
                .map(Commands::getCommand)
                .anyMatch(e -> e.equals(command));
    }
}
