import groovy.json.JsonSlurper

def getFtpPublishProfile(def publishProfilesJson) {
  def pubProfiles = new JsonSlurper().parseText(publishProfilesJson)
  for (p in pubProfiles)
    if (p['publishMethod'] == 'FTP')
      return [url: p.publishUrl, username: p.userName, password: p.userPWD]
}

node {
  withEnv(['AZURE_SUBSCRIPTION_ID=078464ec-10e7-409d-a3dd-633527f88d50',
        'AZURE_TENANT_ID=26aa7fd9-ab4d-4fc6-9a5f-99d4882840bc',
        'AZURE_STORAGE_AUTH_MODE=login']) {
    stage('init') {
      checkout scm
    }

    stage('build') {
      sh 'mvn clean package -Dmaven.test.skip=true'
    }

    stage('unit tests') {
      sh 'mvn test'
    }

    stage('archive image') {
      archiveArtifacts artifacts: 'target/*.war', fingerprint: true
      archiveArtifacts artifacts: '**/*.jar', fingerprint: true
      sh 'mkdir -p image_upload'
      sh 'cp target/*.war image_upload'
      withCredentials([usernamePassword(credentialsId: 'AzureServicePrincipal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
       sh '''
          az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID
          az account set -s $AZURE_SUBSCRIPTION_ID
        '''
       sh 'az storage blob upload-batch -d images -s image_upload --pattern calculator-???.war --account-name imagewas'
      }
      sh 'rm -r -f image_upload'
    }

    stage('test deploy') {
      def resourceGroup = 'pipeline-rg'
      def webAppName = 'pipeline-test-was'
      // login Azure
      withCredentials([usernamePassword(credentialsId: 'AzureServicePrincipal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
       sh '''
          az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID
          az account set -s $AZURE_SUBSCRIPTION_ID
        '''
      }
      // get publish settings
      def pubProfilesJson = sh script: "az webapp deployment list-publishing-profiles -g $resourceGroup -n $webAppName", returnStdout: true
      def ftpProfile = getFtpPublishProfile pubProfilesJson
      // upload package
      sh "curl -T target/calculator-1.0.war $ftpProfile.url/webapps/ROOT.war -u '$ftpProfile.username:$ftpProfile.password'"
      // log out
      sh 'az logout'
    }

    stage('tests by hand') {
      sh 'sleep 5'
      input("Ready to proceed?")
    }

    stage('prod deploy') {
      def resourceGroup = 'pipeline-rg'
      def webAppName = 'pipeline-app-was'
      // login Azure
      withCredentials([usernamePassword(credentialsId: 'AzureServicePrincipal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
       sh '''
          az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID
          az account set -s $AZURE_SUBSCRIPTION_ID
        '''
      }
      // get publish settings
      def pubProfilesJson = sh script: "az webapp deployment list-publishing-profiles -g $resourceGroup -n $webAppName", returnStdout: true
      def ftpProfile = getFtpPublishProfile pubProfilesJson
      // upload package
      sh "curl -T target/calculator-1.0.war $ftpProfile.url/webapps/ROOT.war -u '$ftpProfile.username:$ftpProfile.password'"
      // log out
      sh 'az logout'
    }
  }
}
