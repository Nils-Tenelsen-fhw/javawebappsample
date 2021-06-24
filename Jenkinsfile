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
        'AZURE_STORAGE_KEY=login']) {
    stage('init') {
      checkout scm
      sh 'rm -r -f $WORKSPACE/target'
      sh 'rm -r -f $WORKSPACE/checkSum.txt'
    }

    stage('build') {
      sh 'build_calc.sh'
    }

    stage('unit tests') {
      sh 'mvn test'
    }

    stage('archive image') {
      def warName = 'calculator-1.0.war_' + BUILD_NUMBER
      def warNameDir = 'image_upload/' + warName
      archiveArtifacts artifacts: 'target/*.war', fingerprint: true
      archiveArtifacts artifacts: '**/*.jar', fingerprint: true
      sh 'mkdir -p image_upload'
      sh 'cp target/*.war image_upload'
      sh "mv image_upload/*.war $warNameDir"
      //login to azure
      withCredentials([usernamePassword(credentialsId: 'AzureServicePrincipal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
       sh '''
          az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID
          az account set -s $AZURE_SUBSCRIPTION_ID
        '''
      }
      //provide extra credentials for blob storage
      withCredentials([usernamePassword(credentialsId: 'AzureBlobKey', passwordVariable: 'AZURE_STORAGE_KEY', usernameVariable: 'storage_name')]) {
       //upload to blob storage
       sh "az storage blob upload-batch -d images -s image_upload --pattern $warName --account-name imageswas"
      }
      sh 'rm -r -f image_upload'
      sh 'az logout'
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

    stage('retrieve image') {
      def warName = 'calculator-1.0.war_' + BUILD_NUMBER
      def warNameDir = 'image_download/calculator-1.0.war_' + BUILD_NUMBER
      def warNameDirClean = 'image_download/calculator-1.0.war'
      sh 'mkdir -p image_download'
      //login to azure
      withCredentials([usernamePassword(credentialsId: 'AzureServicePrincipal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
       sh '''
          az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID
          az account set -s $AZURE_SUBSCRIPTION_ID
        '''
      }
      //provide extra credentials for blob storage
      withCredentials([usernamePassword(credentialsId: 'AzureBlobKey', passwordVariable: 'AZURE_STORAGE_KEY', usernameVariable: 'storage_name')]) {
       //download fromn blob storage
       sh "az storage blob download-batch -d image_download -s images --pattern $warName  --account-name imageswas"
      }
      sh 'az logout'
      sh "mv $warNameDir $warNameDirClean"
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

    stage('verify') {
        // verify package
      sh 'verify_calc.sh'
    }

    deleteDir()
  }
}
