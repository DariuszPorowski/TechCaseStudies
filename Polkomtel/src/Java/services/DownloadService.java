@Service
public interface DownloadService {

    List < BlobData > listBlobs(String container);
}