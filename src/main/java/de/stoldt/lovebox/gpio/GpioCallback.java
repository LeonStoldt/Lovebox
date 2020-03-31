package de.stoldt.lovebox.gpio;

public interface GpioCallback {

    void updateBoxState(boolean refreshPage);

    void notifyLeds();

    boolean hasUnreadMessages();

    void setHasUnreadMessages(boolean hasUnreadMessages);
}
