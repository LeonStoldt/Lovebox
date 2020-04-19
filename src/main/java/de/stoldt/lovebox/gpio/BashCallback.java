package de.stoldt.lovebox.gpio;

public interface BashCallback {

    void startDisplay();

    void stopDisplay();

    void refreshPage();

    void startMediaPlayer(String fileUrl);

    void stopMediaPlayer();

    void upgradePackages();

    void rebootSystem();

    void shutdownSystem();

    void disableScreenSaver();
}
