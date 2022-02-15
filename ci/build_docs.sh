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
sed -i "s/\${VERSION}/$VERSION/" general_defs.tex


pushd guides
make clean
make package
popd

mkdir out
cp guides/out/RedExpert_Guide-ru.pdf out/RedExpert-$VERSION-Guide-ru.pdf