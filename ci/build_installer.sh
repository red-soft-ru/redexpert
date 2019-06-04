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
check_variable VERSION
check_variable DIST
check_variable ARCH
DIST=${DIST:=dist}
INSTALLBUILDER_BINARY=${INSTALLBUILDER_BINARY:=/opt/installbuilder/bin/builder}
echo $LICENSE_INSTALLBUILDER > /opt/installbuilder/license.xml
SRC_DIR=$(readlink -f `dirname $0`/..)
INSTALLER_SRC_DIR=$SRC_DIR/installer
INSTALLER_COMPONENTS=$DIST
INSTALLER_OUTPUT_DIR=$DIST/bin
VERSION=${VERSION=version}
ARCH=${ARCH=arch}
INSTALLER_NAME="RedExpert-$VERSION-installer-linux-$ARCH.bin"
exec_file="RedExpert"
if [ "$ARCH" == "x86_64" ]; then
    exec_file="RedExpert64"
fi
echo "Building RedExpert installer"
$INSTALLBUILDER_BINARY build $INSTALLER_SRC_DIR/RedExpert.xml --verbose --setvars \
        redexpert_dir=$INSTALLER_COMPONENTS \
        output_dir=$INSTALLER_OUTPUT_DIR \
        installer_name=$INSTALLER_NAME \
        execution_file=$exec_file \
        VERSION=$VERSION