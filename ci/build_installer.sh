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
ARCH=${ARCH=arch}
url_installbuilder="http://builds.red-soft.biz/release_hub/installbuilder/18.7.0/download/installbuilder:linux-x86:18.7.0:run"
if [ "$ARCH" == "x86_64" ]; then
    url_installbuilder="http://builds.red-soft.biz/release_hub/installbuilder/18.7.0/download/installbuilder:linux-x86_64:18.7.0:run"
fi
echo $url_installbuilder
wget -O installbuilder.run $url_installbuilder
chmod +x installbuilder.run
./installbuilder.run --mode unattended  --prefix /opt/installbuilder
DIST=${DIST:=dist}
INSTALLBUILDER_BINARY=${INSTALLBUILDER_BINARY:=/opt/installbuilder/bin/builder}
echo $LICENSE_INSTALLBUILDER_REDXPERT > /opt/installbuilder/license.xml
SRC_DIR=$(readlink -f `dirname $0`/..)
INSTALLER_SRC_DIR=$SRC_DIR/installer
INSTALLER_COMPONENTS=$DIST
INSTALLER_OUTPUT_DIR=$DIST/bin
VERSION=${VERSION=version}
INSTALLER_NAME="RedXpert-$VERSION-installer-linux-$ARCH.bin"
exec_file="RedXpert"
if [ "$ARCH" == "x86_64" ]; then
    exec_file="RedXpert64"
fi
echo "Building RedXpert installer"
$INSTALLBUILDER_BINARY build $INSTALLER_SRC_DIR/RedXpert.xml --verbose --setvars \
        redexpert_dir=$INSTALLER_COMPONENTS \
        output_dir=$INSTALLER_OUTPUT_DIR \
        installer_name=$INSTALLER_NAME \
        execution_file=$exec_file \
        VERSION=$VERSION