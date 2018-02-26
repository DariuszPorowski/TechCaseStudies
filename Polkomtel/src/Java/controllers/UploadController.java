@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @RequestMapping(method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file) {
        return uploadService.uploadMultipartFile(file);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/f")
    public String upload(@RequestParam("file") File file) {

        return uploadService.uploadFile(file);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/l")
    public String uploadLocal() {

        return uploadService.upload();
    }
}