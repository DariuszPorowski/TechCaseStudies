using System;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.EventGrid;
using Microsoft.Azure.WebJobs.Host;
using Microsoft.WindowsAzure.Storage.Blob;

namespace ImageProcessing
{
    public static class DeleteImage
    {
        [FunctionName("DeleteImage")]
        public static void Run([EventGridTrigger] EventGridEvent eventGridEvent, TraceWriter log)
        {
            var deletedBlobUri = new Uri((string)eventGridEvent.Data["url"]);
            var deletedImage = new CloudBlob(deletedBlobUri);

            var otherImage = deletedImage.Container.Name.Equals(StorageHelper.GetImageContainerName()) ?
                StorageHelper.GetThumbnailBlobReference(deletedImage.Name) :
                StorageHelper.GetImageBlobReference(deletedImage.Name);
            try
            {
                otherImage.DeleteIfExists();
            }
            catch (Exception e)
            {
                log.Info($"Exception caught at: {DateTime.UtcNow}, Message: {e.Message}");
            }
        }
    }
}
