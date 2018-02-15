#!/usr/bin/env bash
desktopsh="re.sh"

if [ ! -f "$desktopsh" ]; then
    echo "#!/bin/sh
builtin cd $PWD
exec  ./RedExpert.sh &">>re.sh
chmod 777 re.sh
echo "Exec=\"$PWD/re.sh\" %f">>redexpert.desktop
echo "Icon=$PWD/red_expert.png">>redexpert.desktop
cp "./redexpert.desktop" $HOME/.local/share/applications/redexpert.desktop
chmod 777 redexpert.desktop
fi