echo "Building installer"
$SRC_DIR="$PSScriptRoot\.."
if (-Not(Test-Path Env:\RE_VERSION))
{
    echo "RE_VERSION environment variable required"
    exit 1
}
if (-Not(Test-Path Env:\LICENSE_INSTALLBUILDER_REDXPERT))
{
    echo "LICENSE_INSTALLBUILDER_REDXPERT environment variable required"
    exit 1
}
$DIST=$env:DIST
$ARCH=$env:ARCH
$VERSION=$env:RE_VERSION
echo "version=$VERSION"
$INSTALLER_NAME="RedXpert-$VERSION-installer-windows-$ARCH.exe"
$exec_file="RedXpert"
if ($ARCH -eq "amd64")
{
    $exec_file="RedXpert64"
}
echo "Src=$SRC_DIR"
echo "Dist=$DIST"
cd $SRC_DIR/installer
$env:LICENSE_INSTALLBUILDER_REDXPERT | out-file license.xml
builder-cli.exe build RedXpert.xml --license license.xml --verbose --setvars redexpert_dir=$DIST output_dir=$DIST/bin installer_name=$INSTALLER_NAME execution_file=$exec_file VERSION=$VERSION
