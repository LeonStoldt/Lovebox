package de.stoldt.lovebox;

import de.stoldt.lovebox.gpio.GpioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitialApplicationConfiguration {

    private final GpioManager gpioManager;

    @Autowired
    public InitialApplicationConfiguration(GpioManager gpioManager) {
        this.gpioManager = gpioManager;
    }

    @PostConstruct
    public void postConstruct() {
        gpioManager.updateBoxState();
    }

}
