#!/usr/bin/env bash

if [ ! -f $HOME/.local/share/applications/redexpert.desktop ]; then
    EXE_PATH=$PWD
    if [[ $PWD == *"/bin"* ]]; then
        EXE_PATH=$(dirname "$EXE_PATH")
    fi
    cp "$EXE_PATH/redexpert.desktop" $HOME/.local/share/applications/redexpert.desktop
    if [ `getconf LONG_BIT` = "64" ]
    then
        echo "Exec=\"$EXE_PATH/bin/RedExpert64\"">>$HOME/.local/share/applications/redexpert.desktop
    else
        echo "Exec=\"$EXE_PATH/bin/RedExpert\"">>$HOME/.local/share/applications/redexpert.desktop
    fi

    echo "Icon=$EXE_PATH/red_expert.png">>$HOME/.local/share/applications/redexpert.desktop
fi