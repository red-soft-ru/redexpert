Install-Module Pscx -Scope CurrentUser -Force
Import-Module Pscx

if(-Not (Test-Path Env:\JAVA_HOME)){
    echo "JAVA_HOME environment variable required"
    exit 1
}

if(-Not (Test-Path Env:\ARCH)){
    echo "ARCH environment variable required"
    exit 1
}

if(-Not (Test-Path Env:\QMAKE)){
    echo "QMAKE environment variable required"
    exit 1
}

$SRC_DIR="$PSScriptRoot\.."
$QMAKE=$env:QMAKE

echo "JAVA_HOME=$Env:JAVA_HOME"
echo "SRC_DIR=$SRC_DIR"
echo "QMAKE=$QMAKE"

cd "$SRC_DIR\native\RedExpertNativeLauncher\"

Import-VisualStudioVars -VisualStudioVersion 2013 -Architecture $env:ARCH

& "$QMAKE"
if (-Not$?)
{
    echo "qmake failed"
    exit 1
}

& "nmake.exe"
if (-Not$?)
{
    echo "nmake failed"
    exit 1
}

mkdir -force bin
cp -recurse .\release\bin\* bin
