pipeline {
    agent any
    environment {
        DOCKER_HUB_URL = 'registry.cn-hangzhou.aliyuncs.com'
        DOCKER_HUB_URL_VPC = 'registry-vpc.cn-hangzhou.aliyuncs.com'
        DOCKER_HUB_WORKSPACE = 'liweiyi'
        DINGTALK_ID = '0e390ce4-c612-4c26-bc84-c086439a2da8'
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
            description: '是否发布前端',
            name: 'DEPLOY_UI')
        choice(
            choices: [ 'N', 'Y' ],
            description: '是否发布工具站',
            name: 'DEPLOY_TOOLS')
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
        stage("Checkout") {
            steps {
				dir('./ChestnutCMS') {
					checkout([$class: 'GitSCM', branches: [[name: '*/dev']], extensions: [], userRemoteConfigs: [[credentialsId: 'gitea_lwy', url: 'https://ccgit.1000mz.com/liweiyi/ChestnutCMS']]])
				}
				dir('./ChestnutTools') {
					checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'gitea_lwy', url: 'https://ccgit.1000mz.com/liweiyi/ChestnutTools']]])
				}
            }
        }
        stage("Build") {
			when {
                expression { return params.DEPLOY_SERVER == 'Y' }
            }
            steps {
				dir('./ChestnutCMS') {
					withEnv(['JAVA_HOME=/var/jenkins_home/jdks/jdk17', 'PATH+JAVA=/var/jenkins_home/jdks/jdk17/bin']) {
						withMaven(maven: 'M3.9.9') {
							sh 'mvn -U clean package -pl chestnut-build/chestnut-official -am -Dmaven.test.skip=true'
						}
					}
				}
            }
        }
        stage("chestnut-official") {
			when {
                expression { return params.DEPLOY_SERVER == 'Y' }
            }
            steps {
            	withEnv(['APP_PATH=chestnut-build/chestnut-official', 'APP_NAME=chestnut-official']) {
   					echo "docker build start: ${APP_PATH}#${APP_VERSION}"
	            	dir('./ChestnutCMS') {
	                	withCredentials([usernamePassword(credentialsId: 'ALIYUN-DOCKER-REGISTRY-LWY', passwordVariable: 'DOCKERPWD', usernameVariable: 'DOCKERUSER')]) {
							sh '''
								cd ${APP_PATH}
			                    echo ${DOCKERPWD} | docker login --username=${DOCKERUSER} --password-stdin ${DOCKER_HUB_URL}
								docker build -t ${DOCKER_HUB_URL}/${DOCKER_HUB_WORKSPACE}/${APP_NAME}:${IMAGE_TAG} . --build-arg APP_NAME=${APP_NAME} --build-arg APP_VERSION=${APP_VERSION}
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
								sed -i "s/{{DOCKER_HUB_URL}}/${DOCKER_HUB_URL_VPC}/g" docker-image-clear.sh
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
		                    cp -f docker/docker-compose_${DEPLOY_ENV}.yml ${APP_PATH}

            	    		cd ${APP_PATH}
            	    		mv docker-compose_${DEPLOY_ENV}.yml docker-compose.yml

		                    sed -i "s/{{DOCKERUSER}}/${DOCKERUSER}/g" docker-deploy.sh
							sed -i "s/{{DOCKERPWD}}/${DOCKERPWD}/g" docker-deploy.sh
							sed -i "s/{{DOCKER_HUB_URL}}/${DOCKER_HUB_URL_VPC}/g" docker-deploy.sh
							sed -i "s/{{IMAGE_REPOSITORY}}/${DOCKER_HUB_WORKSPACE}\\/${APP_NAME}/g" docker-deploy.sh
							sed -i "s/{{IMAGE_TAG}}/${IMAGE_TAG}/g" docker-deploy.sh

							sed -i "s/{{DOCKER_IMAGE}}/${DOCKER_HUB_URL_VPC}\\/${DOCKER_HUB_WORKSPACE}\\/${APP_NAME}:${IMAGE_TAG}/g" docker-compose.yml
		            	    '''
							sshPublisher(publishers: [sshPublisherDesc(configName: 'lwy_dev', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''
										mkdir -p /home/admin/docker/chestnut-admin
										cd /home/admin/docker/chestnut-admin
			                            sh docker-deploy.sh
										''', execTimeout: 600000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'chestnut-admin/',
										remoteDirectorySDF: false, removePrefix: 'chestnut-build/chestnut-official/',
										sourceFiles: 'chestnut-build/chestnut-official/docker-compose.yml,chestnut-build/chestnut-official/docker-deploy.sh')],
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
					sshPublisher(publishers: [sshPublisherDesc(configName: 'lwy_dev', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''
								cd /home/admin/docker/chestnut-admin/wwwroot_release
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
            	dir('./ChestnutCMS/chestnut-ui-vue3') {
               		nodejs('nodejs_20_19_0') {
	            	    sh '''
	            	    npm install --registry=https://registry.npmmirror.com
	            	    npm run build:prod
	            	    cd dist
	            	    zip -q -r ui.zip *
	            	    '''
					}
            	}
				dir('./ChestnutCMS/chestnut-ui-vue3/dist') {
            	    sshPublisher(publishers: [sshPublisherDesc(configName: 'lwy_dev', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''
            	    					mkdir -p /home/admin/docker/chestnut-ui
										cd /home/admin/docker/chestnut-ui
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
        stage("chestnut-tools") {
			when {
                expression { return params.DEPLOY_TOOLS == 'Y' }
            }
            steps {
            	dir('./ChestnutTools') {
               		nodejs('nodejs_20_19_0') {
	            	    sh '''
	            	    npm install --registry=https://registry.npmmirror.com
	            	    npm run build
	            	    cd dist
	            	    zip -q -r tools.zip *
	            	    '''
					}
            	}
				dir('./ChestnutTools/dist') {
            	    sshPublisher(publishers: [sshPublisherDesc(configName: 'lwy_dev', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''
            	    					mkdir -p /home/admin/docker/chestnut-tools
										cd /home/admin/docker/chestnut-tools
			                            unzip -o -q tools.zip
			                            rm -f tools.zip
										''', execTimeout: 600000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'chestnut-tools/',
										remoteDirectorySDF: false, removePrefix: '',
										sourceFiles: 'tools.zip')],
										usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
					sh 'rm -f tools.zip'
				}
	        }
	    }
    }
}
