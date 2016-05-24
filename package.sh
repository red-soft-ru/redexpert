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

check_variable RED_EXPERT_VERSION

rm -rf tmp dist
mkdir tmp dist

cp RedExpert-${RED_EXPERT_VERSION}.jar tmp/RedExpert.jar
cp eq.sh tmp/RedExpert.sh
cp RedExpert.bat tmp

cp -r red_expert.{png,ico} \
	  lib \
	  docs \
	  license \
	  LICENSE \
	  tmp

cd tmp
ARCHIVE_PREFIX=RedExpert-$RED_EXPERT_VERSION
tar --transform "s|^.|RedExpert-$RED_EXPERT_VERSION|" -czf ../dist/RedExpert-$RED_EXPERT_VERSION.tar.gz .
zip -9 -r -y  ../dist/RedExpert-$RED_EXPERT_VERSION.zip . > /dev/null
