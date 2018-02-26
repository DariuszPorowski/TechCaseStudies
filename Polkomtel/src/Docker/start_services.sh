#!/bin/bash

# Starting SSH
rc-service sshd start

# Starting Tomcat
/usr/local/tomcat/bin/catalina.sh run