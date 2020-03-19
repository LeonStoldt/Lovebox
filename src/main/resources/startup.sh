#!/bin/bash
sleep 5
xrandr -s 1024x600 &

sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y git maven default-jre midori matchbox unclutter

cd "$HOME" || exit

LOVEBOX_DIR=$HOME/Lovebox

if [ ! -d "$LOVEBOX_DIR" ]
then
    echo "Directory $LOVEBOX_DIR DOES NOT exist."
    git clone https://github.com/LeonStoldt/Lovebox.git
fi

cd LOVEBOX_DIR || exit

git pull
mvn clean install
mvn spring-boot:run

xset -dpms # disable DPMS (Energy Star) features.
xset s off # disable screen saver
xset s noblank # don't blank the video device
unclutter &
matchbox-window-manager -use_cursor no -use_titlebar no  &
while :; do
  ps -ef | grep 'midori -e Fullscreen' | grep -v grep > /dev/null 2>&1
  if [ $? -eq 1 ] ; then
    midori -e Fullscreen -a http://localhost &
  fi
  sleep 1800
done