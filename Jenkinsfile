pipeline {
  agent any
  tools { jdk 'jdk17' }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    AWS_REGION = 'ap-northeast-2'
    EKS_CLUSTER_NAME = 'rhight-prod'
    K8S_NAMESPACE = 'prod'
    ECR_REPO = 'rhight-api-prod'
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

    stage('Deploy to EKS') {
      steps {
        withCredentials([
          string(credentialsId: 'PROD_DB_URL', variable: 'PROD_DB_URL'),
          string(credentialsId: 'PROD_DB_USER', variable: 'PROD_DB_USER'),
          string(credentialsId: 'PROD_DB_PASSWORD', variable: 'PROD_DB_PASSWORD'),
          string(credentialsId: 'PROD_REDIS_HOST', variable: 'PROD_REDIS_HOST'),
          string(credentialsId: 'PROD_REDIS_PORT', variable: 'PROD_REDIS_PORT'),
          string(credentialsId: 'PROD_REDIS_PASSWORD', variable: 'PROD_REDIS_PASSWORD'),
          string(credentialsId: 'PROD_S3_ENDPOINT', variable: 'PROD_S3_ENDPOINT'),
          string(credentialsId: 'PROD_S3_REGION', variable: 'PROD_S3_REGION'),
          string(credentialsId: 'PROD_S3_BUCKET', variable: 'PROD_S3_BUCKET'),
          string(credentialsId: 'PROD_S3_KEY', variable: 'PROD_S3_KEY'),
          string(credentialsId: 'PROD_S3_SECRET', variable: 'PROD_S3_SECRET'),
          string(credentialsId: 'PROD_JWT_SECRET', variable: 'PROD_JWT_SECRET'),
          string(credentialsId: 'PROD_JWT_EXPIRATION', variable: 'PROD_JWT_EXPIRATION'),
          string(credentialsId: 'PROD_JWT_REFRESH_EXPIRATION', variable: 'PROD_JWT_REFRESH_EXPIRATION'),
          string(credentialsId: 'AWS_MAIL_HOST', variable: 'AWS_MAIL_HOST'),
          string(credentialsId: 'AWS_MAIL_PORT', variable: 'AWS_MAIL_PORT'),
          string(credentialsId: 'AWS_MAIL_USERNAME', variable: 'AWS_MAIL_USERNAME'),
          string(credentialsId: 'AWS_MAIL_PASSWORD', variable: 'AWS_MAIL_PASSWORD'),
          string(credentialsId: 'AWS_MAIL_FROM', variable: 'AWS_MAIL_FROM'),
          string(credentialsId: 'SECURITY_ENC_KEY_BASE64', variable: 'SECURITY_ENC_KEY_BASE64'),
          string(credentialsId: 'SECURITY_HASH_PEPPER', variable: 'SECURITY_HASH_PEPPER')
        ]) {
          sh '''
            aws eks update-kubeconfig --region "${AWS_REGION}" --name "${EKS_CLUSTER_NAME}"
            kubectl get namespace "${K8S_NAMESPACE}" >/dev/null 2>&1 || kubectl create namespace "${K8S_NAMESPACE}"

            set +x
            kubectl -n "${K8S_NAMESPACE}" create secret generic rhight-api-secret \
              --from-literal=PROD_DB_URL="${PROD_DB_URL}" \
              --from-literal=PROD_DB_USER="${PROD_DB_USER}" \
              --from-literal=PROD_DB_PASSWORD="${PROD_DB_PASSWORD}" \
              --from-literal=PROD_REDIS_HOST="${PROD_REDIS_HOST}" \
              --from-literal=PROD_REDIS_PORT="${PROD_REDIS_PORT}" \
              --from-literal=PROD_REDIS_PASSWORD="${PROD_REDIS_PASSWORD}" \
              --from-literal=PROD_S3_ENDPOINT="${PROD_S3_ENDPOINT}" \
              --from-literal=PROD_S3_REGION="${PROD_S3_REGION}" \
              --from-literal=PROD_S3_BUCKET="${PROD_S3_BUCKET}" \
              --from-literal=PROD_S3_KEY="${PROD_S3_KEY}" \
              --from-literal=PROD_S3_SECRET="${PROD_S3_SECRET}" \
              --from-literal=PROD_JWT_SECRET="${PROD_JWT_SECRET}" \
              --from-literal=PROD_JWT_EXPIRATION="${PROD_JWT_EXPIRATION}" \
              --from-literal=PROD_JWT_REFRESH_EXPIRATION="${PROD_JWT_REFRESH_EXPIRATION}" \
              --from-literal=AWS_MAIL_HOST="${AWS_MAIL_HOST}" \
              --from-literal=AWS_MAIL_PORT="${AWS_MAIL_PORT}" \
              --from-literal=AWS_MAIL_USERNAME="${AWS_MAIL_USERNAME}" \
              --from-literal=AWS_MAIL_PASSWORD="${AWS_MAIL_PASSWORD}" \
              --from-literal=AWS_MAIL_FROM="${AWS_MAIL_FROM}" \
              --from-literal=SECURITY_ENC_KEY_BASE64="${SECURITY_ENC_KEY_BASE64}" \
              --from-literal=SECURITY_HASH_PEPPER="${SECURITY_HASH_PEPPER}" \
              --dry-run=client -o yaml | kubectl apply -f -
            set -x

            kubectl -n "${K8S_NAMESPACE}" set image deployment/rhight-api app="${IMAGE_URI}:${IMAGE_TAG}"
            kubectl -n "${K8S_NAMESPACE}" rollout status deployment/rhight-api --timeout=180s
          '''
        }
      }
    }
  }

  post {
    success {
      echo "Pushed image: ${IMAGE_URI}:${IMAGE_TAG}"
    }
  }
}
