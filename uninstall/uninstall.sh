#!/bin/sh

# determine the java command to be run
JAVA=`which java`

if [ "X$JAVA" = "X" ]; then
    # try possible default location (which should have come up anyway...)
    JAVA=/usr/bin/java
fi

exec $JAVA -jar "uninstall.jar" &

