package de.stoldt.lovebox;

import java.util.UUID;

public class TokenGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
