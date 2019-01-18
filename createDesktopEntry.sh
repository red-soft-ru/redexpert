#!/usr/bin/env bash

if [ ! -f $HOME/.local/share/applications/redexpert.desktop ]; then
    EXE_PATH=$PWD
    if [[ $PWD == *"/bin"* ]]; then
        EXE_PATH=$(dirname "$EXE_PATH")
    fi
    cp $EXE_PATH/redexpert.desktop $HOME/.local/share/applications/redexpert.desktop
    echo "Exec=\"$EXE_PATH/RedExpert.sh\" $JAVA_HOME">>$HOME/.local/share/applications/redexpert.desktop
    echo "Icon=$EXE_PATH/red_expert.png">>$HOME/.local/share/applications/redexpert.desktop
fi