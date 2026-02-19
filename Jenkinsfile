properties([
    pipelineTriggers([ 
        [
            $class: 'GenericTrigger',
            token: 'MY_SPRING_TOKEN',  
            printContributedVariables: true,
            genericVariables: [
                [key: 'ref',       value: '$.ref'],
                [key: 'repo_name', value: '$.repository.name']
            ], 
            regexpFilterText: '$repo_name:$ref',   
            regexpFilterExpression: '^.+:refs/heads/.+$'
        ]
    ])
])
 
pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3.6'
    }

    environment {
        // Git
        GIT_CREDENTIALS_ID = 'github-creds'
        GIT_BRANCH_URL = 'https://github.com/vincentino1/catalog-service.git'

        // Nexus
        NEXUS_VERSION        = 'nexus3'
        NEXUS_CREDENTIALS_ID = 'nexus-creds'
        NEXUS_URL=    'repo.vinny-dev.com'
        NEXUS_REPO    = 'myapp-maven-hosted'


        // Nexus Docker Registry ENV
        DOCKER_REPO_PUSH           = 'myapp-docker-hosted'
        DOCKER_REPO_PULL          = 'myapp-docker-group'
        DOCKER_CREDENTIALS_ID = 'docker-registry-creds'


    }

    stages {

        stage('Webhook Debug') {
            steps {
                echo "Branch ref: ${env.ref}"
                echo "Repository: ${env.repo_name}"
            }
        }

        stage('Clean Workspace') {
            steps {
                echo 'Deleting workspace...'
                cleanWs()
            }
        }

        stage('Checkout') {
            steps {
                script {
                    if (!env.ref) {
                        error "Webhook did not send 'ref'. Cannot determine branch."
                    }
                    env.branchName = env.ref.replace('refs/heads/', '')
                    echo "Checking out branch: ${env.branchName}"
                }

                git(
                    branch: env.branchName,
                    credentialsId: env.GIT_CREDENTIALS_ID,
                    url: env.GIT_BRANCH_URL
                )

                script {
                    // Read Maven POM AFTER checkout
                    def pom = readMavenPom file: 'pom.xml'
                    env.APP_NAME = pom.artifactId
                }
            }
        }

        stage('Install & Build') {
            steps {
                configFileProvider([
                    configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')
                ]) {
                    sh 'mvn -s $MAVEN_SETTINGS clean package'
                }
            }
        }

        stage('Publish') {
            when { 
                expression { env.branchName == 'main' }
            }
            steps {
                script {
                    // Read POM
                    def pom = readMavenPom file: 'pom.xml'
                    def artifactId = pom.artifactId
                    def version    = pom.version
                    def packaging  = pom.packaging

                    def expected = "${artifactId}-${version}.${packaging}"

                    // Locate artifact
                    def artifacts = findFiles(glob: "target/${expected}")
                    if (artifacts.length == 0) {
                        error "Artifact not found: target/${expected}"
                    }

                    def artifactPath = artifacts[0].path
                    echo "Publishing ${artifactPath}"

                    nexusArtifactUploader(
                        nexusVersion: env.NEXUS_VERSION,
                        protocol: 'https',
                        nexusUrl: env.NEXUS_URL,
                        groupId: pom.groupId,
                        version: version,
                        repository: env.NEXUS_REPO,
                        credentialsId: env.NEXUS_CREDENTIALS_ID,
                        artifacts: [[
                            artifactId: artifactId,
                            file: artifactPath,
                            type: packaging
                        ]]
                    )
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.IMAGE_NAME = "${env.NEXUS_URL}/${env.DOCKER_REPO_PUSH}/${env.APP_NAME}:v${env.BUILD_NUMBER}"

                    docker.withRegistry("https://${env.NEXUS_URL}", "${env.DOCKER_CREDENTIALS_ID}") {                   
                        docker.build(env.IMAGE_NAME, "--build-arg DOCKER_PRIVATE_REPO=${env.NEXUS_URL}/${env.DOCKER_REPO_PULL} .")
                    }  

                    echo "Docker image built: ${env.IMAGE_NAME}"             
                }
            }
        }

        stage('Deploy Image to Nexus Docker Registry') {
            when { 
                expression { env.branchName == 'main' }
            }
            steps {
                script {
                    docker.withRegistry("https://${env.NEXUS_URL}", "${env.DOCKER_CREDENTIALS_ID}") {
                        def image = docker.image(env.IMAGE_NAME)
                        image.push()
                    }
                }
            }            
        }
    }

    post {
        always {
            script {
                if (env.IMAGE_NAME) {
                    sh "docker rmi ${env.IMAGE_NAME} || true"
                }
            }
        }
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'The pipeline encountered an error and did not complete successfully.'
        }
    }
}
