pipeline {
    agent any

    environment {
            TOKEN = credentials('telegram-token')
            CHAT_ID = credentials('telegram-chat-id')
        }

    parameters {
        string(name: "Tag", defaultValue: "v0.0.1", description: "Enter tag for docker image")
    }

    stages {

        stage("Build docker image and push to hub"){
            steps {
                sh "TAG=${params.Tag} docker compose -f docker-compose.yml build"
                sh "docker image tag fintrack:${params.Tag} psammy171/unacero:fintrack-${params.Tag}"
                sh "docker image push psammy171/unacero:fintrack-${params.Tag}"
            }
        }

        stage("Start/Restart container"){
            steps {
                sh "docker stop fintrack || true"
                sh "docker rm fintrack || true"
                sh "docker images --format '{{.Repository}}:{{.Tag}}' | grep '^fintrack' | xargs -r docker rmi || true"
                sh "docker image tag psammy171/unacero:fintrack-${params.Tag} fintrack:${params.Tag}"
                sh "docker image rm psammy171/unacero:fintrack-${params.Tag}"
                sh "TAG=${params.Tag} docker compose -f docker-compose.yml up -d"
            }
        }
    }
    post {
        always {
            echo "Job Completed!"
        }
        success {
            sh """
                curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage \
                -d chat_id=${CHAT_ID} \
                --data-urlencode "text=✅ Fintrack backend built and deployed.\nJob Name : ${JOB_NAME}\nBuild Number : #${BUILD_NUMBER}\nTag : ${params.Tag}\nBuild URL : ${env.BUILD_URL}"
            """
        }
        failure {
            sh """
                curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage \
                -d chat_id=${CHAT_ID} \
                --data-urlencode "text=❌ Fintrack backend build failed.\nJob Name : ${JOB_NAME}\nBuild Number : #${BUILD_NUMBER}\nTag : ${params.Tag}\nBuild URL : ${env.BUILD_URL}"
            """
        }
    }
}
