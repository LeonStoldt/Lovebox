#!/bin/sh

echo "Starting Window Manager."
matchbox-window-manager -use_cursor no -use_titlebar no &
while :; do
  ps -ef | grep 'midori -p http://localhost' | grep -v grep >/dev/null 2>&1
  if [ $? -eq 1 ]; then
    echo "Starting Midori in Fullscreen mode."
    midori -p http://localhost &
    sleep 30
    midori -e Fullscreen http://localhost
  fi
  sleep 1800
done