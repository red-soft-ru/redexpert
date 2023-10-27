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

echo "Downloading tests"
git clone -q http://git.red-soft.biz/red-database/re-tests.git
git clone -q http://git.red-soft.biz/red-database/python/lackey.git

echo "Installing components"
start-process "${PYTHON}" "-m pip install pytest pyautogui" -wait -nonewwindow
start-process "${PYTHON}" "-m pip install git+http://git.red-soft.biz/red-database/python/red-database-python-driver.git" -wait -nonewwindow
start-process "${PYTHON}" "-m pip install -e .\lackey" -wait -nonewwindow
start-process "${PYTHON}" "-m pip install -e .\re-tests" -wait -nonewwindow

echo "Start testing"
cd re-tests
start-process "${PYTHON}" "-m pytest -vv --junitxml .\results.xml .\tests" -wait -nonewwindow

echo "Copy test results"
if (Test-Path "results.xml") {
    mkdir "${WORKSPACE}\test-results\"
    copy "results.xml" "${WORKSPACE}\test-results\${DISTRO}-${DBMS}-${ARCH}.xml"
}
else
{
    echo "No test results. Testing not completed properly!"
    exit 1
}
