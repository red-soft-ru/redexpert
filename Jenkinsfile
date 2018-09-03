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
            mkdir dist/guide
            mkdir dist/license
            mkdir dist/config
            cd native/RedExpertNativeLauncher
            /usr/bin/qmake-qt5
            make
            cd ..
            mkdir bin
            cp RedExpertNativeLauncher/bin/RedExpert64 bin/
            cd ..
            mvn package
            cp -r native/bin dist/
            cp -r modules/redexpert/target/lib dist/
            cp -r guide/*.pdf dist/guide/
            cp -r license/ dist/
            cp -r config/ dist/
            cp red_expert.png dist/
            cp red_expert.ico dist/
            cp redexpert.desktop dist/
            cp modules/redexpert/target/RedExpert.jar dist/
            cp modules/redexpert/target/RedExpert.sh dist/
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
            bat '''cd %ARCHIVE_PREFIX%\\
            call "C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\vcvarsall.bat" amd64
            mkdir dist
            mkdir dist\\bin\\
            mkdir dist\\bin\\platforms\\
            mkdir dist\\lib\\
            mkdir dist\\guide\\
            mkdir dist\\license\\
            mkdir dist\\config\\
            cd native\\RedExpertNativeLauncher
            "c:\\Qt\\Qt5.6.3\\5.6.3\\msvc2013_64\\bin\\qmake.exe"
            "C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\bin\\amd64\\nmake.exe"
            cd ..
            mkdir bin
            copy RedExpertNativeLauncher\\release\\bin\\RedExpert64.exe bin\\
            cd ..
            copy /y native\\bin\\ dist\\bin\\
            copy /y RedExpert.bat dist\\
            move dist ..
            '''
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

        sh "tar xf dist-src/RedExpert-${version}-src.tar.gz"
        sh "VERSION=${version} RedExpert-${version}/ci/package-bin.sh"
        
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
