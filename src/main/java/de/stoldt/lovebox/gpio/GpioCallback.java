package de.stoldt.lovebox.gpio;

import de.stoldt.lovebox.telegram.MessageCallback;

public interface GpioCallback {

    void setMessageCallback(MessageCallback messageCallback);

    void updateBoxState();

    void notifyLeds();

    boolean hasUnreadMessages();

    void setHasUnreadMessages(boolean hasUnreadMessages);
}
