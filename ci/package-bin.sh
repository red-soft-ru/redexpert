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
RESULT_DIR=$BIN/dist

#mkdir -p $RESULT_DIR

#cp -r linux-bin/ $RESULT_DIR
#cp -r windows-bin/ $RESULT_DIR

tar --transform "s/.*\/dist//" -czf RedXpert-$VERSION.tar.gz $RESULT_DIR
cd $RESULT_DIR
zip -rq ../RedXpert-$VERSION.zip ./
cd ..

mv RedXpert-$VERSION.tar.gz $RESULT_DIR
mv RedXpert-$VERSION.zip $RESULT_DIR