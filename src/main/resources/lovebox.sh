#!/bin/bash
echo "Checking required packages."
packages=(git maven midori xinit x11-xserver-utils matchbox unclutter ca-certificates-java java-common at-spi2-core)
for package in "${packages[@]}"; do
  if [ "$(dpkg-query -W -f='${Status}' "$package" 2>/dev/null | grep -c "ok installed")" -eq 0 ]; then
    echo "Installing required package: $package"
    apt-get install "$package"
  fi
  echo "Package $package is installed."
done

[ -z "$DISPLAY" ] && echo "Could not found \$DISPLAY. Please add the environment variable to the ~/.bashrc or ~/.profile file" && exit || echo "Found \$DISPLAY"

cd "$HOME" || exit
LOVEBOX_DIR=$HOME/Desktop/Lovebox

if [ ! -d "$LOVEBOX_DIR" ]; then
  echo "Directory $LOVEBOX_DIR DOES NOT exist."
  cd Desktop || exit
  echo "Cloning the repository..."
  git clone https://github.com/LeonStoldt/Lovebox.git
fi

cd "$LOVEBOX_DIR" || exit
echo "Moved to Lovebox Directory."
git pull
mvn clean compile
echo "Starting Lovebox Application. Log of application: /LOVEBOX_DIR/logs/application.log"
mvn spring-boot:run &>logs/application.log &

echo "Disable DPMS (Energy Star) features."
xset -dpms
echo "Disable screen saver."
xset s off
echo "Don't blank the video device."
xset s noblank

echo "Copying xinit File to \$HOME Folder"
/bin/cp ./.xinitrc "$HOME"/.xinitrc
echo "Removing mouse cursor."
unclutter &
echo "Starting GUI with Browser"
startx
