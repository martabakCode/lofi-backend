pipeline {
    agent any

    environment {
        // Docker Hub credentials (configure in Jenkins)
        DOCKER_REGISTRY = credentials('docker-registry-url')
        DOCKER_CREDENTIALS = credentials('docker-credentials-id')
        
        // VPS Deployment credentials (configure in Jenkins)
        VPS_HOST = credentials('vps-host')
        VPS_USER = credentials('vps-user')
        VPS_SSH_KEY = credentials('vps-ssh-key')
        
        // Application configuration
        APP_NAME = 'lofiapps'
        DOCKER_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}"
        VERSION = "${env.BUILD_NUMBER}"
        
        // Maven configuration
        MAVEN_OPTS = '-Xmx1024m'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH_NAME = sh(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()
                }
            }
        }

        stage('Build Info') {
            steps {
                echo "Building branch: ${env.GIT_BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT_SHORT}"
                echo "Build number: ${env.BUILD_NUMBER}"
            }
        }

        stage('Code Quality - Spotless Check') {
            steps {
                sh './mvnw spotless:check'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './mvnw test -Dtest="*Test" -DexcludedGroups="IntegrationTest"'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }

        stage('Integration Tests') {
            steps {
                sh './mvnw test -Dgroups="IntegrationTest"'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build Application') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'release/*'
                }
            }
            steps {
                script {
                    def imageTag = "${DOCKER_IMAGE}:${VERSION}"
                    def latestTag = "${DOCKER_IMAGE}:latest"
                    
                    // Build Docker image
                    sh "docker build -t ${imageTag} -t ${latestTag} ."
                    
                    // Login to Docker registry
                    withCredentials([usernamePassword(credentialsId: 'docker-credentials-id', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "echo ${DOCKER_PASS} | docker login ${DOCKER_REGISTRY} -u ${DOCKER_USER} --password-stdin"
                    }
                    
                    // Push Docker image
                    sh "docker push ${imageTag}"
                    sh "docker push ${latestTag}"
                    
                    // Clean up local images
                    sh "docker rmi ${imageTag} ${latestTag} || true"
                }
            }
        }

        stage('Security Scan - Trivy') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    def imageTag = "${DOCKER_IMAGE}:${VERSION}"
                    
                    // Scan Docker image for vulnerabilities
                    sh """
                        trivy image \
                            --severity HIGH,CRITICAL \
                            --exit-code 0 \
                            --format table \
                            --output trivy-report.txt \
                            ${imageTag}
                    """
                    
                    // Archive the report
                    archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true
                }
            }
        }

        stage('Deploy to Development') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    deployToVPS('development')
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'release/*'
            }
            steps {
                script {
                    deployToVPS('staging')
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                // Require manual approval for production deployment
                input message: 'Deploy to Production?', ok: 'Deploy',
                      submitterParameter: 'APPROVER'
                
                script {
                    echo "Approved by: ${env.APPROVER}"
                    deployToVPS('production')
                }
            }
        }
    }

    post {
        always {
            // Clean workspace
            cleanWs(deleteDirs: true, notFailBuild: true)
            
            // Send notification
            script {
                def buildStatus = currentBuild.result ?: 'SUCCESS'
                def color = buildStatus == 'SUCCESS' ? 'good' : 'danger'
                def message = """
                    Build ${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                    Branch: ${env.GIT_BRANCH_NAME}
                    Commit: ${env.GIT_COMMIT_SHORT}
                    Duration: ${currentBuild.durationString}
                    <${env.BUILD_URL}|View Build>
                """.stripIndent()
                
                // Uncomment to enable Slack notification
                // slackSend(color: color, message: message)
            }
        }
        failure {
            echo 'Build failed! Check the logs for details.'
        }
        success {
            echo 'Build completed successfully!'
        }
    }
}

// Helper function for VPS deployment
def deployToVPS(String environment) {
    def composeFile = environment == 'production' ? 'docker-compose.prod.yml' : 'docker-compose.yml'
    def envFile = ".env.${environment}"
    
    sshagent(['vps-ssh-key']) {
        sh """
            ssh -o StrictHostKeyChecking=no ${VPS_USER}@${VPS_HOST} '
                set -e
                
                # Create deployment directory if not exists
                mkdir -p /opt/lofiapps/${environment}
                cd /opt/lofiapps/${environment}
                
                # Pull latest Docker image
                docker pull ${DOCKER_IMAGE}:${VERSION}
                
                # Stop existing containers
                docker-compose -f ${composeFile} down || true
                
                # Update environment file
                echo "VERSION=${VERSION}" > ${envFile}
                echo "DOCKER_IMAGE=${DOCKER_IMAGE}" >> ${envFile}
                
                # Start new containers
                VERSION=${VERSION} docker-compose -f ${composeFile} up -d
                
                # Clean up old images
                docker image prune -f
                
                # Health check
                sleep 10
                curl -f http://localhost:8080/actuator/health || exit 1
                
                echo "Deployment to ${environment} completed successfully!"
            '
        """
    }
}
