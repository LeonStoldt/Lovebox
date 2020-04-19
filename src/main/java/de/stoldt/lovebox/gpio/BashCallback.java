package de.stoldt.lovebox.gpio;

public interface BashCallback {

    void startDisplay();

    void stopDisplay();

    void refreshPage();

    void startVideoPlayer(String fileUrl);

    void stopVideoPlayer();

    void upgradePackages();

    void rebootSystem();

    void shutdownSystem();

    void disableScreenSaver();
}
