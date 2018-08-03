@Library('jenkins_pipeline_utils')

import ru.redsoft.jenkins.Pipeline;
import ru.redsoft.jenkins.Git;
import ru.redsoft.jenkins.ReleaseHub;

String release_hub_project = 'red_expert'
String rev
String version
String vcs_url
String branch = env.BRANCH_NAME

properties([
    buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '90', numToKeepStr: ''))
])

try
{

node('master')
{
    def wd = pwd()
    deleteDir()
    stage('Prepare')
    {

        checkout scm

        rev = Git.getGitRevision(wd)

        def matcher = (new File(wd + '/src/org/executequery/eq.system.properties').text =~ /(?sm).*re\.version=(?<version>\d+((\.\d+)*)?).*/)
        if (!matcher.matches())
        {
            throw new Exception("Unable obtain version")
        }
        version = matcher.group('version')
        version += "." + ReleaseHub.getBuildNo(release_hub_project, version)
        matcher = null
        
        vcs_url = "https://github.com/redsoftbiz/executequery/commit/" + rev
        

        println("rev=${rev}")
        println("version=${version}")
        println("vcs_url=${vcs_url}")
    }

    stage('Create source dist')
    {
        sh "VERSION=${version} ci/prepare-src.sh"       
        stash includes: 'dist-src/**', name: 'src'
    }
}

node('jdk18&&linux&&builder&&x86_64&&mvn')
{
    stage('Build')
    {
        deleteDir()
        unstash 'src'
        def archive_prefix="RedExpert-${version}"
        
        sh "tar xf dist-src/${archive_prefix}-src.tar.gz"
        withEnv(["JAVA_HOME=${JAVA_HOME_1_8}", "RED_EXPERT_VERSION=${version}", "ARCHIVE_PREFIX=${archive_prefix}"]) {
            sh '''cd ${ARCHIVE_PREFIX}
            mkdir dist
            mkdir dist/bin
            mkdir dist/lib
            mkdir dist/docs
            mkdir dist/license
            mkdir dist/config
            cd native/RedExpertNativeLauncher
            /usr/bin/qmake-qt5
            make
            cd ..
            mkdir bin
            cp RedExpertNativeLauncher/bin/RedExpertNativeLauncher64 bin/
            cd ..
            mvn package
            cp -r native/bin dist/
            cp -r modules/redexpert/target/lib dist/
            cp -r docs/ dist/
            cp -r license/ dist/
            cp -r config/ dist/
            cp red_expert.png dist/
            cp red_expert.ico dist/
            cp redexpert.desktop dist/
            cp modules/redexpert/target/RedExpert.jar dist/
            cp createDesktopEntry.sh dist/
            cp LICENSE.txt dist/
            mv dist ..'''
        }
        
        stash includes: 'dist/**', name: 'linux-bin'
    }
}

node('jdk18&&windows&&builder&&x86_64')
{
    stage('Build')
    {
        deleteDir()
        unstash 'src'
        def archive_prefix="RedExpert-${version}"

        bat "unzip dist-src\\${archive_prefix}-src.zip"
        withEnv(["JAVA_HOME=${JAVA_HOME_1_8_x64}", "RED_EXPERT_VERSION=${version}", "ARCHIVE_PREFIX=${archive_prefix}"]) {
            // TODO QT_HOME variable?
            bat '''cd %ARCHIVE_PREFIX%\\
            call %comspec% /k "C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\vcvarsall.bat" amd64
            mkdir dist
            mkdir dist\\bin\\
            mkdir dist\\bin\\platforms\\
            mkdir dist\\lib\\
            mkdir dist\\docs\\
            mkdir dist\\license\\
            mkdir dist\\config\\
            cd native\\RedExpertNativeLauncher
            "c:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\bin\\qmake.exe"
            "C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\bin\\amd64\\nmake.exe"
            cd ..
            mkdir bin
            copy RedExpertNativeLauncher\\release\\bin\\RedExpertNativeLauncher64.exe bin\\
            mkdir bin\\platforms
            copy "C:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\plugins\\platforms\\qminimal.dll" bin\\platforms\\
            copy "C:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\plugins\\platforms\\qoffscreen.dll" bin\\platforms\\
            copy "C:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\plugins\\platforms\\qwindows.dll" bin\\platforms\\
            copy "C:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\bin\\Qt5Core.dll" bin\\
            copy "C:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\bin\\Qt5Gui.dll" bin\\
            copy "C:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\bin\\Qt5Widgets.dll" bin\\
            cd ..
            mvn package
            copy /y native\\bin\\ dist\\bin\\
            copy /y native\\bin\\platforms\\ dist\\bin\\platforms\\
            copy /y modules\\redexpert\\target\\lib\\ dist\\lib\\
            copy /y docs\\ dist\\docs\\
            copy /y license\\ dist\\license\\
            copy /y config\\ dist\\config\\
            copy /y red_expert.png dist\\
            copy /y red_expert.ico dist\\
            copy /y redexpert.desktop dist\\
            copy /y modules\\redexpert\\target\\RedExpert.jar dist\\
            copy /y createDesktopEntry.sh dist\\
            copy /y LICENSE.txt dist\\
            move dist ..'''
        }

        stash includes: 'dist/**', name: 'windows-bin'
    }
}

node('master')
{
    stage('Deploy')
    {
        deleteDir()
        def wd = pwd()

        unstash 'src'
        unstash 'linux-bin'
        unstash 'windows-bin'
        
        sh "echo artifact red_expert ${version} > artifacts"
        sh "echo file dist/RedExpert-${version}.tar.gz tar.gz bin >> artifacts"
        sh "echo file dist/RedExpert-${version}.zip zip bin >> artifacts"
        sh "echo end >> artifacts"

        sh "echo artifact red_expert-src ${version} >> artifacts"
        sh "echo file dist-src/RedExpert-${version}-src.tar.gz tar.gz src >> artifacts"
        sh "echo file dist-src/RedExpert-${version}-src.zip zip src >> artifacts"
        sh "echo end >> artifacts"

        ReleaseHub.deployToReleaseHub(release_hub_project, version, env.BUILD_URL, rev, wd+'/artifacts', wd, 'red_expert', '', '', branch)
        
        Pipeline.defaultSuccessActions(currentBuild)
    }
}

} // try
catch (any)
{
    Pipeline.defaultFailureActions(currentBuild, any)
}
finally
{
    mail(to: Pipeline.defaultEmailAddresses() + ',mikhail.kalyashin@red-soft.ru',
         subject: Pipeline.defaultEmailSubject(currentBuild, version, rev),
         body: Pipeline.defaultEmailBody(currentBuild, vcs_url, release_hub_project, version));
}
