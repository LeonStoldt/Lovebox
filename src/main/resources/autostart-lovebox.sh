#!/bin/bash
# 1. copy this content to custom.sh in /var/lib/dietpi/dietpi-autostart/
# 2. setup custom.sh as startup script in dietpi-config -> autostart options
sleep 3
sudo /root/Desktop/Lovebox/src/main/resources/lovebox.sh |& tee /var/lib/dietpi/dietpi-autostart/lovebox.log