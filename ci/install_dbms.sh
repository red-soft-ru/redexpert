#!/bin/bash
set -e
set -x

echo "Install and run DBMS"

function die()
{
	echo "$1"
	exit 1
}

echo "Installing"
chmod +x ./installer.bin
./installer.bin --mode unattended --sysdba_password masterkey --architecture Super || die "Unable to install server"


echo "Starting guardian"
PIDDIR=/var/run/reddatabase/
PIDFILE=$PIDDIR/default.pid
mkdir -p "$PIDDIR"  
/opt/RedDatabase/bin/rdbguard -daemon -guardpidfile $PIDFILE