properties([
    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '10', numToKeepStr: '10'))
    }
])

node('maven') {
    stage('docker setup') {
        withCredentials([dockerCert(credentialsId: 'docker-cert', variable: 'DOCKER_CERT_PATH'),
                         usernamePassword(credentialsId: 'docker-login', passwordVariable: 'pass', usernameVariable: 'username')]) {
            sh "cp -r \'$DOCKER_CERT_PATH\' /root/.docker/"
            sh "echo $pass | docker login --username $username --password-stdin"
        }
    }
    stage('dockerize & push') {
        checkout scm
        def repoName = "tonyhsu17"
        def containerName = sh script: 'mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout', returnStdout: true
        def version = sh script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true

        sh "docker build -t ${repoName}/${containerName}:${version} ."
        sh "docker run ${repoName}/${containerName}:${version}"
        sh "docker push ${repoName}/${containerName}:${version}"
        try {
            input 'Push to stable?'
            sh "docker tag ${repoName}/${containerName}:${version} ${repoName}/${containerName}:stable"
            sh "docker push ${repoName}/${containerName}:stable"
        } catch(err) {
            println "Skipping pushing to stable"
        }
    }
}
