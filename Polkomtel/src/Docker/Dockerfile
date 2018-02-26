FROM tomcat:8.5-alpine
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
    && rm -rf /usr/local/tomcat/conf/server.xml \
    && chmod 755 /bin/start_services.sh
COPY ./sshd_config /etc/ssh/
COPY ./server.xml /usr/local/tomcat/conf/server.xml
COPY ./target/cms.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080 2222
ENTRYPOINT ["/bin/start_services.sh"]