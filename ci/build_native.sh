#!/bin/bash
set -e
set -x

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

check_variable JAVA_HOME

QMAKE=${QMAKE:=qmake}
SRC_DIR=$(readlink -f `dirname $0`/..)
RESULT_DIR="${SRC_DIR}/dist"
rm -rf "${RESULT_DIR}"

cd "${SRC_DIR}/native/RedXpertNativeLauncher"
$QMAKE
make
