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
        GIT_CREDENTIALS_ID = 'Git_Credential'

        // Nexus
        NEXUS_VERSION        = 'nexus3'
        NEXUS_URL            = '10.0.10.208:8081'  
        NEXUS_REPO           = 'myapp-maven-releases'
        NEXUS_CREDENTIALS_ID = 'Nexus_ID'
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
                    env.branchName = env.ref.replace('refs/heads/', '')
                    echo "Checking out branch: ${env.branchName}"
                }

                git(
                    branch: env.branchName,
                    credentialsId: env.GIT_CREDENTIALS_ID,
                    url: 'https://github.com/vincentino1/catalog-service.git'
                )
            }
        }

        stage('Install & Build') {
            steps {
                configFileProvider([
                    configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')
                ]) {
                    sh 'mvn -s $MAVEN_SETTINGS clean package'
                    sh 'mvn -s $MAVEN_SETTINGS deploy -X'
                }
            }
        }

        stage('Publish') {
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
                        protocol: 'http',
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
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'The pipeline encountered an error and did not complete successfully.'
        }
    }
}
