#!/bin/bash
sleep 5

sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y git maven midori x11-xserver-utils matchbox unclutter ca-certificates-java java-common

cd "$HOME" || exit
LOVEBOX_DIR=$HOME/Desktop/Lovebox

if [ ! -d "$LOVEBOX_DIR" ]
then
    echo "Directory $LOVEBOX_DIR DOES NOT exist."
    cd Desktop || exit
    git clone https://github.com/LeonStoldt/Lovebox.git
fi

# start lovebox program
cd LOVEBOX_DIR || exit
git pull
mvn clean install
mvn spring-boot:run &> logs/application.log &

export DISPLAY=:0.0

xset -dpms # disable DPMS (Energy Star) features.
xset s off # disable screen saver
xset s noblank # don't blank the video device

# start browser in fullscreen (kiosk mode) and remove mouse cursor
unclutter &
matchbox-window-manager -use_cursor no -use_titlebar no &
while :; do
  ps -ef | grep 'midori -e Fullscreen' | grep -v grep > /dev/null 2>&1
  if [ $? -eq 1 ] ; then
    midori -e Fullscreen -a http://localhost &
  fi
  sleep 1800
done