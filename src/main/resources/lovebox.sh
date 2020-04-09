#!/bin/bash
echo "Checking required packages."
packages=(git ca-certificates-java at-spi2-core maven midori xinit x11-xserver-utils xdotool matchbox unclutter wiringpi)
for package in "${packages[@]}"; do
  if [ "$(dpkg-query -W -f='${Status}' "$package" 2>/dev/null | grep -c "ok installed")" -eq 0 ]; then
    echo "Installing required package: $package"
    apt-get install "$package" -y
  fi
  echo "Package $package is installed."
done

WORKING_DIR=/root

cd $WORKING_DIR || exit
LOVEBOX_DIR=$WORKING_DIR/Lovebox

if [ ! -d "$LOVEBOX_DIR" ]; then
  echo "Directory $LOVEBOX_DIR DOES NOT exist."
  cd $WORKING_DIR || exit
  echo "Cloning the repository..."
  git clone https://github.com/LeonStoldt/Lovebox.git
fi

cd "$LOVEBOX_DIR" || exit
echo "Changed WORKING_DIR to Lovebox Directory."
if git pull | grep -q 'Already up to date.'; then
  echo "Repository is up to date."
else
  echo "Compiling Project."
  mvn clean compile
fi
echo "Starting Lovebox Application. Log of application: /LOVEBOX_DIR/logs/application.log"
sudo mvn spring-boot:run -Dmaven.test.skip=true |& tee logs/application.log &

echo "Disable DPMS (Energy Star) features."
xset -dpms
echo "Disable screen saver."
xset s off
echo "Don't blank the video device."
xset s noblank

echo "Copying xinit File to $WORKING_DIR Folder"
/bin/cp "$LOVEBOX_DIR"/src/main/resources/.xinitrc $WORKING_DIR/.xinitrc
