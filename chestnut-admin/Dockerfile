FROM openjdk:17-jdk-oraclelinux8 as builder

ARG APP_NAME
ARG APP_VERSION
ENV SPRING_PROFILES_ACTIVE=prod

WORKDIR /home/app

COPY target/$APP_NAME.jar app.jar

RUN java -Djarmode=layertools -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar extract

FROM openjdk:17-jdk-oraclelinux8

WORKDIR /home/app

COPY --from=builder /home/app/dependencies/ ./  

COPY --from=builder /home/app/spring-boot-loader/ ./  

COPY --from=builder /home/app/snapshot-dependencies/ ./ 

COPY --from=builder /home/app/application ./   

ENV TZ="Asia/Shanghai" \
    SERVER_PORT=8090 \
    JVM_OPTS="-Xms2g -Xmx2g -Xmn512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m" \
    JAVA_OPTS=""

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
	&& mkdir -p logs
    
VOLUME ["/home/app/logs","/home/app/uploadPath","/home/app/wwwroot_release","/home/app/_xy_member"]

EXPOSE $SERVER_PORT $NETTY_SOCKET_PORT

ENTRYPOINT ["sh","-c","java $JVM_OPTS $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]