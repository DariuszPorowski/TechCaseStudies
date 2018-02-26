using System;
using System.IO;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Host;
using Microsoft.Azure.WebJobs.Extensions.EventGrid;
using ImageResizer;

namespace ImageProcessing
{
    public static class CreateThumb
    {
        [FunctionName("CreateThumb")]
        public static void Run([EventGridTrigger] EventGridEvent eventGridEvent, TraceWriter log)
        {
            var blobUri = new Uri((string)eventGridEvent.Data["url"]);

            var cloudBlob = StorageHelper.GetBlobReference(blobUri);
            string blobName = StorageHelper.GetBlobName(cloudBlob);

            var instructions = new Instructions
            {
                Width = GetThumbWidth(),
                Height = GetThumbHeight(),
                Mode = FitMode.Max,
                Scale = ScaleMode.DownscaleOnly
            };

            var outputBlob = StorageHelper.GetThumbnailBlobReference(blobName);

            using (MemoryStream inStream = new MemoryStream())
            {
                StorageHelper.RequestInputBlob(cloudBlob, inStream);
                inStream.Position = 0;

                using (MemoryStream outStream = new MemoryStream())
                {
                    try
                    {
                        ImageBuilder.Current.Build(new ImageJob(inStream, outStream, instructions));
                        outStream.Position = 0;
                        outputBlob.UploadFromStream(outStream);
                    }
                    catch (Exception e)
                    {
                        log.Info($"Exception caught at: {DateTime.UtcNow}, Message: {e.Message}");
                    }
                }
            }
        }

        private static int GetThumbWidth()
        {
            Int32 imageWidth;
            var imgWidth = Environment.GetEnvironmentVariable("THUMB_WIDTH");
            if (!Int32.TryParse(imgWidth, out imageWidth))
            {
                imageWidth = 400;
            }

            return imageWidth;
        }

        private static int GetThumbHeight()
        {
            Int32 imageHeight;
            var imgHeight = Environment.GetEnvironmentVariable("THUMB_HEIGHT");
            if (!Int32.TryParse(imgHeight, out imageHeight))
            {
                imageHeight = 300;
            }

            return imageHeight;
        }
    }
}
