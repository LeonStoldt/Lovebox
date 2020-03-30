package de.stoldt.lovebox;

import de.stoldt.lovebox.gpio.BashExecutor;
import de.stoldt.lovebox.gpio.GpioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class InitialApplicationConfiguration {

    @PostConstruct
    public void postConstruct(@Autowired GpioManager gpioManager, @Autowired BashExecutor bashExecutor) {
        if (gpioManager.isBoxClosed()) {
            bashExecutor.stopDisplay();
        } else {
            bashExecutor.startDisplay();
            bashExecutor.refreshPage();
        }
    }

}
