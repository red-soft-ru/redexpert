if(-Not (Test-Path Env:\JAVA_HOME)){
    echo "JAVA_HOME environment variable required"
    exit 1
}

$SRC_DIR="$PSScriptRoot\.."

echo "JAVA_HOME=$Env:JAVA_HOME"
echo "SRC_DIR=$SRC_DIR"

cd "$SRC_DIR"
& mvn package
