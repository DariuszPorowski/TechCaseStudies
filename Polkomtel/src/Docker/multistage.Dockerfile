FROM maven:3.5-jdk-8-alpine AS build
COPY ./src /usr/src/app/src
COPY ./pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM tomcat:8.5-alpine AS final
LABEL maintainer="Dariusz.Porowski@microsoft.com"
COPY ./start_services.sh /bin/
RUN apk add --no-cache --update openssh-server \
	&& echo "root:Docker!" | chpasswd \
    && apk update \
    && apk add --no-cache openrc \
    && rc-status \
    && touch /run/openrc/softlevel \
    && rm -rf /var/cache/apk/* /tmp/* \
    && rm -rf /usr/local/tomcat/webapps/ \
    && chmod 755 /bin/init_container.sh  
COPY ./sshd_config /etc/ssh/
COPY --from=build /usr/src/app/target/demo.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080 2222
ENTRYPOINT ["/bin/start_services.sh"]