FROM alpine
LABEL maintainer="Dariusz.Porowski@microsoft.com"

COPY ./entrypoint.sh /bin/
RUN apk add --no-cache openssh \
    && sed -i s/#PermitRootLogin.*/PermitRootLogin\ yes/ /etc/ssh/sshd_config \
    && echo "root:Docker!" | chpasswd \
    && apk update \
    && rm -rf /var/cache/apk/* /tmp/* \
    && chmod 755 /bin/entrypoint.sh 

EXPOSE 22
ENTRYPOINT ["/bin/entrypoint.sh"]