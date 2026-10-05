import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;

public class ProjectLoader {

    private final ObjectMapper mapper;

    public ProjectLoader() {

        this.mapper =
                new ObjectMapper();
    }

    public Project load(
            List<String> astPaths,
            List<String> sourcePaths)
            throws Exception {

        // =====================================================
        // Validate input
        // =====================================================

        if (astPaths == null
                || sourcePaths == null) {

            throw new IllegalArgumentException(
                    "AST paths and source paths cannot be null"
            );
        }

        if (astPaths.size() != sourcePaths.size()) {

            throw new IllegalArgumentException(
                    "Number of AST files must match "
                            + "number of source files"
            );
        }

        // =====================================================
        // Create project
        // =====================================================

        Project project =
                new Project();

        // =====================================================
        // Load every translation unit
        // =====================================================

        for (int i = 0;
             i < astPaths.size();
             i++) {

            String astPath =
                    astPaths.get(i);

            String sourcePath =
                    sourcePaths.get(i);

            // -------------------------------------------------
            // Read AST
            // -------------------------------------------------

            AstNode ast =
                    mapper.readValue(
                            new File(astPath),
                            AstNode.class
                    );

            // -------------------------------------------------
            // Create translation unit
            // -------------------------------------------------

            TranslationUnit translationUnit =
                    new TranslationUnit(
                            new SourceFile(
                                    sourcePath
                            ),
                            ast
                    );

            // -------------------------------------------------
            // Add translation unit to project
            // -------------------------------------------------

            project.addTranslationUnit(
                    translationUnit
            );
        }

        return project;
    }
}