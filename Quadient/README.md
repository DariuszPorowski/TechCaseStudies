# From long semi-manual VMs setup to quick standardized solution deployment based on Docker containers and Kubernetes for legacy monolithic Java application
> Author: [Dariusz Porowski](https://www.linkedin.com/in/dariuszporowski/)
>
> Date: 2018-05-22

This article describes how we run Customer Communications Management (CCM) solutions from Quadient - Inspire on Microsoft Azure cloud using Azure Kubernetes Service (AKS) to simplify and standardize deployment model and speed up delivery time to Quadient's customers.

## Table of Contents
* [Executive summary](#executive-summary)
* [Customer profile](#customer-profile)
* [Key technologies](#key-technologies)
* [The Team](#the-team)
* [Problem statement](#problem-statement)
* [Solution, steps, and delivery](#solution-steps-and-delivery)
	* [Architecture](#architecture)
	* [Azure Infrastructure](#azure-infrastructure)
	* [SQL Database connection settings tokenization](#sql-database-connection-settings-tokenization)
	* [Customer unique license file for generic Docker image](#customer-unique-license-file-for-generic-docker-image)
	* [Kubernetes YAML files for Quadient's solution](#kubernetes-yaml-files-for-quadients-solution)
	* [Jump Host for SSH access](#jump-host-for-ssh-access)
* [Future work](#future-work)
	* [More containers](#more-containers)
	* [Helm](#helm)
* [Results and learning](#results-and-learning)
	* [Quadient - in their own words](#quadient---in-their-own-words)
* [References and additional resources](#references-and-additional-resources)

## Executive summary
Together with Quadient team, we have verified the possibility to run Quadient Inspire solution on containers managed by Azure Kubernetes Service (AKS). The goal for Quadient was to skip manual or semi-manual setup of infrastructure based on VMs, storage, network, OS configuration, etc. and create standardized deployment model using containers for their solution - monolithic application written in Java and run on Linux operating system.

## Customer profile
[Quadient](https://www.quadient.com/) combines the capabilities of three Neopost sister companies, GMC Software, Human Inference and Satori Software into one technology portfolio of solutions designed to improve customer experience by improving the customer journey across print, digital and social channels. Our solutions bring together and activate the entire organization in the name of customer experience, through better collaboration and visibility into the customer journey.

![Quadient](images/logo.png)

Quadient supports thousands of clients and partners worldwide in the financial services, insurance and service provider industries in their quest to achieve customer experience excellence via mobile, digital, social media and print technologies.

## Key technologies
During hackfest we used the following technologies and services:
* [Azure Kubernetes Service](https://azure.microsoft.com/en-us/services/container-service/) - Simplify the deployment, management, and operations of Kubernetes.
* [Azure SQL Database](https://azure.microsoft.com/en-us/services/sql-database/) - Managed relational SQL Database as a service.
* [Azure Blob Storage](https://azure.microsoft.com/en-us/services/storage/blobs/) - REST-based object storage for unstructured data.
* [Azure Container Registry](https://azure.microsoft.com/en-us/services/container-registry/) - Store and manage container images across all types of deployments.

## The Team
The project team consisted of the following members:
* [Dariusz Porowski](https://www.linkedin.com/in/dariuszporowski/) - Microsoft - Software Development Engineer
* [Lucie Vasickova](https://www.linkedin.com/in/lucie-va%C5%A1%C3%AD%C4%8Dkov%C3%A1-60175465) - Microsoft - Program Manager
* Petr Novotny - Quadient - Lead Software Architect
* Jaroslav Halamka - Quadient - Software Integration Test Engineer

![Hackfest members](images/hackfest.jpg)

*Figure 1. Hackfest members*

## Problem statement
Quadient has recognized that container technology brings excellent benefits. The benefits include architecture standardization, resource optimization, and change management. All of these are very important for large enterprise organizations. As enterprise infrastructures move to private or public clouds, adopting containers becomes even more important. The support of Kubernetes by the most used cloud platforms simplifies the development and delivery of complex enterprise systems for private clouds.

The goal of the project was to assess whether Kubernetes cluster managed by Azure Kubernetes Service (AKS) is a suitable platform for running Quadient's Customer Communication Management (CCM) solutions like **Inspire Scaler** and **Inspire Interactive**.

## Solution, steps, and delivery
The whole Quadient Inspire solution based on four main components initially launched every time on separate VMs. During tree days hackfest we decided to focus only on two parts - License Manager and Content Manager. Why? Because the architecture of two other components is similar to Content Manager. So, having a working solution for Content Manager, it's easy to adjust to other components - at the end, different is only in source container and TCP ports.

### Architecture
A first full day we spent on Architecture Design Session (ADS) to understand each other form technical perspective. That was the first touch with Quadien Inspire for Microsoft engineers, and first touch with Kubernetes and Azure Kubernetes Service (AKS) for Quadient team.

![Architecture Design Session (ADS)](images/ads.jpg)

*Figure 2. Architecture Design Session (ADS)*

After ADS we agreed on below architecture to try to achieve during next two days and validate Quadient's assumptions. More details about each part of our solution you will find later in this article.

![Hackfest architecture](images/architecture.jpg)

*Figure 3. Hackfest architecture*

### Azure Infrastructure
The first step after ADS what we have done was spin up a Kubernetes cluster using **Azure Kubernetes Service (AKS)** and private registry for containers with **Azure Container Registry (ACR)**.
We have written two small very basic bash scripts for that - [aks.sh](src/aks.sh) and [acr.sh](src/acr.sh).

```bash
#!/bin/bash

# Variables
location=westeurope
aksRgName=quadientk8shack
aksClusterName=quadientk8shack

# Enabling AKS
az provider register --namespace Microsoft.Network
az provider register --namespace Microsoft.Storage
az provider register --namespace Microsoft.Compute
az provider register --namespace Microsoft.ContainerService

# Create a resource group for AKS
az group create --name $aksRgName --location $location

# Create an AKS cluster
az aks create --resource-group $aksRgName --name $aksClusterName --generate-ssh-keys

# Connect to the cluster
az aks install-cli
az aks get-credentials --resource-group $aksRgName --name $aksClusterName
```

In production scenarios you should use a [Service Principal](https://msdn.microsoft.com/Library/Azure/Ad/Graph/api/entity-and-complex-type-reference#serviceprincipal-entity) for container registry access. Below script contains example for quick start and dev/test purposes and production with Service Principal. The SP role is **Reader**, and it is enough for docker pull access.

```bash
#!/bin/bash

# Variables
location=westeurope
acrRgName=quadientcr
acrName=quadientcr
acrSku=Basic

# Create a resource group for ACR
az group create --name $acrRgName --location $location

# Create an ACR (dev & test)
#az acr create --resource-group $acrRgName --name $acrName --sku $acrSku --admin-enabled true
#acrPass=$(az acr credential show --name $acrName --output tsv --query "passwords[0].value")

# Create an ACR
az acr create --resource-group $acrRgName --name $acrName --sku $acrSku --admin-enabled false

# Create a Service Principal for ACR
acrRegistryId=$(az acr show --name $acrName --query id --output tsv)
spPasswd=$(az ad sp create-for-rbac --name $acrName --scopes $acrRegistryId --role reader --query password --output tsv)
spAppId=$(az ad sp show --id http://$acrName --query appId --output tsv)

# Set ACR secrets 
acrLoginServer=$(az acr show --name $acrName --query "{acrLoginServer:loginServer}" --output tsv)
kubectl create secret docker-registry acr-secret --docker-server=$acrLoginServer --docker-username=$spAppId --docker-password=$spPasswd --docker-email=AKShackfest@quadient.com
```

### SQL Database connection settings tokenization
Quadient Inspire (Content Manager module) uses Microsoft SQL Server as a database engine, so changed it to **Azure SQL Database** was not a big deal - just configuration settings change in XML config file.

This small and easy change generated one issue. The application uses only local XML file for configuration, so we cannot use environment variables to pass connection detail to generic Docker image. We have solved it straightforwardly keeping generic Docker image and SQL configuration details in environment variables by Tokenization concept. We have created [configuration.xml](src/cm/configuration.xml) file based on existing working config file with unique token keys.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<LocalConfiguration>
   <!--
    Quadient Inspire configuration sections - omitted for this example
   -->
   <Database>
      <Username>__DB_USERNAME__</Username>
      <Password>__DB_PASSWORD__</Password>
      <DriverClassName>__DB_DRIVER__</DriverClassName>
      <Url>__DB_URL__</Url>
   </Database>
   <!--
    Quadient Inspire configuration sections - omitted for this example
   -->
</LocalConfiguration>
```

At the beginning of entrypoint file, we have added **sed** commands to replace tokens to real values provided in environment variables for the database inside config file. See [cm-db.sh](src/cm/cm-db.sh).

```bash
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
```

SQL Database settings we have stored as [Kubernetes Secrets](https://kubernetes.io/docs/concepts/configuration/secret/) and use during deployment as the environment variable. Below [example](src/cm/cm-db-secret.yaml) shows how to store secret data into Kubernetes Secrets with YAML file.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: cm-db-secret
type: Opaque
data:
  dbUsername: ZGJVc2VybmFtZQ==
  dbPassword: ZGJQYXNzd29yZA==
  dbDriver: bmV0LnNvdXJjZWZvcmdlLmp0ZHMuamRiYy5Ecml2ZXI=
  dbUrl: amRiYzpqdGRzOnNxbHNlcnZlcjovL3F1YWRpZW50LmRhdGFiYXNlLndpbmRvd3MubmV0OjE0MzMvZGJOYW1lO3ByZXBhcmVTUUw9MDtzc2w9cmVxdWVzdDtzb2NrZXRUaW1lb3V0PTkwMA==
```

> **Note**
>
> Before you store data into Kubernetes Secrets, you have to encode it to [Base64](https://en.wikipedia.org/wiki/Base64).

### Customer unique license file for generic Docker image
During ADS we discovered, one of part of the whole system (License Manager) uses license file located on local storage. Every customer has own license file, so we cannot use the same file for generic Docker image.

Our first idea was to keep license file externally on [Azure Files](https://docs.microsoft.com/en-us/azure/storage/files/storage-files-introduction) and use native Kubernetes feature - [Persistent Volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/). See Persistent volumes with [Azure Files](https://docs.microsoft.com/en-us/azure/aks/azure-files-dynamic-pv) documentation.

Finally, we doped this approach because after some crash tests we discovered container agility and recreation was not efficient for Quadient's team.

The new idea was much more straightforward to implement and efficient for this case. We have done similar approach like for SQL Database to keep generic Docker image and use environment variables to pass required data. In an entrypoint file for License Manager, we have added some bash commands to download .zip package with license file stored privately on [Azure Blob Storage](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction), and available only via unique per customer [Shared Access Signatures (SAS)](https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1) with an expiration time.

In [lm-lic.sh](src/lm/lm-lic.sh) script you can find example implementation.

```bash
#!/bin/bash

# Example for ENV
# LIC_URL="https://quadient.blob.core.windows.net/lic/lic.zip?st=2018-02-22T00%3A00%3A00Z&se=2019-02-22T00%3A00%3A00Z&sp=rl&sv=2017-04-17&sr=b&sig=s2sb8xdoeL72bSL232%2FpZJQLY0g5Ngv79w4gDeVlxjw%3D"

mkdir /tmp/lic
curl -sS "$LIC_URL" > /tmp/lic/lic.zip
unzip -o /tmp/lic/lic.zip -d /opt/Quadient/InspireLicenseManager/
rm -f /tmp/lic/lic.zip
```

> **Note**
>
> Generating unique dedicated SAS for files is possible to achieve from code, and it's highly recommended that approach, but this was not part of our scope. See [SAS examples](https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1#sas-examples) in C# to learn more.

Generated SAS we have stored as [Kubernetes Secrets](https://kubernetes.io/docs/concepts/configuration/secret/) and use during deployment as the environment variable. Below [example](src/lm/lm-lic-secret.yaml) shows how to store secret data into Kubernetes Secrets with YAML file.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: lm-lic
type: Opaque
data:
  url: aHR0cHM6Ly9xdWFkaWVudC5ibG9iLmNvcmUud2luZG93cy5uZXQvbGljL2xpYy56aXA/c3Q9MjAxOC0wMi0yMlQwMCUzQTAwJTNBMDBaJnNlPTIwMTktMDItMjJUMDAlM0EwMCUzQTAwWiZzcD1ybCZzdj0yMDE3LTA0LTE3JnNyPWImc2lnPXMyc2I4eGRvZUw3MmJTTDIzMiUyRnBaSlFMWTBnNU5ndjc5dzRnRGVWbHhqdyUzRA==
```

> **Note**
>
> Before you store data into Kubernetes Secrets, you have to encode it to [Base64](https://en.wikipedia.org/wiki/Base64).

### Kubernetes YAML files for Quadient's solution
After we finalized part of [SQL Database](#sql-database-connection-settings-tokenization) and [license file](#customer-unique-license-file-for-generic-docker-image), we have created samples for Kubernetes [Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) and [Service](https://kubernetes.io/docs/concepts/services-networking/service/) for [Content Manager](#content-manager) and [License Manager](#license-manager).

> **Note**
>
> Dockerfiles for Quadient's solution are not part of this article. There are Quadient's Intellectual Property (IP).

#### Content Manager
Deployment YAML file for Content Manager ([cm-deploy.yaml](src/cm/cm-deploy.yaml)) uses **secretKeyRef** to reads values stored in Kubernetes Secrets for [SQL Database](#sql-database-connection-settings-tokenization) connection and exposes two ports:
* 22 for troubleshooting during our tests
* 30353 for Content Manager service

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cm-deploy
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: cm
  template:
    metadata:
      name: cm
      labels:
        app: cm
        tier: cm
    spec:
      containers:
      - name: cm
        image: quadientcr.azurecr.io/cm:12.0.0.0-test
        imagePullPolicy: IfNotPresent
        ports:
        - name: ssh
          containerPort: 22
        - name: cm-30353
          containerPort: 30353
        env:
        - name: CX_LIC_SERVER
          value: lm-svc
        - name: ICM_PORT
          value: "30353"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: cm-db-secret
              key: dbUsername
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: cm-db-secret
              key: dbPassword
        - name: DB_DRIVER
          valueFrom:
            secretKeyRef:
              name: cm-db-secret
              key: dbDriver
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: cm-db-secret
              key: dbUrl
      dnsPolicy: ClusterFirst
      imagePullSecrets:
      - name: acr-secret
      restartPolicy: Always
```

Service YAML file for Content Manager ([cm-svc.yaml](src/cm/cm-svc.yaml)) generates external load balancer and exposes only one port - just for Content Manager service.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: cm-svc
spec:
  selector:
    app: cm
    tier: cm
  type: LoadBalancer
  ports:
  - name: cm-30353
    protocol: TCP
    port: 30353
    targetPort: 30353
```

#### License Manager
Deployment YAML file for License Manager ([lm-deploy.yaml](src/lm/lm-deploy.yaml)) is almost the same as for Content Manager. It uses **secretKeyRef** to reads values stored in Kubernetes Secrets for [license file](#customer-unique-license-file-for-generic-docker-image) SAS-based URL and exposes ports:
* 22 for troubleshooting during our tests
* 9961, 9962, 9963 for License Manager service

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lm-deploy
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: lm
  template:
    metadata:
      name: lm
      labels:
        app: lm
        tier: lm
    spec:
      containers:
      - name: lm
        image: quadientcr.azurecr.io/lm:11.0.0.0-test
        imagePullPolicy: IfNotPresent
        ports:
        - name: ssh
          containerPort: 22
        - name: lm-9961
          containerPort: 9961
        - name: lm-9962
          containerPort: 9962
        - name: lm-9963
          containerPort: 9963
        env:
        - name: LIC_URL
          valueFrom:
            secretKeyRef:
              name: lm-lic-secret
              key: url
      dnsPolicy: ClusterFirst
      imagePullSecrets:
      - name: acr-secret
      restartPolicy: Always
```

Service YAML file for License Manager ([lm-svc.yaml](src/lm/lm-svc.yaml)) exposes the service on a cluster-internal IP. This part of the solution does not need to be available externally for end users.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: lm-svc
spec:
  selector:
    app: lm
    tier: lm
  type: ClusterIP
  ports:
  - name: ssh
    protocol: TCP
    port: 22
    targetPort: 22
  - name: lm-9961
    protocol: TCP
    port: 9961
    targetPort: 9961
  - name: lm-9962
    protocol: TCP
    port: 9962
    targetPort: 9962
  - name: lm-9963
    protocol: TCP
    port: 9963
    targetPort: 9963
```

### Jump Host for SSH access
To simplify access to containers for troubleshooting and debugging only during our tests and development, we have created Jump Host exposed externally for SSH access bases on [alpine Docker image](https://hub.docker.com/_/alpine/).

Our [Deckerfile](src/jumphost/Dockerfile) add [OpenSSH](https://www.openssh.com) package to vanilla image and set basic settings.

```dockerfile
FROM alpine

COPY ./entrypoint.sh /bin/
RUN apk add --no-cache openssh \
    && sed -i s/#PermitRootLogin.*/PermitRootLogin\ yes/ /etc/ssh/sshd_config \
    && echo "root:Docker!" | chpasswd \
    && apk update \
    && rm -rf /var/cache/apk/* /tmp/* \
    && chmod 755 /bin/entrypoint.sh 

EXPOSE 22
ENTRYPOINT ["/bin/entrypoint.sh"]
```

> **Note**
>
> Dockerfile sets a default password for root user! It is only for testing purposes for a quick start. It is highly not recommended in the production environment.

[Entrypoint.sh](src/jumphost/entrypoint.sh) file used in above Dockerfile, generate host SSH keys if not present and runs SSD daemon.

```bash
#!/bin/ash

# Generate host keys if not present
ssh-keygen -A

# Do not detach (-D), log to stderr (-e), passthrough other arguments
exec /usr/sbin/sshd -D -e "$@"
```

Deployment YAML file for Jump Host ([jumphost-deploy.yaml](src/jumphost/jumphost-deploy.yaml)) looks like below. It exposes only 22 port and pulls Docker image from private container registry.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jumphost-deploy
spec:
  replicas: 1
  strategy:
    type: Recreate
  template:
    metadata:
      name: jumphost
      labels:
        app: jumphost
        tier: jumphost
    spec:
      containers:
      - name: jumphost
        image: quadientcr.azurecr.io/jumphost:1.0-test
        imagePullPolicy: IfNotPresent
        ports:
        - name: ssh
          containerPort: 22
      imagePullSecrets:
      - name: acr-secret
      restartPolicy: Always
```

Service YAML file for Jump Host ([jumphost-svc.yaml](src/jumphost/jumphost-svc.yaml)) looks like below. It setups external load balancer and exposes only one port for SSH connection.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: jumphost-svc
spec:
  selector:
    app: jumphost
    tier: jumphost
  type: LoadBalancer
  ports:
  - name: ssh
    protocol: TCP
    port: 22
    targetPort: 22
```

## Future work
In addition to the above, we have discovered more ideas. Unfortunately, the time of the hackfest did not allow us to cover all things. Below are recommendations and ideas for the further development of the application.

### More containers
The whole Quadient Inspire solution based on four independent modules. Each module runs on own instance. During hackfest, we have focused only on two tricky modules - to find a solution for loading external license file, and for the database connection. Another two modules are simple stateless applications (from the high-level architecture point of view) without any particular needs, so, next step for this is to create simple Deployment and Service.

### Helm
After having a whole working solution is good to transition it to a generalized package for Kubernetes. The answer for this is [Helm](https://helm.sh/) - the package manager for Kubernetes.
It's easier to install and upgrade even the most complex Kubernetes application. An additional benefit is to create multiple independent deployments of the same solution on one K8s cluster - to achieve, e.g., different test environments or separate dedicated environment per each customer.

## Results and learning
Some learning from Quadient and Microsoft cooperation:
* Complex solutions written over the years like multi-instance legacy monolithic Java application can be run on containers on top of Kubernetes cluster.
* Using modern solutions for mature applications extremely simplified deployment model and future operations.
* Using Containers instead of VMs reduce overall storage capacity and delivery time for end customers.
* Automatic new container recreation in a couple of seconds after application crash reduce downtime and increase SLA.

### Quadient - in their own words
> At hackfest, we talked to Dariusz about a migration of traditional 3-tier enterprise applications into the world of modern agile architecture. The lively discussions generated a lot of interesting ideas for development at Quadient. Even though we did not manage to finish the project in the course of Hackfest, Jaroslav (having information and examples from Dariusz) was able to launch the reference architecture based on Azure Kubernetes Service in the week following Hackfest.
> 
> — Petr Novotny - Lead Software Architect, Quadient

## References and additional resources
Below you can find useful links related to this project topic.
* [Azure Kubernetes Service (AKS) Documentation](https://docs.microsoft.com/en-us/azure/aks/)
* [Azure Container Registry (ACR) Documentation](https://docs.microsoft.com/en-us/azure/container-registry/)
* [Kubernetes Documentation](https://kubernetes.io/docs)
* [Helm Documentation](https://docs.helm.sh/)