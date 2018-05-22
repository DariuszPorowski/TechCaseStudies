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