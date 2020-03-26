package de.stoldt.lovebox.gpio;

public interface GpioCallback {

    boolean ledsAreActive();

    void startLeds();

    void stopLeds();

    boolean isBoxClosed();
}
