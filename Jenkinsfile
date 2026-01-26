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
            regexpFilterExpression: '^catalog-service:refs/heads/main$'
        ]
    ])
])

pipeline {
    agent any

    tools {
        maven 'maven 3.6' // Name must match the one you configured in Jenkins
    }
        environment {
        // credentials for git
        GIT_CREDENTIALS = 'Git_Credential'
    }
    
    stages {

        stage('Webhook Debug') {
            steps {
                echo "Branch: ${env.ref}"
                echo "Repo: ${env.repo_name}"
            }
        }

        stage('Clean Workspace') {
            steps {
                echo "Deleting workspace..."
                cleanWs()   // or use deleteDir()
            }
        }
        
        stage('Checkout') {
            steps {
                script {
                    
                    env.branchName = (env.ref ?: 'refs/heads/main').replace('refs/heads/', '')
                    echo "Checking out branch: ${env.branchName}"   
                }
                git(
                    branch: env.branchName,
                    credentialsId: "${env.GIT_CREDENTIALS}",
                    url: 'https://github.com/vincentino1/catalog-service.git'
                )
            }
        }

        stage('Install Dependencies') {
            steps {
                    sh 'mvn clean install'
            }
        }
    }
}
