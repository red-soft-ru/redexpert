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

node('jdk18&&linux&&builder&&mvn')
{
    stage('Build')
    {
        deleteDir()
        unstash 'src'
        def archive_prefix="RedExpert-${version}"
        
        sh "tar xf dist-src/${archive_prefix}-src.tar.gz"
        withEnv(["JAVA_HOME=${JAVA_HOME_1_8}", "RED_EXPERT_VERSION=${version}"]) {
            sh "cd ${archive_prefix} && mvn package && mkdir dist && cp ./modules/redexpert/target/${archive_prefix}.* dist/ && mv dist .."
        }
        
        stash includes: 'dist/**', name: 'bin'
    }
}

node('master')
{
    stage('Deploy')
    {
        deleteDir()
        def wd = pwd()

        unstash 'src'
        unstash 'bin'
        
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
