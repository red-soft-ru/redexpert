echo "Run tests"

function die($msg)
{
    echo "${msg}"
    exit 1
}

if (-Not (Test-Path env:\ARCH)) { die("ARCH not defined") }
$ARCH=$env:ARCH

if (-Not (Test-Path env:\WORKSPACE)) { die("WORKSPACE not defined") }
$WORKSPACE=$env:WORKSPACE

if (-Not (Test-Path env:\PYTHON)) { die("PYTHON not defined") }
$PYTHON=$env:PYTHON

if (-Not (Test-Path env:\DISTRO)) { die("DISTRO not defined") }
$DISTRO=$env:DISTRO

if (-Not (Test-Path env:\DBMS)) { die("DBMS not defined") }
$DBMS=$env:DBMS

if ( ${DBMS} -eq "rdb30" -or ${DBMS} -eq "rdb50")
{
    start-process ".\installer.exe" "--mode unattended --architecture Super --sysdba_password masterkey" -wait -nonewwindow
    $SERVICE_NAME="RedDatabase Server - DefaultInstance"
    if ((Get-Service $SERVICE_NAME -ErrorAction SilentlyContinue) -eq $null) { die("RDB not running") }
}
else
{
    start-process ".\installer.exe" "/SILENT" -wait -nonewwindow
    $SERVICE_NAME="Firebird Server - DefaultInstance"
    if ((Get-Service $SERVICE_NAME -ErrorAction SilentlyContinue) -eq $null) { die("FB not running") }
}





