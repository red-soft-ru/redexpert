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

RESULT_DIR=`pwd`/dist/guide
rm -rf "$RESULT_DIR" out
mkdir -p "$RESULT_DIR"
sed -i "s/\${VERSION}/$VERSION/" guide/general_defs.tex


pushd guide
make clean
make package
popd

mkdir out
cp guide/out/RedExpert_Guide-ru.pdf out/RedExpert-$VERSION-Guide-ru.pdf