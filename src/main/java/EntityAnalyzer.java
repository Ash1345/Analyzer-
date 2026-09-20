import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntityAnalyzer {

    public static List<CodeEntity> analyze(
            AstNode node,
            String sourceFile) {

        List<CodeEntity> entities =
                new ArrayList<>();

        SourceLocationResolver locationResolver;

        try {
            locationResolver =
                    new SourceLocationResolver(sourceFile);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to read source file: "
                            + sourceFile,
                    e
            );
        }

        analyze(
                node,
                entities,
                null,
                null,
                null,
                sourceFile,
                locationResolver
        );

        return entities;
    }

    private static void analyze(
            AstNode node,
            List<CodeEntity> entities,
            String currentClass,
            String currentClassId,
            CodeEntity currentFunction,
            String sourceFile,
            SourceLocationResolver locationResolver) {

        if (node == null) {
            return;
        }

        // Ignore implicit declarations
        if (Boolean.TRUE.equals(node.isImplicit)) {
            return;
        }


        // =========================================================
        // Class
        // =========================================================

        if ("CXXRecordDecl".equals(node.kind)
                && node.name != null) {

            currentClass = node.name;

            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "CLASS",
                            node.name,
                            node.name
                    );

            entity.logicalId =
                    "CLASS:" + node.name;

            /*
             * currentClassId now stores the Analyzer++
             * identity instead of the raw Clang AST ID.
             */
            currentClassId = entity.id;

            extractLocation(
                    node,
                    entity,
                    sourceFile,
                    locationResolver
            );

            entities.add(entity);
        }


        // =========================================================
        // Method
        // =========================================================

        if ("CXXMethodDecl".equals(node.kind)
                && node.name != null) {

            String methodClass = currentClass;
            String methodClassId = currentClassId;

            /*
             * For an out-of-class definition such as:
             *
             * int Calculator::add(int a, int b)
             *
             * Clang gives us previousDecl.
             *
             * We use the previous declaration to find the
             * already-known Calculator class.
             */
            if (methodClass == null
                    && node.previousDecl != null) {

                for (CodeEntity existingEntity :
                        entities) {

                    /*
                     * previousDecl is still a raw Clang AST ID,
                     * so compare it with astId rather than id.
                     */
                    if (existingEntity.astId != null
                            && existingEntity.astId.equals(
                            node.previousDecl)) {

                        if ("METHOD".equals(
                                existingEntity.kind)) {

                            methodClass =
                                    extractClassName(
                                            existingEntity.qualifiedName
                                    );

                            /*
                             * parentId is now already an
                             * Analyzer++ ID.
                             */
                            methodClassId =
                                    existingEntity.parentId;
                        }

                        break;
                    }
                }
            }


            String qualifiedName;

            if (methodClass != null) {

                qualifiedName =
                        methodClass
                                + "::"
                                + node.name;

            } else {

                qualifiedName =
                        node.name;
            }


            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "METHOD",
                            node.name,
                            qualifiedName
                    );

            entity.logicalId =
                    node.mangledName;

            /*
             * parentId now contains the Analyzer++
             * identity of the class.
             *
             * Example:
             *
             * CLASS:Calculator
             */
            entity.parentId =
                    methodClassId;

            entity.returnType =
                    extractReturnType(node);

            extractLocation(
                    node,
                    entity,
                    sourceFile,
                    locationResolver
            );

            entities.add(entity);

            currentFunction = entity;
        }


        // =========================================================
        // Free function
        // =========================================================

        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "FUNCTION",
                            node.name,
                            node.name
                    );

            entity.returnType =
                    extractReturnType(node);

            extractLocation(
                    node,
                    entity,
                    sourceFile,
                    locationResolver
            );

            entities.add(entity);

            currentFunction = entity;
        }


        // =========================================================
        // Constructor
        // =========================================================

        if ("CXXConstructorDecl".equals(node.kind)
                && node.name != null
                && currentClass != null) {

            String qualifiedName =
                    currentClass
                            + "::"
                            + node.name;

            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "CONSTRUCTOR",
                            node.name,
                            qualifiedName
                    );

            entity.parentId =
                    currentClassId;

            extractLocation(
                    node,
                    entity,
                    sourceFile,
                    locationResolver
            );

            entities.add(entity);

            currentFunction = entity;
        }


        // =========================================================
        // Variable
        // =========================================================

        if ("VarDecl".equals(node.kind)
                && node.name != null
                && currentFunction != null) {

            String variableType = null;

            if (node.type != null) {

                Object qualType =
                        node.type.get("qualType");

                if (qualType != null) {

                    variableType =
                            qualType.toString();
                }
            }


            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "VARIABLE",
                            node.name,
                            currentFunction.qualifiedName
                                    + "::"
                                    + node.name
                    );

            /*
             * currentFunction.id is already an
             * Analyzer++ ID.
             */
            entity.parentId =
                    currentFunction.id;

            entity.type =
                    variableType;

            extractLocation(
                    node,
                    entity,
                    sourceFile,
                    locationResolver
            );

            entities.add(entity);
        }


        // =========================================================
        // Parameter
        // =========================================================

        if ("ParmVarDecl".equals(node.kind)
                && node.name != null
                && currentFunction != null) {

            String parameterType = null;

            if (node.type != null) {

                Object qualType =
                        node.type.get("qualType");

                if (qualType != null) {

                    parameterType =
                            qualType.toString();
                }
            }


            Parameter parameter =
                    new Parameter(
                            node.id,
                            node.name,
                            parameterType
                    );

            currentFunction.parameters.add(
                    parameter
            );
        }


        // =========================================================
        // Analyze children
        // =========================================================

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                analyze(
                        child,
                        entities,
                        currentClass,
                        currentClassId,
                        currentFunction,
                        sourceFile,
                        locationResolver
                );
            }
        }
    }

    private static String extractClassName(
            String qualifiedName) {

        if (qualifiedName == null) {
            return null;
        }

        int separator =
                qualifiedName.lastIndexOf("::");

        if (separator <= 0) {
            return null;
        }

        return qualifiedName.substring(
                0,
                separator
        );
    }

    private static String extractReturnType(
            AstNode node) {

        if (node.type == null) {
            return null;
        }

        Object qualType =
                node.type.get("qualType");

        if (qualType == null) {
            return null;
        }

        String functionType =
                qualType.toString();

        int spaceIndex =
                functionType.indexOf(" ");

        if (spaceIndex > 0) {

            return functionType.substring(
                    0,
                    spaceIndex
            );
        }

        return functionType;
    }

    private static void extractLocation(
            AstNode node,
            CodeEntity entity,
            String sourceFile,
            SourceLocationResolver locationResolver) {

        if (node.loc == null) {
            return;
        }

        Object offsetObject =
                node.loc.get("offset");

        // Prefer Clang's byte/character offset.
        if (offsetObject != null) {

            int offset =
                    Integer.parseInt(
                            offsetObject.toString()
                    );

            SourceLocationResolver.Location location =
                    locationResolver.resolve(offset);

            entity.file =
                    location.file;

            entity.line =
                    location.line;

            entity.column =
                    location.column;

            return;
        }

        // Fallback when offset is unavailable.
        Object file =
                node.loc.get("file");

        Object line =
                node.loc.get("line");

        Object column =
                node.loc.get("col");

        if (file != null) {

            entity.file =
                    file.toString();

        } else {

            entity.file =
                    sourceFile;
        }

        if (line != null) {

            entity.line =
                    Integer.valueOf(
                            line.toString()
                    );
        }

        if (column != null) {

            entity.column =
                    Integer.valueOf(
                            column.toString()
                    );
        }
    }
}