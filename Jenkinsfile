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
            tmpdir=$(mktemp -d)
            trap 'rm -rf "$tmpdir"' EXIT

            printf '%s' "${PROD_DB_URL}" > "$tmpdir/PROD_DB_URL"
            printf '%s' "${PROD_DB_USER}" > "$tmpdir/PROD_DB_USER"
            printf '%s' "${PROD_DB_PASSWORD}" > "$tmpdir/PROD_DB_PASSWORD"
            printf '%s' "${PROD_REDIS_HOST}" > "$tmpdir/PROD_REDIS_HOST"
            printf '%s' "${PROD_REDIS_PORT}" > "$tmpdir/PROD_REDIS_PORT"
            printf '%s' "${PROD_REDIS_PASSWORD}" > "$tmpdir/PROD_REDIS_PASSWORD"
            printf '%s' "${PROD_S3_ENDPOINT}" > "$tmpdir/PROD_S3_ENDPOINT"
            printf '%s' "${PROD_S3_REGION}" > "$tmpdir/PROD_S3_REGION"
            printf '%s' "${PROD_S3_BUCKET}" > "$tmpdir/PROD_S3_BUCKET"
            printf '%s' "${PROD_S3_KEY}" > "$tmpdir/PROD_S3_KEY"
            printf '%s' "${PROD_S3_SECRET}" > "$tmpdir/PROD_S3_SECRET"
            printf '%s' "${PROD_JWT_SECRET}" > "$tmpdir/PROD_JWT_SECRET"
            printf '%s' "${PROD_JWT_EXPIRATION}" > "$tmpdir/PROD_JWT_EXPIRATION"
            printf '%s' "${PROD_JWT_REFRESH_EXPIRATION}" > "$tmpdir/PROD_JWT_REFRESH_EXPIRATION"
            printf '%s' "${AWS_MAIL_HOST}" > "$tmpdir/AWS_MAIL_HOST"
            printf '%s' "${AWS_MAIL_PORT}" > "$tmpdir/AWS_MAIL_PORT"
            printf '%s' "${AWS_MAIL_USERNAME}" > "$tmpdir/AWS_MAIL_USERNAME"
            printf '%s' "${AWS_MAIL_PASSWORD}" > "$tmpdir/AWS_MAIL_PASSWORD"
            printf '%s' "${AWS_MAIL_FROM}" > "$tmpdir/AWS_MAIL_FROM"
            printf '%s' "${SECURITY_ENC_KEY_BASE64}" > "$tmpdir/SECURITY_ENC_KEY_BASE64"
            printf '%s' "${SECURITY_HASH_PEPPER}" > "$tmpdir/SECURITY_HASH_PEPPER"

            kubectl -n "${K8S_NAMESPACE}" delete secret rhight-api-secret --ignore-not-found=true
            kubectl -n "${K8S_NAMESPACE}" create secret generic rhight-api-secret \
              --from-file=PROD_DB_URL="$tmpdir/PROD_DB_URL" \
              --from-file=PROD_DB_USER="$tmpdir/PROD_DB_USER" \
              --from-file=PROD_DB_PASSWORD="$tmpdir/PROD_DB_PASSWORD" \
              --from-file=PROD_REDIS_HOST="$tmpdir/PROD_REDIS_HOST" \
              --from-file=PROD_REDIS_PORT="$tmpdir/PROD_REDIS_PORT" \
              --from-file=PROD_REDIS_PASSWORD="$tmpdir/PROD_REDIS_PASSWORD" \
              --from-file=PROD_S3_ENDPOINT="$tmpdir/PROD_S3_ENDPOINT" \
              --from-file=PROD_S3_REGION="$tmpdir/PROD_S3_REGION" \
              --from-file=PROD_S3_BUCKET="$tmpdir/PROD_S3_BUCKET" \
              --from-file=PROD_S3_KEY="$tmpdir/PROD_S3_KEY" \
              --from-file=PROD_S3_SECRET="$tmpdir/PROD_S3_SECRET" \
              --from-file=PROD_JWT_SECRET="$tmpdir/PROD_JWT_SECRET" \
              --from-file=PROD_JWT_EXPIRATION="$tmpdir/PROD_JWT_EXPIRATION" \
              --from-file=PROD_JWT_REFRESH_EXPIRATION="$tmpdir/PROD_JWT_REFRESH_EXPIRATION" \
              --from-file=AWS_MAIL_HOST="$tmpdir/AWS_MAIL_HOST" \
              --from-file=AWS_MAIL_PORT="$tmpdir/AWS_MAIL_PORT" \
              --from-file=AWS_MAIL_USERNAME="$tmpdir/AWS_MAIL_USERNAME" \
              --from-file=AWS_MAIL_PASSWORD="$tmpdir/AWS_MAIL_PASSWORD" \
              --from-file=AWS_MAIL_FROM="$tmpdir/AWS_MAIL_FROM" \
              --from-file=SECURITY_ENC_KEY_BASE64="$tmpdir/SECURITY_ENC_KEY_BASE64" \
              --from-file=SECURITY_HASH_PEPPER="$tmpdir/SECURITY_HASH_PEPPER"

            rm -rf "$tmpdir"
            set -x

            sed "s|__IMAGE__|${IMAGE_URI}:${IMAGE_TAG}|g" k8s/prod/deployment.yaml | kubectl apply -f -
            kubectl apply -f k8s/prod/service.yaml
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
