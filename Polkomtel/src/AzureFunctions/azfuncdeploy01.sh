#!/bin/bash

# Variables
location=northeurope
rgName=plkplmspoc
storageAccountName=plkplmspoc
functionName=plkplmspoc

# Create a Resource Group
az group create --name $rgName --location $location

# Create a Storage Account
az storage account create --name $storageAccountName --resource-group $rgName --sku Standard_LRS --location northeurope --kind StorageV2 --access-tier Hot

# Create Blob Storage containers
storageAccountKey=$(az storage account keys list --resource-group $rgName --account-name $storageAccountName --query [0].value --output tsv)
az storage container create --name images --account-name $storageAccountName --account-key $storageAccountKey --public-access blob
az storage container create --name thumbs --account-name $storageAccountName --account-key $storageAccountKey --public-access blob

# Create a Function App
az functionapp create --name $functionName --storage-account $storageAccountName --resource-group $rgName --consumption-plan-location $location

# Configure the Function App
storageAccountConnectionString=$(az storage account show-connection-string --resource-group $rgName --name $storageAccountName --query connectionString --output tsv)
az functionapp config appsettings set --name $functionName --resource-group $rgName --settings STORAGE_ACCOUNT_CS=$storageAccountConnectionString THUMB_CONTAINER_NAME=thumbs IMAGE_CONTAINER_NAME=images THUMB_WIDTH=400 THUMB_HEIGHT=300