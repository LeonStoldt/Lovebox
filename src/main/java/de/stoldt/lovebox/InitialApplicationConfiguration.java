package de.stoldt.lovebox;

import de.stoldt.lovebox.gpio.BashCallback;
import de.stoldt.lovebox.gpio.GpioCallback;
import de.stoldt.lovebox.telegram.MessageCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitialApplicationConfiguration {

    private final GpioCallback gpioCallback;
    private final BashCallback bashCallback;
    private final MessageCallback messageCallback;

    @Autowired
    public InitialApplicationConfiguration(GpioCallback gpioCallback, MessageCallback messageCallback, BashCallback bashCallback) {
        this.gpioCallback = gpioCallback;
        this.messageCallback = messageCallback;
        this.bashCallback = bashCallback;
    }

    @PostConstruct
    public void postConstruct() {
        gpioCallback.setMessageCallback(messageCallback);
        bashCallback.disableScreenSaver();
        gpioCallback.updateBoxState();
    }
}
