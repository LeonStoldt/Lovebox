# WIP Project

## Pi installation
-   download the dietPi image from the official website: [dietpi.com/downloads/images/DietPi_RPi-ARMv6-Buster.7z](https://dietpi.com/downloads/images/DietPi_RPi-ARMv6-Buster.7z)
-   unzip the file e.g. with 7zip to extract the image
-   download balenaEtcher from the official website [balena.io/etcher/](https://www.balena.io/etcher/) and open it
-   select the image `DietPi_RPi-ARMv6-Buster.img` in the first step and select the plugged in sd card as target in the second step
-   click "Flash!" and wait until it is finished
-   after that, you might need to remove the sd card and plug it in again (if the system ejected the sd card after flashing)
-   copy and replace the files `pi-config/config.txt` and `pi-config/dietpi.txt` into sd root folder
-   fill in the following variables in line 34 and 36 of the dietpi.txt:
```
AUTO_SETUP_NET_STATIC_IP=<AVAILABLE IP ADDRESS IN YOUR NETWORK>
[...]
AUTO_SETUP_NET_STATIC_GATEWAY=<IP ADDRESS OF YOUR ROUTER>
```
-   you might change the keyboard layout settings by replacing "de" with your country code and change the wifi code and time zone
-   plug the sd card to your pi and start it
-   on a console, you can run `ping -l <YOUR CHOSEN IP ADDRESS OF THE PI>` to check when the pi is available
-   start putty to connect to your pi via ssh with your chosen IP address of the pi and default port 22
-   default username is `pi` and password is `dietpi`
-   you need to agree the license and you should see the installation process starting
-   it is recommended to change passwords (you will be asked for it)
-   the serial console can be disabled
-   reaching the dietpi-software window, you should add the following software:
    -   **Software Optimised**
        -   Hardware Projects -> `WiringPi: gpio interfaces library (c)`
    -   **Software Additional**
        -   System -> `XServer: linux display system`
        -   Shared Libraries -> `Java: OpenJDK + JRE Library`
        -   Development / Programming
            -   `Build Essentials: common packages for compile`
            -   `Git Client: git clone etc`
        -   [Optional!] Text Editors -> `Vim: vi enhanced text editor`
-   hit `Install` and wait

After system reboot:
-   run `sudo -i` to login as sudo
-   run `apt-get update && apt-get upgrade`
-   run `git clone https://github.com/LeonStoldt/Lovebox.git` into your $HOME folder (/root)
-   run `dietpi-config` and select AutoStart Options
-   choose 14: Custom.sh - /var/lib/dietpi/dietpi-autostart/custom.sh
-   exit dietpi-config and cancel reboot
-   run `echo "sleep 3 && sudo bash /root/Lovebox/src/main/resources/lovebox.sh |& tee /root/lovebox.log" > /var/lib/dietpi/dietpi-autostart/custom.sh`
-   run `apt-get install maven`
-   run `cd Lovebox`
-   run `vim /root/Lovebox/src/main/resources/application.properties` (use e.g. nano instead of vim if optional part was skipped)
-   add the telegram token and change `spring.jpa.hibernate.ddl-auto` to `create` instead of validate for first run
-   run `mvn clean install`
-   check if the database files are created with `ls -la /root/Lovebox/db` you should see a db file
-   run `vim /root/Lovebox/src/main/resources/application.properties` again and change `spring.jpa.hibernate.ddl-auto` back to `validate` 
-   run `reboot`

