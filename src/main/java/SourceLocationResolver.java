import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SourceLocationResolver {

    private final String sourceFile;
    private final String sourceContent;
    private final int[] lineStartOffsets;

    public SourceLocationResolver(String sourceFile)
            throws IOException {

        this.sourceFile = sourceFile;

        this.sourceContent =
                Files.readString(
                        Path.of(sourceFile)
                );

        this.lineStartOffsets =
                buildLineStartOffsets(
                        sourceContent
                );
    }

    public Location resolve(int offset) {

        if (offset < 0
                || offset > sourceContent.length()) {

            return new Location(
                    sourceFile,
                    null,
                    null
            );
        }

        int lineIndex =
                findLine(offset);

        int lineStart =
                lineStartOffsets[lineIndex];

        int line =
                lineIndex + 1;

        int column =
                offset - lineStart + 1;

        return new Location(
                sourceFile,
                line,
                column
        );
    }

    private int findLine(int offset) {

        int low = 0;
        int high =
                lineStartOffsets.length - 1;

        while (low <= high) {

            int middle =
                    (low + high) / 2;

            if (lineStartOffsets[middle]
                    <= offset) {

                low =
                        middle + 1;

            } else {

                high =
                        middle - 1;
            }
        }

        return high;
    }

    private int[] buildLineStartOffsets(
            String content) {

        int lineCount = 1;

        for (int i = 0;
             i < content.length();
             i++) {

            if (content.charAt(i) == '\n') {
                lineCount++;
            }
        }

        int[] offsets =
                new int[lineCount];

        int index = 0;

        offsets[index++] = 0;

        for (int i = 0;
             i < content.length();
             i++) {

            if (content.charAt(i) == '\n'
                    && index < offsets.length) {

                offsets[index++] = i + 1;
            }
        }

        return offsets;
    }

    public static class Location {

        public String file;
        public Integer line;
        public Integer column;

        public Location(
                String file,
                Integer line,
                Integer column) {

            this.file = file;
            this.line = line;
            this.column = column;
        }
    }
}