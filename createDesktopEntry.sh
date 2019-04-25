#!/usr/bin/env bash

EXE_PATH=$PWD
if [[ $PWD == *"/bin"* ]]; then
    EXE_PATH=$(dirname "$EXE_PATH")
fi
yes | cp -rf "$EXE_PATH/redexpert.desktop" $HOME/.local/share/applications/redexpert.desktop
if [ `getconf LONG_BIT` = "64" ]
then
    echo "Exec=bash -c \" cd $EXE_PATH/bin && $EXE_PATH/bin/RedExpert64\"">>$HOME/.local/share/applications/redexpert.desktop
else
    echo "Exec=bash -c \" cd $EXE_PATH/bin && $EXE_PATH/bin/RedExpert\"">>$HOME/.local/share/applications/redexpert.desktop
fi
echo "Path=\"$EXE_PATH/bin\"">>$HOME/.local/share/applications/redexpert.desktop
echo "Icon=$EXE_PATH/red_expert.png">>$HOME/.local/share/applications/redexpert.desktop
