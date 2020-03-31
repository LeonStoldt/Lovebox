package de.stoldt.lovebox;

import de.stoldt.lovebox.gpio.BashExecutor;
import de.stoldt.lovebox.gpio.GpioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitialApplicationConfiguration {

    private final GpioManager gpioManager;
    private final BashExecutor bashExecutor;

    @Autowired
    public InitialApplicationConfiguration(GpioManager gpioManager, BashExecutor bashExecutor) {
        this.gpioManager = gpioManager;
        this.bashExecutor = bashExecutor;
    }

    @PostConstruct
    public void postConstruct() {
        bashExecutor.startBrowser();
        gpioManager.updateBoxState(false);
    }

}
