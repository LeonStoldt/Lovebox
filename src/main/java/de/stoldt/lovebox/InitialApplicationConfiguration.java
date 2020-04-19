package de.stoldt.lovebox;

import de.stoldt.lovebox.gpio.BashExecutor;
import de.stoldt.lovebox.gpio.GpioCallback;
import de.stoldt.lovebox.telegram.MessageCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitialApplicationConfiguration {

    private final GpioCallback gpioCallback;
    private final BashExecutor bashExecutor;
    private final MessageCallback messageCallback;

    @Autowired
    public InitialApplicationConfiguration(GpioCallback gpioCallback, MessageCallback messageCallback, BashExecutor bashExecutor) {
        this.gpioCallback = gpioCallback;
        this.messageCallback = messageCallback;
        this.bashExecutor = bashExecutor;
    }

    @PostConstruct
    public void postConstruct() {
        gpioCallback.setMessageCallback(messageCallback);
        bashExecutor.disableScreenSaver();
        gpioCallback.updateBoxState();
    }
}
