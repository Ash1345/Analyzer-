
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualDispatchAnalyzer implements GraphAnalyzer {

    @Override
    public List<Relationship> analyze(
            AstNode root,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver) {

        List<Relationship> relationships =
                new ArrayList<>();

        if (root == null || entityRegistry == null) {
            return relationships;
        }

        Map<String, AstNode> variableDeclarations =
                new HashMap<>();

        collectVariableDeclarations(
                root,
                variableDeclarations
        );

        analyzeNode(
                root,
                root,
                entityRegistry,
                locationResolver,
                variableDeclarations,
                relationships,
                null
        );

        return relationships;
    }

    private void analyzeNode(
            AstNode node,
            AstNode root,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver,
            Map<String, AstNode> variableDeclarations,
            List<Relationship> relationships,
            CodeEntity currentFunction) {

        if (node == null) {
            return;
        }

        if (("FunctionDecl".equals(node.kind)
                || "CXXMethodDecl".equals(node.kind))
                && node.name != null) {

            CodeEntity function =
                    entityRegistry.findByAstId(node.id);

            if (function != null) {
                currentFunction = function;
            }
        }

        if ("CXXMemberCallExpr".equals(node.kind)
                && currentFunction != null) {

            AstNode memberExpr =
                    findNodeByKind(node, "MemberExpr");

            if (memberExpr != null
                    && memberExpr.referencedMemberDecl != null) {

                CodeEntity staticTarget =
                        entityRegistry.findByAstId(
                                memberExpr.referencedMemberDecl
                        );

                if (staticTarget != null
                        && "METHOD".equals(staticTarget.kind)) {

                    String receiverId =
                            findReferencedVariableId(memberExpr);

                    AstNode receiverDeclaration =
                            receiverId == null
                                    ? null
                                    : variableDeclarations.get(receiverId);

                    if (receiverDeclaration != null) {

                        String initializedVariableId =
                                findInitializerVariableId(
                                        receiverDeclaration
                                );

                        if (initializedVariableId != null) {

                            CodeEntity initializedVariable =
                                    entityRegistry.findByAstId(
                                            initializedVariableId
                                    );

                            if (initializedVariable != null
                                    && initializedVariable.type != null) {

                                String derivedClassName =
                                        normalizeTypeName(
                                                initializedVariable.type
                                        );

                                CodeEntity derivedClass =
                                        entityRegistry.findById(
                                                "CLASS:" + derivedClassName
                                        );

                                String baseClassName =
                                        getOwnerClassName(
                                                staticTarget.qualifiedName
                                        );

                                if (derivedClass != null
                                        && baseClassName != null
                                        && !derivedClassName.equals(
                                        baseClassName)
                                        && isDerivedFrom(
                                        root,
                                        derivedClassName,
                                        baseClassName)) {

                                    CodeEntity runtimeTarget =
                                            findMethod(
                                                    entityRegistry,
                                                    derivedClassName,
                                                    staticTarget.name
                                            );

                                    if (runtimeTarget != null
                                            && !runtimeTarget.id.equals(
                                            staticTarget.id)) {

                                        Relationship relationship =
                                                new Relationship(
                                                        currentFunction.id,
                                                        currentFunction.qualifiedName,
                                                        runtimeTarget.id,
                                                        runtimeTarget.qualifiedName,
                                                        "VIRTUAL_DISPATCHES_TO"
                                                );

                                        RelationshipLocation.attach(
                                                relationship,
                                                node,
                                                locationResolver
                                        );

                                        relationships.add(
                                                relationship
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (node.inner != null) {
            for (AstNode child : node.inner) {
                analyzeNode(
                        child,
                        root,
                        entityRegistry,
                        locationResolver,
                        variableDeclarations,
                        relationships,
                        currentFunction
                );
            }
        }
    }

    private void collectVariableDeclarations(
            AstNode node,
            Map<String, AstNode> declarations) {

        if (node == null) {
            return;
        }

        if ("VarDecl".equals(node.kind)
                && node.id != null) {

            declarations.put(node.id, node);
        }

        if (node.inner != null) {
            for (AstNode child : node.inner) {
                collectVariableDeclarations(
                        child,
                        declarations
                );
            }
        }
    }

    private String findReferencedVariableId(
            AstNode node) {

        if (node == null) {
            return null;
        }

        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null) {

            Object kind =
                    node.referencedDecl.get("kind");

            Object id =
                    node.referencedDecl.get("id");

            if ("VarDecl".equals(
                    kind == null ? null : kind.toString())
                    && id != null) {

                return id.toString();
            }
        }

        if (node.inner != null) {
            for (AstNode child : node.inner) {
                String result =
                        findReferencedVariableId(child);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private String findInitializerVariableId(
            AstNode variableDeclaration) {

        if (variableDeclaration == null
                || variableDeclaration.inner == null) {
            return null;
        }

        for (AstNode child : variableDeclaration.inner) {

            String result =
                    findReferencedVariableId(child);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private AstNode findNodeByKind(
            AstNode node,
            String kind) {

        if (node == null) {
            return null;
        }

        if (kind.equals(node.kind)) {
            return node;
        }

        if (node.inner != null) {
            for (AstNode child : node.inner) {

                AstNode result =
                        findNodeByKind(child, kind);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private String normalizeTypeName(String type) {

        if (type == null) {
            return null;
        }

        String result = type.trim();

        result = result.replace("*", "").trim();
        result = result.replace("&", "").trim();

        if (result.startsWith("const ")) {
            result = result.substring(6).trim();
        }

        return result;
    }

    private String getOwnerClassName(
            String qualifiedMethodName) {

        if (qualifiedMethodName == null) {
            return null;
        }

        int separator =
                qualifiedMethodName.lastIndexOf("::");

        if (separator <= 0) {
            return null;
        }

        return qualifiedMethodName.substring(
                0,
                separator
        );
    }

    private CodeEntity findMethod(
            EntityRegistry entityRegistry,
            String className,
            String methodName) {

        for (CodeEntity entity : entityRegistry.getAll()) {

            if ("METHOD".equals(entity.kind)
                    && methodName.equals(entity.name)
                    && (className + "::" + methodName)
                    .equals(entity.qualifiedName)) {

                return entity;
            }
        }

        return null;
    }

    private boolean isDerivedFrom(
            AstNode root,
            String derivedClassName,
            String baseClassName) {

        if (root == null) {
            return false;
        }

        if ("CXXRecordDecl".equals(root.kind)
                && derivedClassName.equals(root.name)
                && root.bases != null) {

            for (BaseSpecifier base : root.bases) {

                if (base != null
                        && baseClassName.equals(
                        base.getQualifiedType())) {

                    return true;
                }
            }
        }

        if (root.inner != null) {
            for (AstNode child : root.inner) {

                if (isDerivedFrom(
                        child,
                        derivedClassName,
                        baseClassName)) {

                    return true;
                }
            }
        }

        return false;
    }
}
