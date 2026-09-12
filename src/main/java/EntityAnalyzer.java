import java.util.ArrayList;
import java.util.List;

public class EntityAnalyzer {

    public static List<CodeEntity> analyze(AstNode node) {

        List<CodeEntity> entities = new ArrayList<>();

        analyze(node, entities, null, null, null);

        return entities;
    }

    private static void analyze(
            AstNode node,
            List<CodeEntity> entities,
            String currentClass,
            String currentClassId,
            CodeEntity currentFunction) {

        if (node == null) {
            return;
        }

        // Ignore implicit declarations
        if (Boolean.TRUE.equals(node.isImplicit)) {
            return;
        }

        // Class
        if ("CXXRecordDecl".equals(node.kind)
                && node.name != null) {

            currentClass = node.name;
            currentClassId = node.id;

            entities.add(
                    new CodeEntity(
                            node.id,
                            "CLASS",
                            node.name,
                            node.name
                    )
            );
        }

        // Method
        if ("CXXMethodDecl".equals(node.kind)
                && node.name != null) {

            String qualifiedName =
                    currentClass + "::" + node.name;

            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "METHOD",
                            node.name,
                            qualifiedName
                    );

            entity.parentId = currentClassId;
            entity.returnType = extractReturnType(node);

            entities.add(entity);

            currentFunction = entity;
        }

        // Free function
        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "FUNCTION",
                            node.name,
                            node.name
                    );

            entity.returnType = extractReturnType(node);

            entities.add(entity);

            currentFunction = entity;
        }

        // Constructor
        if ("CXXConstructorDecl".equals(node.kind)
                && node.name != null
                && currentClass != null) {

            String qualifiedName =
                    currentClass + "::" + node.name;

            CodeEntity entity =
                    new CodeEntity(
                            node.id,
                            "CONSTRUCTOR",
                            node.name,
                            qualifiedName
                    );

            entity.parentId = currentClassId;

            entities.add(entity);

            currentFunction = entity;
        }

        // Parameter
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

        // Analyze children
        if (node.inner != null) {

            for (AstNode child : node.inner) {

                analyze(
                        child,
                        entities,
                        currentClass,
                        currentClassId,
                        currentFunction
                );
            }
        }
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
            CodeEntity entity) {

        if (node.loc == null) {
            return;
        }

        Object file =
                node.loc.get("file");

        Object line =
                node.loc.get("line");

        Object column =
                node.loc.get("col");

        if (file != null) {
            entity.file = file.toString();
        }

        if (line != null) {
            entity.line =
                    Integer.valueOf(line.toString());
        }

        if (column != null) {
            entity.column =
                    Integer.valueOf(column.toString());
        }
    }
}