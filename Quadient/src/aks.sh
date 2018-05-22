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