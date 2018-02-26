@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private BlobHandler blobHandler;

    @Override
    public void upload(MultipartFile file) {
        blobHandler.uploadFile(file);
    }

    @Override
    public void justUpload() {
        blobHandler.justUpload();
    }

    @Override
    public String uploadFile(File file) {
        return blobHandler.uploadToCloud(file, null);
    }
    @Override
    public String upload() {
        return blobHandler.uploadFile();
    }

    public String uploadMultipartFile(MultipartFile sourceFile, Map < String, String > metadata) {
        File file = blobHandler.uploadMultipartFile(sourceFile);
        return blobHandler.uploadToCloud(file, metadata);
    }

    public String uploadMultipartFile(MultipartFile sourceFile) {
        File file = blobHandler.uploadMultipartFile(sourceFile);
        return blobHandler.uploadToCloud(file, null);
    }
}