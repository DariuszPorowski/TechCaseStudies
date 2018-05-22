#!/bin/bash

# Example for ENV
# DB_USERNAME="dbUsername"
# DB_PASSWORD="dbPassword"
# DB_DRIVER="net.sourceforge.jtds.jdbc.Driver"
# DB_URL="jdbc:jtds:sqlserver://quadient.database.windows.net:1433/dbName;prepareSQL=0;ssl=request;socketTimeout=900"

sed -i "s|__DB_USERNAME__|$DB_USERNAME|g" configuration.xml
sed -i "s|__DB_PASSWORD__|$DB_PASSWORD|g" configuration.xml
sed -i "s|__DB_DRIVER__|$DB_DRIVER|g" configuration.xml
sed -i "s|__DB_URL__|$DB_URL|g" configuration.xml