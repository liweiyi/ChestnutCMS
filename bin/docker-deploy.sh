#!/bin/bash
DOCKERUSER={{DOCKERUSER}}
DOCKERPWD={{DOCKERPWD}}
DOCKER_HUB_URL={{DOCKER_HUB_URL}}
IMAGE_REPOSITORY={{IMAGE_REPOSITORY}}
IMAGE_TAG={{IMAGE_TAG}}

DOCKER_IMAGE=${DOCKER_HUB_URL}/${IMAGE_REPOSITORY}
# pull docker image
echo ${DOCKERPWD} | docker login --username=${DOCKERUSER} --password-stdin ${DOCKER_HUB_URL}
docker pull ${DOCKER_IMAGE}:${IMAGE_TAG}
docker logout ${DOCKER_HUB_URL}

# 处理none镜像
NONE_IMAGE_ID_ARR=$(docker images | grep "${DOCKER_IMAGE}" | grep "<none>" | awk '{print $3}')
for NONE_IMAGE_ID in ${NONE_IMAGE_ID_ARR[*]}; do
  # 停止容器
  NONE_RUNNING_CONTAINER_ID_ARR=$(docker ps -a | grep $NONE_IMAGE_ID | awk '{print $1}')
  for NONE_RUNNING_CONTAINER_ID in ${NONE_RUNNING_CONTAINER_ID_ARR[*]}; do
    docker stop ${NONE_RUNNING_CONTAINER_ID}
    echo ">>>>> Stop docker container done. CONTAINER_ID: ${NONE_RUNNING_CONTAINER_ID}"
  done
  # 删除容器
  NONE_STOPPED_CONTAINER_ID_ARR=$(docker ps -a | grep $NONE_IMAGE_ID | grep 'Exited' | awk '{print $1}')
  for NONE_STOPPED_CONTAINER_ID in ${NONE_STOPPED_CONTAINER_ID_ARR[*]}; do
    docker rm ${NONE_STOPPED_CONTAINER_ID}
    echo ">>>>> Delete docker container done. CONTAINER_ID: ${NONE_STOPPED_CONTAINER_ID}"
  done
  # 删除镜像
  docker rmi $NONE_IMAGE_ID
  echo ">>>>>delete docker <none> image done: $NONE_IMAGE_ID"
done

# 启动容器，老版本命令是docker-compose up -d
docker compose up -d