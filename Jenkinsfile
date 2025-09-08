pipeline {
    agent any

    environment {
        SOURCE_GITHUB_URL = 'git@github.com:backend20250319/BE09-Final-3team-BE.git'
        MANIFESTS_GITHUB_URL = 'https://github.com/gyongcode/petful-manifest.git'
        GIT_USERNAME = 'gyongcode-jenkins'
        GIT_EMAIL = 'gyongcode@gmail.com'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Preparation') {
            steps {
                script {
                    bat 'docker --version'
                    bat 'java -version'
                    echo "Building all services with latest tag"
                }
            }
        }

        stage('Source Clone') {
            steps {
                git credentialsId: 'ssh-jenkins-github--key',
                    branch: "${branch.split("/", 3)[2]}",
                    url: "${env.SOURCE_GITHUB_URL}"
            }
        }

        stage('Container Build and Push') {
            steps {
                script {
                    def services = [
                        'advertiser-service',
                        'campaign-service',
                        'community-service',
                        'config-service',
                        'discovery-service',
                        'gateway-service',
                        'health-service',
                        'notification-service',
                        'pet-service',
                        'sns-service',
                        'user-service'
                    ]

                    withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_PASSWORD', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {

                        bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"


                        services.each { service ->
                            echo "Building Docker image for ${service}..."
                            IMAGE_TAG = "v0.0.${currentBuild.number}"
                            bat "cd ${service} && docker build -t ${DOCKER_USER}/petful-${service}:${IMAGE_TAG} ."

                            echo "Pushing Docker image for ${service}..."
                            bat "docker push ${DOCKER_USER}/petful-${service}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('K8S Manifest Update') {
            steps {
                git credentialsId: 'github-access-token',
                    url: "${env.MANIFESTS_GITHUB_URL}",
                    branch: 'main'

                script {
                    def services = [
                        'advertiser-service',
                        'campaign-service',
                        'community-service',
                        'config-service',
                        'discovery-service',
                        'gateway-service',
                        'health-service',
                        'notification-service',
                        'pet-service',
                        'sns-service',
                        'user-service'
                    ]

                    services.each { service ->
                        echo "Updating manifest for ${service}..."

                        bat "powershell -Command \"if (Test-Path 'deployment.yml') { (Get-Content 'deployment.yml') -replace 'gyongcode/petful-${service}:.*', 'gyongcode/petful-${service}:v0.0.${currentBuild.number}' | Set-Content 'deployment.yml' }\""
                    }

                    bat "git add ."
                    bat "git config --global user.name \"${env.GIT_USERNAME}\""
                    bat "git config --global user.email \"${env.GIT_EMAIL}\""
                    bat "git commit -m \"[CI BE] update all services image tag to v0.0.${currentBuild.number}\""
                    bat "git push origin main"
                }
            }
        }
    }

    post {
        always {
            script {
                bat 'docker logout'
            }
        }
        success {
            echo 'Pipeline succeeded!'
            echo 'Successfully built and deployed all backend services'
            bat '''
                curl -H "Content-Type: application/json" \
                  -d '{"content":"✅ Jenkins Backend Job 성공: ${JOB_NAME} #${BUILD_NUMBER}"}' \
                  https://discord.com/api/webhooks/1414418036793938040/0MKgCC_IZICWO4mBWRINcHamWZfijFXGuxuhhGqsclBnouE8SvWPazg_aPmwYfU096sPhttps://discord.com/api/webhooks/WEBHOOK_ID/WEBHOOK_TOKEN
                '''
        }
        failure {
            echo 'Pipeline failed!'
            bat '''
                curl -H "Content-Type: application/json" \
                  -d '{"content":"❌ Jenkins Backend Job 실패: ${JOB_NAME} #${BUILD_NUMBER}"}' \
                  https://discord.com/api/webhooks/1414418036793938040/0MKgCC_IZICWO4mBWRINcHamWZfijFXGuxuhhGqsclBnouE8SvWPazg_aPmwYfU096sPhttps://discord.com/api/webhooks/WEBHOOK_ID/WEBHOOK_TOKEN
                '''
        }
    }
}
