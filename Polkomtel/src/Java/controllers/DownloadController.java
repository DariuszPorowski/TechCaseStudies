@RestController
@RequestMapping("/download")
public class DownloadController {

    @Autowired
    DownloadService downloadService;

    @RequestMapping(method = RequestMethod.GET, value = "/{cont}")
    public List < BlobData > listBlobs(@PathVariable("cont") String containerName) {
        return downloadService.listBlobs(containerName);
    }
}