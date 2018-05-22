#!/bin/ash

# Generate host keys if not present
ssh-keygen -A

# Do not detach (-D), log to stderr (-e), passthrough other arguments
exec /usr/sbin/sshd -D -e "$@"