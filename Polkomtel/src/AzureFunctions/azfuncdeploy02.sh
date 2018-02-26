#!/bin/bash

# Variables
rgName=plkplmspoc
storageAccountName=plkplmspoc
createThumbEndpoint="https://plkplmspoc.azurewebsites.net/admin/extensions/EventGridExtensionConfig?functionName=CreateThumb&code=iMXG6B4wds/a1YiMgXr9IEwtJWqILSraWZe0DFYZv2UvdASwP8Txhg=="
deleteImageEndpoint="https://plkplmspoc.azurewebsites.net/admin/extensions/EventGridExtensionConfig?functionName=DeleteImage&code=iMXG6B4wds/a1YiMgXr9IEwtJWqILSraWZe0DFYZv2UvdASwP8Txhg=="

# Create Event Grid event subscription to the Storage Account
storageAccountId=$(az storage account show --name $storageAccountName --resource-group $rgName --query id --output tsv)
az eventgrid event-subscription create --name createThumbSub --endpoint $createThumbEndpoint --resource-id $storageAccountId --endpoint-type webhook --included-event-types Microsoft.Storage.BlobCreated --subject-begins-with /blobServices/default/containers/images/blobs/
az eventgrid event-subscription create --name deleteImageSub --endpoint $deleteImageEndpoint --resource-id $storageAccountId --endpoint-type webhook --included-event-types Microsoft.Storage.BlobDeleted --subject-begins-with /blobServices/default/containers/images/blobs/