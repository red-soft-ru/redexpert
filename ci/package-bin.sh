#!/bin/bash
set -e

BIN=$(readlink -f $(dirname $0)/../..)

function die()
{
	echo "$1"
	exit 1
}

function check_variable()
{
	eval val='$'$1
	[ "$val" != "" ] || die "$1 not defined"
}

check_variable VERSION

echo Packing binaries
echo $BIN
cd $BIN
ll
RESULT_DIR=`pwd`/dist

#mkdir -p $RESULT_DIR

#cp -r linux-bin/ $RESULT_DIR
#cp -r windows-bin/ $RESULT_DIR

tar -czvf RedExpert-$VERSION.tar.gz RESULT_DIR
zip -r RedExpert-$VERSION.zip RESULT_DIR

ll