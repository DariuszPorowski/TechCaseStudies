#!/bin/bash

# Example for ENV
# LIC_URL="https://quadient.blob.core.windows.net/lic/lic.zip?st=2018-02-22T00%3A00%3A00Z&se=2019-02-22T00%3A00%3A00Z&sp=rl&sv=2017-04-17&sr=b&sig=s2sb8xdoeL72bSL232%2FpZJQLY0g5Ngv79w4gDeVlxjw%3D"

mkdir /tmp/lic
curl -sS "$LIC_URL" > /tmp/lic/lic.zip
unzip -o /tmp/lic/lic.zip -d /opt/Quadient/InspireLicenseManager/
rm -f /tmp/lic/lic.zip