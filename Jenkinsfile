import java.text.SimpleDateFormat 

String rev
String version
String vcs_url
String utils
String archive_prefix

try
{

node('master')
{
    stage 'Prepare'
    def wd = pwd()

    utils = load '/home/jenkins/pipeline/utils.groovy'

    checkout scm

    def sout = new StringBuilder()
    def serr = new StringBuilder()
    def proc = 'git rev-parse HEAD'.execute(null, new File(wd))
    proc.waitForProcessOutput(sout, serr)
    
    if (proc.exitValue() != 0)
    {
        println(serr)
        throw new Exception("Unable obtain revision")
    }
    rev = sout.toString()
    println(rev)
   
    sout = new StringBuilder()
    serr = new StringBuilder()
    proc = 'git show-ref --tags -d'.execute(null, new File(wd))
    proc.waitForProcessOutput(sout, serr)
    
    if (proc.exitValue() != 0)
    {
        println(serr)
        throw new Exception("Unable obtain tags")
    }        
    proc = null
        
    def matcher = (sout =~ /(?sm).*${rev} refs\/tags\/v(?<version>\d+\.\d+(\.\d+)?).*/)
    if (matcher.matches())
    {
        version = matcher.group('version')
    }
    else
    {
        matcher = (new File(wd + '/build.xml').text =~ /(?sm).*version.*(?<version>\d+\.\d+(\.\d+)?).*/)
        if (!matcher.matches())
        {
            throw new Exception("Unable obtain version")
        }
        version = matcher.group('version')
        version += new SimpleDateFormat(".yyyyMMddHHmmss").format(new Date());
    }
    matcher = null
    
    vcs_url = "https://github.com/redsoftbiz/executequery/commit/" + rev
    
    println("rev=${rev}")
    println("version=${version}")
    println("vcs_url=${vcs_url}")
    
    stage 'Create source dist'
    archive_prefix="RedExpert-${version}"
    sh 'rm -rf dist-src && mkdir dist-src'
    sh "git archive --prefix=${archive_prefix}/ -o dist-src/${archive_prefix}.tar.gz HEAD"
    
    stash includes: 'dist-src/**', name: 'src'
}

node('jdk18&&linux')
{
    stage 'Build'
    deleteDir()
    unstash 'src'
    
    sh "mkdir dist"
    sh "tar xf dist-src/${archive_prefix}.tar.gz"
    withEnv(["JAVA_HOME=${env.JAVA_HOME_1_8}", "RED_EXPERT_VERSION=${version}"]) {
        sh "cd ${archive_prefix} && ant -Dversion=${version} && ./package.sh && mv dist .."
    }
    
    stash includes: 'dist/**', name: 'bin'
}

node('master')
{
    stage 'Deploy'
    deleteDir()
    def wd = pwd()

    unstash 'src'
    unstash 'bin'
    
    sh "echo artifact red_expert ${version} > artifacts"
    sh "echo file dist/RedExpert-${version}.tar.gz tar.gz bin >> artifacts"
    sh "echo file dist/RedExpert-${version}.zip zip bin >> artifacts"
    sh "echo file dist-src/RedExpert-${version}.tar.gz tar.gz src >> artifacts"
    sh "echo end >> artifacts"

    utils.deployAndRegister('red_expert', version, wd+'/artifacts', env.BUILD_URL, vcs_url, 'red_expert', wd, '', '')
    
    currentBuild.result = 'SUCCESS'    
}

}
catch (any)
{
    currentBuild.result = 'FAILURE'
    throw any    
}
finally
{
    def body = "Job URL ${env.BUILD_URL}"

    if (currentBuild.result == 'SUCCESS')
    {
        body += "\n\nBuild page: http://builds.red-soft.biz/release_hub/red_expert/${version}"
    }

    mail (to: 'artyom.smirnov@red-soft.ru',
         subject: "Job '${env.JOB_NAME}' (${env.BUILD_NUMBER}) - ${currentBuild.result}",
         body: body);
}
