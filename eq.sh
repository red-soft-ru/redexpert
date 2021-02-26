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
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

if [ -z "$JAVA_HOME" ]; then
	export JAVA_HOME=$1
fi

if [ `getconf LONG_BIT` = "64" ]
then
    exec "$SCRIPTPATH"/bin/RedXpert64 -Xmx${JAVA_HEAP_SIZE}m &
else
    exec "$SCRIPTPATH"/bin/RedXpert -Xmx${JAVA_HEAP_SIZE}m &
fi

