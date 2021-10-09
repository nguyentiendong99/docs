#!/usr/bin/env groovy

node {
    stage('checkout') {
        checkout scm
    }

    stage('check java') {
        sh "java -version"
    }

    stage('clean') {
        sh "chmod +x mvnw"
        sh "./mvnw clean"
    }

    stage('packaging') {
        sh "./mvnw verify -Pprod -DskipTests"
        archiveArtifacts artifacts: '**/target/*.war', fingerprint: true
    }

    stage('deploy') {
        sh "kill \$(lsof -t -i:8088) > /dev/null 2> /dev/null || : "
        sh "cd ./target/ && java -jar lk-backend-0.0.1-SNAPSHOT.war --spring.profiles.active=prod"
    }
}
