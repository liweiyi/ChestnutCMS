#!/bin/bash
DOCKER_IMAGE={{DOCKER_HUB_URL}}/{{IMAGE_REPOSITORY}}

# 删除docker镜像
NONE_IMAGE_ID_ARR=$(docker images | grep "${DOCKER_IMAGE}" | awk '{print $3}')
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
  echo ">>>>>delete docker image done: $NONE_IMAGE_ID"
done