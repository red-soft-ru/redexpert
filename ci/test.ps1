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

echo "Downloading tests"
git clone -q http://git.red-soft.biz/red-database/re-tests.git

echo "Installing components"
start-process "${PYTHON}" "-m pip install pytest" -wait -nonewwindow
start-process "${PYTHON}" "-m pip install -e .\re-tests" -wait -nonewwindow

echo "Downloading Red RedDatabase"
$URL_DOWNLOAD="http://builds.red-soft.biz/release_hub/rdb30/3.0.10-rc.5/download/red-database:windows-${ARCH}-enterprise:3.0.10-rc.5:exe"
$client=new-object System.Net.WebClient
$client.DownloadFile(${URL_DOWNLOAD}, ".\installer.exe")

if (${ARCH} -eq "x86_64")
{
    $INSTALL_PATH="C:\Program Files\RedDatabase"
}
else
{
    $INSTALL_PATH="C:\Program Files (x86)\RedDatabase"
}

$env:FIREBIRD=$INSTALL_PATH

$env:FB_CLIENT="${INSTALL_PATH}\fbclient.dll"
$SERVICE_NAME="RedDatabase Server - DefaultInstance"

echo "Installing Red RedDatabase"
start-process ".\installer.exe" "--mode unattended --architecture Classic --sysdba_password masterkey" -wait -nonewwindow

echo "Restart RDB server"
net stop "${SERVICE_NAME}"
net start "${SERVICE_NAME}"

echo "Start testing"
cd re-tests
start-process "${PYTHON}" "-m pytest -vv --junitxml .\results.xml .\tests" -wait -nonewwindow

echo "Copy test results"
if (Test-Path "results.xml") {
    mkdir "${WORKSPACE}\test-results\"
    copy "results.xml" "${WORKSPACE}\test-results\${DISTRO}-${ARCH}.xml"
}
else
{
    echo "No test results. Testing not completed properly!"
    exit 1
}
