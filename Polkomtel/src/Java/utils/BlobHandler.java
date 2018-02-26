@ApplicationScope
@Component
public class BlobHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String storageConnectionString = "";
    private CloudBlobContainer imagesContainer;
    private CloudBlobContainer thumbsContainer;
    private CloudBlobContainer filesContainer;


    public String uploadFile() {

        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            CloudBlobContainer container = serviceClient.getContainerReference("images");
            container.createIfNotExists();

            CloudBlockBlob blob = container.getBlockBlobReference("image.jpg");
            File sourceFile = new File("c:\\image.jpg");
            blob.upload(new FileInputStream(sourceFile), sourceFile.length());

            return blob.getQualifiedUri().toString();

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.print("FileNotFoundException encountered: ");
            logger.debug(fileNotFoundException.getMessage());
            System.exit(-1);
        } catch (StorageException storageException) {
            System.out.print("StorageException encountered: ");
            logger.debug(storageException.getMessage());
            System.exit(-2);
        } catch (Exception e) {
            System.out.print("Exception encountered: ");
            logger.debug(e.getMessage());
            System.exit(-3);
        }
        return "err";
    }

    public File uploadMultipartFile(MultipartFile file) {
        try {
            return multipartToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BlobHandler() {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            imagesContainer = serviceClient.getContainerReference("images");
            thumbsContainer = serviceClient.getContainerReference("thumbs");
            filesContainer = serviceClient.getContainerReference("files");
            imagesContainer.createIfNotExists();
            thumbsContainer.createIfNotExists();
            filesContainer.createIfNotExists();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String uploadToCloud(File sourceFile, Map < String, String > metadata) {
        try {


            String fileName = sourceFile.getName();
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                extension = fileName.substring(i);
            }
            UUID uuid = UUID.randomUUID();
            CloudBlockBlob blob = imagesContainer.getBlockBlobReference(uuid.toString() + extension);

            HashMap < String, String > meta = new HashMap < > ();
            if (metadata != null) {
                meta.putAll(metadata);
            }

            meta.put("name", fileName);
            blob.setMetadata(meta);

            blob.upload(new FileInputStream(sourceFile), sourceFile.length());
            return blob.getName();

        } catch (Exception e) {
            System.out.print("Exception encountered: ");
            e.printStackTrace();
            System.exit(-3);
        }
        return null;
    }

    public void uploadFile(MultipartFile file) {
        //todo
    }

    public void justUpload() {
        //todo
    }

    private File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException {

        File convFile = new File(multipart.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(multipart.getBytes());
        fos.close();
        return convFile;
    }

    public List < BlobData > listBlobs(String contName) {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            CloudBlobContainer container = serviceClient.getContainerReference(contName);
            List < BlobData > blobDataList = new ArrayList < > ();
            List < CloudBlockBlob > cloudBlockBlobList = new ArrayList < > ();

            container.listBlobs().forEach(listBlobItem -> cloudBlockBlobList.add((CloudBlockBlob) listBlobItem));

            for (CloudBlockBlob blob: cloudBlockBlobList) {
                blob.downloadAttributes();
                BlobData blobData = new BlobData();
                blobData.setBlobName(blob.getName());
                blobData.setUri(blob.getUri().toString());

                blobData.setMetadata(blob.getMetadata());
                blobDataList.add(blobData);
            }
            return blobDataList;
        } catch (Exception e) {
            System.out.print("Exception encountered: ");
            logger.debug(e.getMessage());
            System.exit(-3);
        }
        return null;
    }

    public List < String > deleteFromCloud(List < String > inputList) {
        List < String > deleted = new ArrayList < > ();
        return deleted;
    }
}