#!/bin/bash
set -e

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

RESULT_DIR=`pwd`/dist/unified

echo Cleanup...

rm -rf "$RESULT_DIR"
mkdir -p "$RESULT_DIR"

TMP_DIR=`pwd`/tmp
rm -rf $TMP_DIR
mkdir $TMP_DIR

echo Preparing...
full=1
while [ "$1" != "" ]; do
	echo "    $1..."
	pushd dist/$1
		rm -rf tmp
		ls ..
		# tar xf ../RedExpert-$VERSION.tar.gz
		if [ "$full" = "1" ]; then
			mv * $TMP_DIR
		else
			mv bin/* "$TMP_DIR/bin"
		fi
	popd
	shift
	full=0
done

echo Archiving...
pushd $TMP_DIR
tar czf "$RESULT_DIR/RedExpert-$VERSION.tar.gz" *
zip -qr "$RESULT_DIR/RedExpert-$VERSION.zip" .
popd
rm -rf $TMP_DIR
