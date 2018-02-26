public class BlobData {
    private String blobName;
    private String uri;
    private String uriThumb;
    private HashMap < String, String > metadata;

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUriThumb() {
        return uriThumb;
    }

    public void setUriThumb(String uriThumb) {
        this.uriThumb = uriThumb;
    }

    public Map < String, String > getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap < String, String > metadata) {
        this.metadata = metadata;
    }
}