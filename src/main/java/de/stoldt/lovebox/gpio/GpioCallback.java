package de.stoldt.lovebox.gpio;

public interface GpioCallback {

    void updateBoxState();

    void notifyLeds();

    boolean hasUnreadMessages();

    void setHasUnreadMessages(boolean hasUnreadMessages);
}
