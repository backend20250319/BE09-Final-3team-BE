pipeline {
    agent any

    environment {
        SOURCE_GITHUB_URL = 'git@github.com:backend20250319/BE09-Final-3team-BE.git'
        MANIFESTS_GITHUB_URL = 'https://github.com/gyongcode/petful-manifest.git'
        GIT_USERNAME = 'gyongcode-jenkins'
        GIT_EMAIL = 'gyongcode@gmail.com'
    }

    parameters {
        choice(
            name: 'SERVICE_NAME',
            choices: [
                'all',
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
            ],
            description: 'Select service to build and deploy'
        )
        string(
            name: 'TAG_VERSION',
            defaultValue: 'latest',
            description: 'Docker image tag version (default: latest)'
        )
    }

    stages {
        stage('Preparation') {
            steps {
                script {
                    bat 'docker --version'
                    bat 'java -version'
                    echo "Building service: ${params.SERVICE_NAME}"
                    echo "Tag version: ${params.TAG_VERSION}"
                }
            }
        }

        stage('Source Clone') {
            steps {
                git credentialsId: 'ssh-jenkins-github--key', 
                    branch: "${branch.split("/")[2]}", 
                    url: "${env.SOURCE_GITHUB_URL}"
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    def services = getServiceList()
                    
                    services.each { service ->
                        echo "Building and testing ${service}..."
                        bat "cd ${service} && .\\gradlew.bat clean build -x test"
                    }
                }
            }
        }

        stage('Container Build and Push') {
            steps {
                script {
                    def services = getServiceList()
                    
                    withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_PASSWORD', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                        
                        services.each { service ->
                            def imageTag = params.TAG_VERSION == 'latest' ? "v0.0.${currentBuild.number}" : params.TAG_VERSION
                            
                            echo "Building Docker image for ${service}..."
                            bat "cd ${service} && docker build -t ${DOCKER_USER}/petful-${service}:${imageTag} ."
                            bat "cd ${service} && docker build -t ${DOCKER_USER}/petful-${service}:latest ."
                            
                            echo "Pushing Docker image for ${service}..."
                            bat "docker push ${DOCKER_USER}/petful-${service}:${imageTag}"
                            bat "docker push ${DOCKER_USER}/petful-${service}:latest"
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
                    def services = getServiceList()
                    def imageTag = params.TAG_VERSION == 'latest' ? "v0.0.${currentBuild.number}" : params.TAG_VERSION
                    
                    services.each { service ->
                        echo "Updating manifest for ${service}..."
                        
                        // Update deployment manifest for each service
                        bat """
                            powershell -Command "if (Test-Path '${service}-deployment.yml') { 
                                (Get-Content '${service}-deployment.yml') -replace 'petful-${service}:.*', 'petful-${service}:${imageTag}' | Set-Content '${service}-deployment.yml'
                            }"
                        """
                        
                        // Also check for general deployment.yml
                        bat """
                            powershell -Command "if (Test-Path 'deployment.yml') { 
                                (Get-Content 'deployment.yml') -replace 'petful-${service}:.*', 'petful-${service}:${imageTag}' | Set-Content 'deployment.yml'
                            }"
                        """
                    }
                    
                    // Git operations
                    bat "git add ."
                    bat "git config --global user.name \"${env.GIT_USERNAME}\""
                    bat "git config --global user.email \"${env.GIT_EMAIL}\""
                    
                    def commitMessage = params.SERVICE_NAME == 'all' ? 
                        "[CI BE] update all services image tag to ${imageTag}" : 
                        "[CI BE] update ${params.SERVICE_NAME} image tag to ${imageTag}"
                    
                    bat "git commit -m \"${commitMessage}\" || echo 'No changes to commit'"
                    bat "git push origin main || echo 'Nothing to push'"
                }
            }
        }
    }

    post {
        always {
            script {
                bat 'docker logout'
                
                // Clean up Docker images to save space
                bat """
                    powershell -Command "
                        try {
                            docker images --format 'table {{.Repository}}:{{.Tag}}' | Where-Object { \\$_ -match 'petful-' -and \\$_ -notmatch 'latest' } | ForEach-Object {
                                if (\\$_ -ne 'REPOSITORY:TAG') {
                                    docker rmi \\$_ -f 2>\\$null
                                }
                            }
                        } catch {
                            Write-Host 'Docker cleanup completed or no images to clean'
                        }
                    "
                """
            }
        }
        success {
            echo 'Pipeline succeeded!'
            script {
                def services = getServiceList()
                def serviceNames = services.join(', ')
                echo "Successfully built and deployed: ${serviceNames}"
            }
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

def getServiceList() {
    if (params.SERVICE_NAME == 'all') {
        return [
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
    } else {
        return [params.SERVICE_NAME]
    }
}
