pipeline {
  agent any
  tools { jdk 'jdk17' }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    AWS_REGION = 'ap-northeast-2'
    ECR_REPO = 'rhight-api-dev'
    IMAGE_TAG = "${BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build and Test') {
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew :api:test :api:bootJar --no-daemon'
      }
    }

    stage('Build Docker Image') {
      steps {
        sh 'docker build -t ${ECR_REPO}:${IMAGE_TAG} .'
      }
    }

    stage('Resolve AWS Context') {
      steps {
        script {
          env.AWS_ACCOUNT_ID = sh(
            script: 'aws sts get-caller-identity --query Account --output text',
            returnStdout: true
          ).trim()
          env.IMAGE_URI = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.ECR_REPO}"
        }
      }
    }

    stage('Login and Push ECR') {
      steps {
        sh 'aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${IMAGE_URI%/*}'
        sh 'docker tag ${ECR_REPO}:${IMAGE_TAG} ${IMAGE_URI}:${IMAGE_TAG}'
        sh 'docker push ${IMAGE_URI}:${IMAGE_TAG}'
      }
    }
  }

  post {
    success {
      echo "Pushed image: ${IMAGE_URI}:${IMAGE_TAG}"
    }
  }
}
