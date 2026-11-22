pipeline {
    agent any
    environment {
        IMAGE = 'rouaaissa26/events-project'
        AWS_DEFAULT_REGION = 'us-east-1'
    }
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps { sh 'chmod +x mvnw && ./mvnw clean package -DskipTests' }
        }
        stage('Tests JUnit') {
            steps { sh './mvnw test' }
            post { always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' } }
        }
        stage('SonarQube') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.token=$SONAR_TOKEN -Dsonar.qualitygate.wait=true'
                }
            }
        }
        stage('Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    sh './mvnw -s settings.xml deploy -DskipTests'
                }
            }
        }
        stage('Docker Build & Push') {
            steps {
                sh 'docker build -t $IMAGE:$BUILD_NUMBER -t $IMAGE:latest .'
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    sh 'echo $DH_PASS | docker login -u $DH_USER --password-stdin'
                    sh 'docker push $IMAGE:$BUILD_NUMBER'
                    sh 'docker push $IMAGE:latest'
                }
            }
        }
        stage('Terraform EKS') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
                    dir('terraform') {
                        sh 'terraform init -input=false'
                        sh 'terraform validate'
                        sh 'terraform apply -auto-approve -input=false'
                    }
                }
            }
        }
        stage('Update kubeconfig') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
                    sh 'aws eks update-kubeconfig --name events-cluster --region $AWS_DEFAULT_REGION --kubeconfig $WORKSPACE/kubeconfig'
                }
            }
        }
        stage('Deploy to AWS Kubernetes') {
            steps {
                withCredentials([
                    file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE'),
                    string(credentialsId: 'mysql-password', variable: 'MYSQL_PASSWORD')
                ]) {
                    sh '''
                        export KUBECONFIG=$WORKSPACE/kubeconfig
                        kubectl create secret generic mysql-secret --from-literal=password="$MYSQL_PASSWORD" --dry-run=client -o yaml | kubectl apply -f -
                        kubectl apply -f k8s-aws/mysql.yaml
                        kubectl apply -f k8s/app.yaml
                        kubectl set image deployment/events-app events-app=$IMAGE:$BUILD_NUMBER
                        kubectl rollout status deployment/events-app --timeout=300s
                        kubectl get nodes -o wide
                    '''
                }
            }
        }
    }
}