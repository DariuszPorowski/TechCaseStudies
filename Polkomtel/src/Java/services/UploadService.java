@Service
public interface UploadService {
    void upload(MultipartFile file);
    void justUpload();
    String upload();
    String uploadMultipartFile(MultipartFile file, Map < String, String > metadata);
    String uploadMultipartFile(MultipartFile file);
    String uploadFile(File file);
}