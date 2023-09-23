pipeline {
    agent any
    environment {
        DOCKER_HUB_URL = 'registry.cn-hangzhou.aliyuncs.com'
        DOCKER_HUB_WORKSPACE = 'xxxxxx'
        DINGTALK_ID = 'xxxxxx'
        // 获取Maven pom.xml项目版本号
        //APP_VERSION = readMavenPom().getVersion()
         APP_VERSION = '0.1.0'
    }
    parameters {
        choice(
            choices: [ 'N', 'Y' ],
            description: '是否发布服务端',
            name: 'DEPLOY_SERVER')
        choice(
            choices: [ 'N', 'Y' ],
            description: '是否发布wwwroot_release',
            name: 'DEPLOY_WWWROOT')
        choice(
            choices: [ 'N', 'Y' ],
            description: '是否发布前端',
            name: 'DEPLOY_UI')
        choice(
            choices: [ 'latest' ],
            description: '镜像TAG',
            name: 'IMAGE_TAG')
        choice(
            choices: [ 'prod' ],
            description: '发布环境',
            name: 'DEPLOY_ENV')
    }
    options {
        //设置在项目打印日志时带上对应时间
        timestamps()
        //不允许同时执行流水线，被用来防止同时访问共享资源等
        disableConcurrentBuilds()
        // 表示保留n次构建历史
        buildDiscarder(logRotator(numToKeepStr: '2'))
    }
    stages {
        stage("Preparing") {
            steps {
                dingtalk (
	                robot: '${DINGTALK_ID}',
	                type: 'MARKDOWN',
	                at: [],
	                atAll: false,
                    title: '(${JOB_NAME} #${BUILD_NUMBER})',
	                text: [
	                    '### 开始构建(${JOB_NAME} #${BUILD_NUMBER})',
	                    '---',
	                    '- DEPLOY_SERVER: ${DEPLOY_SERVER}',
	                    '- DEPLOY_UI: ${DEPLOY_UI}',
	                    '- IMAGE_TAG: ${IMAGE_TAG}',
	                    '- DEPLOY_ENV: ${DEPLOY_ENV}',
	                ],
                    messageUrl: '${BUILD_URL}',
                    picUrl: ''
                )
            }
        }
        stage("Checkout") {
            steps {
				dir('./ChestnutCMS') {
					checkout([$class: 'GitSCM', branches: [[name: '*/dev']], extensions: [], userRemoteConfigs: [[credentialsId: 'LwyGitee', url: 'https://gitee.com/liweiyi/ChestnutCMS.git']]])
				}
				dir('./wwwroot_release') {
					checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'gitea', url: 'http://gitea.huaray.com/liweiyi/swikoon_wwwroot_release.git']]])
				}
            }
        }
        stage("Build") {
			when {
                expression { return params.DEPLOY_SERVER == 'Y' }
            }
            steps {
				dir('./ChestnutCMS') {
					withEnv(['JAVA_HOME=/var/jenkins_home/jdk/jdk-17.0.4.1']) {
						withMaven(maven: 'M3.8') {
							sh 'mvn -U clean package -Dmaven.test.skip=true'
						}
					}
				}
            }
        }
        stage("chestnut-admin") {
			when {
                expression { return params.DEPLOY_SERVER == 'Y' }
            }
            steps {
            	withEnv(['APP_PATH=chestnut-admin', 'APP_NAME=chestnut-admin']) {
   					echo "docker build start: ${APP_PATH}#${APP_VERSION}"
	            	dir('./ChestnutCMS') {
	                	withCredentials([usernamePassword(credentialsId: 'ALIYUN-DOCKER-REGISTRY-LWY', passwordVariable: 'DOCKERPWD', usernameVariable: 'DOCKERUSER')]) {
							sh '''
								cd ${APP_PATH}
			                    echo ${DOCKERPWD} | docker login --username=${DOCKERUSER} --password-stdin ${DOCKER_HUB_URL}
								docker build -t ${DOCKER_HUB_URL}/${DOCKER_HUB_WORKSPACE}/${APP_NAME}:${IMAGE_TAG} . --build-arg APP_NAME=${APP_PATH} --build-arg APP_VERSION=${APP_VERSION}
			                    docker logout ${DOCKER_HUB_URL}
							'''
			            }
	            	}
   					echo "docker push start: ${APP_PATH}#${APP_VERSION}"
					dir('./ChestnutCMS') {
	                	withCredentials([usernamePassword(credentialsId: 'ALIYUN-DOCKER-REGISTRY-LWY', passwordVariable: 'DOCKERPWD', usernameVariable: 'DOCKERUSER')]) {
			                sh '''
			                    echo ${DOCKERPWD} | docker login --username=${DOCKERUSER} --password-stdin ${DOCKER_HUB_URL}
			                    docker push ${DOCKER_HUB_URL}/${DOCKER_HUB_WORKSPACE}/${APP_NAME}:${IMAGE_TAG}
			                    docker logout ${DOCKER_HUB_URL}
								
								cp -f bin/docker-image-clear.sh docker-image-clear.sh
								sed -i "s/{{DOCKER_HUB_URL}}/${DOCKER_HUB_URL}/g" docker-image-clear.sh
								sed -i "s/{{IMAGE_REPOSITORY}}/${DOCKER_HUB_WORKSPACE}\\/${APP_NAME}/g" docker-image-clear.sh
			                    /bin/bash docker-image-clear.sh
								rm -f docker-image-clear.sh
			                '''
			            }
	                }
	                // deploy
	                dir('./ChestnutCMS') {
		            	withCredentials([usernamePassword(credentialsId: 'ALIYUN-DOCKER-REGISTRY-LWY', passwordVariable: 'DOCKERPWD', usernameVariable: 'DOCKERUSER')]) {
		            	    sh '''
		            	    cp -f bin/docker-deploy.sh ${APP_PATH}
		            	    
            	    		cd ${APP_PATH}
		                    
		                    cp -f ../bin/docker-deploy.sh docker-deploy.sh
		                    
		                    sed -i "s/{{DOCKERUSER}}/${DOCKERUSER}/g" docker-deploy.sh
							sed -i "s/{{DOCKERPWD}}/${DOCKERPWD}/g" docker-deploy.sh
							sed -i "s/{{DOCKER_HUB_URL}}/${DOCKER_HUB_URL}/g" docker-deploy.sh
							sed -i "s/{{IMAGE_REPOSITORY}}/${DOCKER_HUB_WORKSPACE}\\/${APP_NAME}/g" docker-deploy.sh
							sed -i "s/{{IMAGE_TAG}}/${IMAGE_TAG}/g" docker-deploy.sh
										
		                    cp -f docker/docker-compose_${DEPLOY_ENV}.yml docker-compose.yml
		                    
							sed -i "s/{{DOCKER_IMAGE}}/${DOCKER_HUB_URL}\\/${DOCKER_HUB_WORKSPACE}\\/${APP_NAME}:${IMAGE_TAG}/g" docker-compose.yml
		            	    '''
							sshPublisher(publishers: [sshPublisherDesc(configName: 'GameCluster', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''
										mkdir -p /www/docker/chestnut-admin
										cd /www/docker/chestnut-admin
			                            sh docker-deploy.sh
			                            rm -f docker-deploy.sh
										''', execTimeout: 600000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'chestnut-admin/',
										remoteDirectorySDF: false, removePrefix: 'chestnut-admin/',
										sourceFiles: 'chestnut-admin/docker-compose.yml,chestnut-admin/docker-deploy.sh')],
										usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
			        	}
	                }
	
	                // delete tmp file
	                dir('./ChestnutCMS') {
			        	sh 'rm -f ${APP_PATH}/docker-deploy.sh'
			        	sh 'rm -f ${APP_PATH}/docker-compose.yml'
	                }
   					echo "build end: ${APP_PATH}"
  				}
            }
        }
        stage("wwwroot_release") {
			when {
                expression { return params.DEPLOY_WWWROOT == 'Y' }
            }
            steps {
				dir('./wwwroot_release') {
					sh 'zip -q -r wwwroot_release.zip * --exclude *.svn* --exclude *.git*'
					sshPublisher(publishers: [sshPublisherDesc(configName: 'GameCluster', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''
								cd /www/docker/chestnut-admin/wwwroot_release
								unzip -o -q wwwroot_release.zip
								rm -f wwwroot_release.zip
								''', execTimeout: 600000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'chestnut-admin/wwwroot_release/',
								remoteDirectorySDF: false, removePrefix: '', 
								sourceFiles: 'wwwroot_release.zip')], 
								usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
				}
			}
		}
        stage("chestnut-ui") {
			when {
                expression { return params.DEPLOY_UI == 'Y' }
            }
            steps {
            	dir('./ChestnutCMS/chestnut-ui') {
               		nodejs('NodeJS16_13') {
	            	    sh '''
	            	    npm install --registry=https://registry.npmmirror.com
	            	    npm run build:prod
	            	    cd dist
	            	    zip -q -r ui.zip *
	            	    '''
					}
            	}
				dir('./ChestnutCMS/chestnut-ui/dist') {
            	    sshPublisher(publishers: [sshPublisherDesc(configName: 'GameCluster', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''
            	    					mkdir -p /www/docker/chestnut-ui
										cd /www/docker/chestnut-ui
			                            unzip -o -q ui.zip
			                            rm -f ui.zip
										''', execTimeout: 600000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'chestnut-ui/',
										remoteDirectorySDF: false, removePrefix: '', 
										sourceFiles: 'ui.zip')], 
										usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
					sh 'rm -f ui.zip'
				}

	        }
	    }
    }
    post {
        success {
            dingtalk (
                robot: '${DINGTALK_ID}',
                type: 'MARKDOWN',
                at: [],
                atAll: false,
                title: '${JOB_NAME} #${BUILD_NUMBER}',
                text: [
                    '### 构建成功(${JOB_NAME} #${BUILD_NUMBER})',
                    '---',
                    '- IMAGE_TAG: ${IMAGE_TAG}',
                    '- DEPLOY_ENV: ${DEPLOY_ENV}',
                ],
                messageUrl: '${BUILD_URL}',
                picUrl: ''
            )
        }
        failure {
            dingtalk (
                robot: '${DINGTALK_ID}',
                type: 'LINK',
                at: [],
                atAll: false,
                title: '${JOB_NAME} #${BUILD_NUMBER}',
                text: [
                	'构建失败',
                ],
                messageUrl: '${BUILD_URL}',
                picUrl: ''
            )
        }
        unstable {
            dingtalk (
                robot: '${DINGTALK_ID}',
                type: 'LINK',
                at: [],
                atAll: false,
                title: '${JOB_NAME} #${BUILD_NUMBER}',
                text: [
                	'构建流程可能出现问题，详情请查看流程日志',
                ],
                messageUrl: '${BUILD_URL}',
                picUrl: ''
            )
        }
    }
}