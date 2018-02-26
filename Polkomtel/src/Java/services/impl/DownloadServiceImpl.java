@Service
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    BlobHandler blobHandler;

    @Override
    public List < BlobData > listBlobs(String container) {
        return blobHandler.listBlobs(container);
    }
}