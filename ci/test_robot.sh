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

check_variable ARCH
check_variable WORKSPACE
check_variable DISTRO
check_variable PYTHON
check_variable DBMS

echo "Downloading tests"
git clone -q http://git.red-soft.biz/red-database/re-tests-robot

echo "Installing components"
$PYTHON -m pip install git+http://git.red-soft.biz/red-database/python/red-database-python-driver.git
$PYTHON -m pip install robotframework

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/RedDatabase/lib

export PYTHONPATH=$PYTHONPATH:/root/remoteswinglibrary-2.3.3.jar
export DISPLAY=:0
su reduser -c 'xhost +'

echo "Start testing"
cd re-tests-robot
$PYTHON -m robot -x results.xml --nostatusrc ./tests

echo "Copy test results"
mkdir "${WORKSPACE}/test-results/"
cp results.xml "${WORKSPACE}/test-results/${DISTRO}-${DBMS}-${ARCH}.xml"
cp log.html "${WORKSPACE}/test-results/${DISTRO}-${DBMS}-${ARCH}-log.html"
cp report.html "${WORKSPACE}/test-results/${DISTRO}-${DBMS}-${ARCH}-report.html"