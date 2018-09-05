#!/bin/sh

# Java heap size, in megabytes
JAVA_HEAP_SIZE=1024

# determine the java command to be run
JAVA=`which java`

if [ "X$JAVA" = "X" ]; then
    # try possible default location (which should have come up anyway...)
    JAVA=/usr/bin/java
fi

# DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ `getconf LONG_BIT` = "64" ]
then
    exec bin/RedExpert64 -mx${JAVA_HEAP_SIZE}m &
else
    exec bin/RedExpert -mx${JAVA_HEAP_SIZE}m &
fi

