public class SourceFile {

    private final String path;

    public SourceFile(String path) {

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(
                    "Source file path cannot be null or blank"
            );
        }

        this.path = path;
    }

    public String getPath() {
        return path;
    }
}