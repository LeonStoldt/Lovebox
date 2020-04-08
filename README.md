# WIP Project

## Pi installation
-   copy pi-config/config.txt into sd boot folder and replace config.txt
-   add ssh file to activate ssh
-   run `ssh pi@raspberrypi.local` in some terminal or use putty to connect to your pi via ssh
-   run `sudo -i` to login as sudo
-   run `apt-get update`
-   run `apt-get upgrade`
-   run `apt-get install git`
-   run `git clone https://github.com/LeonStoldt/Lovebox.git` into your $HOME folder (/home/pi)
-   go into the raspberry config by `raspi-config`
-   change `Boot Options` -> `Desktop / CLI` to `Console Autologin`
-   change `Boot Options` -> `Wait for Network at Boot` to `Yes`
-   change `Localisation Options` -> `Change Timezone` to your Timezone
-   change `Localisation Options` -> `Change Wi-fi Country` to your Country
-   run `echo "sleep 3 && sudo bash /home/pi/Lovebox/src/main/resources/lovebox.sh |& tee /home/pi/lovebox.log" > /etc/init.d/lovebox.sh`
-   run `chmod 755 /etc/init.d/lovebox.sh`
-   run `update-rc.d lovebox.sh defaults`
-   run `reboot`

