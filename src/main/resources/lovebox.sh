#!/bin/bash
echo "Checking required packages."
packages=(git maven midori xinit x11-xserver-utils xdotool matchbox unclutter ca-certificates-java java-common wiringpi at-spi2-core)
for package in "${packages[@]}"; do
  if [ "$(dpkg-query -W -f='${Status}' "$package" 2>/dev/null | grep -c "ok installed")" -eq 0 ]; then
    echo "Installing required package: $package"
    apt-get install "$package"
  fi
  echo "Package $package is installed."
done

cd "$HOME" || exit
LOVEBOX_DIR=$HOME/Desktop/Lovebox

if [ ! -d "$LOVEBOX_DIR" ]; then
  echo "Directory $LOVEBOX_DIR DOES NOT exist."
  cd Desktop || exit
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
sudo mvn spring-boot:run |& tee logs/application.log &

echo "Disable DPMS (Energy Star) features."
xset -dpms
echo "Disable screen saver."
xset s off
echo "Don't blank the video device."
xset s noblank

echo "Copying xinit File to \$HOME Folder"
/bin/cp "$LOVEBOX_DIR"/src/main/resources/.xinitrc "$HOME"/.xinitrc
