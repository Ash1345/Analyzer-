import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverrideAnalyzer implements GraphAnalyzer {

    @Override
    public List<Relationship> analyze(
            AstNode root,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver) {

        List<Relationship> relationships =
                new ArrayList<>();

        if (root == null
                || entityRegistry == null) {

            return relationships;
        }

        /*
         * Collect all class AST nodes first.
         *
         * We need these because the method node contains
         * parentDeclContextId, but the base-class information
         * is stored on the corresponding CXXRecordDecl.
         */
        Map<String, AstNode> classNodesByAstId =
                new HashMap<>();

        collectClassNodes(
                root,
                classNodesByAstId
        );

        analyzeNode(
                root,
                classNodesByAstId,
                entityRegistry,
                locationResolver,
                relationships
        );

        return relationships;
    }

    private void collectClassNodes(
            AstNode node,
            Map<String, AstNode> classNodesByAstId) {

        if (node == null) {
            return;
        }

        if ("CXXRecordDecl".equals(node.kind)
                && node.id != null
                && node.name != null) {

            classNodesByAstId.put(
                    node.id,
                    node
            );
        }

        if (node.inner == null) {
            return;
        }

        for (AstNode child :
                node.inner) {

            collectClassNodes(
                    child,
                    classNodesByAstId
            );
        }
    }

    private void analyzeNode(
            AstNode node,
            Map<String, AstNode> classNodesByAstId,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver,
            List<Relationship> relationships) {

        if (node == null) {
            return;
        }

        if ("CXXMethodDecl".equals(node.kind)
                && ("start".equals(node.name))) {

            System.out.println(
                    "[OverrideDebug] Method: " + node.name
                            + ", AST ID: " + node.id
                            + ", parent ID: " + node.parentDeclContextId
                            + ", has OverrideAttr: "
                            + hasOverrideAttribute(node)
            );

            if (hasOverrideAttribute(node)) {
                analyzeOverride(
                        node,
                        classNodesByAstId,
                        entityRegistry,
                        locationResolver,
                        relationships
                );
            }
        }

        if (node.inner == null) {
            return;
        }

        for (AstNode child :
                node.inner) {

            analyzeNode(
                    child,
                    classNodesByAstId,
                    entityRegistry,
                    locationResolver,
                    relationships
            );
        }
    }



    private void analyzeOverride(
            AstNode methodNode,
            Map<String, AstNode> classNodesByAstId,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver,
            List<Relationship> relationships) {

        CodeEntity overridingMethod =
                entityRegistry.findByAstId(
                        methodNode.id
                );

        if (overridingMethod == null
                || !"METHOD".equals(
                overridingMethod.kind)) {

            return;
        }

        AstNode derivedClassNode = null;

        // First, try to resolve the derived class using the AST parent ID.
        if (methodNode.parentDeclContextId != null) {
            derivedClassNode =
                    classNodesByAstId.get(
                            methodNode.parentDeclContextId
                    );
        }

        // Fallback: resolve the owning class from the method's qualified name.
        if (derivedClassNode == null) {
            String ownerClassName =
                    getOwnerClassName(
                            overridingMethod.qualifiedName
                    );

            if (ownerClassName != null) {
                for (AstNode classNode :
                        classNodesByAstId.values()) {

                    if (ownerClassName.equals(classNode.name)
                            && classNode.bases != null
                            && !classNode.bases.isEmpty()) {

                        derivedClassNode = classNode;
                        break;
                    }
                }
            }
        }

        // DEBUG 1: Check whether the derived class was found.
        System.out.println(
                "[OverrideDebug] Method AST ID: " + methodNode.id
                        + ", parent ID: " + methodNode.parentDeclContextId
                        + ", derived class: "
                        + (derivedClassNode == null
                        ? "null"
                        : derivedClassNode.name)
        );

        if (derivedClassNode == null) {
            return;
        }

        CodeEntity derivedClass =
                entityRegistry.findByAstId(
                        derivedClassNode.id
                );

        if (derivedClass == null
                || !"CLASS".equals(
                derivedClass.kind)) {

            return;
        }

        if (derivedClassNode.bases == null
                || derivedClassNode.bases.isEmpty()) {

            return;
        }

        for (BaseSpecifier base :
                derivedClassNode.bases) {

            if (base == null) {
                continue;
            }

            String baseClassName =
                    base.getQualifiedType();

            if (baseClassName == null
                    || baseClassName.isBlank()) {

                continue;
            }

            CodeEntity baseClass =
                    findClassByName(
                            baseClassName,
                            entityRegistry
                    );

            if (baseClass == null) {
                continue;
            }

            CodeEntity overriddenMethod =
                    findMatchingBaseMethod(
                            baseClass,
                            overridingMethod,
                            entityRegistry
                    );

            // DEBUG 2: Check whether the base method was found.
            System.out.println(
                    "[OverrideDebug] Looking for base method: "
                            + baseClassName + "::" + overridingMethod.name
                            + ", result: "
                            + (overriddenMethod == null
                            ? "null"
                            : overriddenMethod.qualifiedName)
            );

            if (overriddenMethod == null) {
                continue;
            }

            Relationship relationship =
                    new Relationship(
                            overridingMethod.id,
                            overridingMethod.qualifiedName,
                            overriddenMethod.id,
                            overriddenMethod.qualifiedName,
                            "OVERRIDES"
                    );

            RelationshipLocation.attach(
                    relationship,
                    methodNode,
                    locationResolver
            );

            relationships.add(
                    relationship
            );
        }
    }



    private boolean hasOverrideAttribute(
            AstNode methodNode) {

        if (methodNode.inner == null) {
            return false;
        }

        for (AstNode child :
                methodNode.inner) {

            if ("OverrideAttr".equals(
                    child.kind)) {

                return true;
            }
        }

        return false;
    }

    private CodeEntity findClassByName(
            String className,
            EntityRegistry entityRegistry) {

        if (className == null) {
            return null;
        }

        for (CodeEntity entity :
                entityRegistry.getAll()) {

            if (entity == null) {
                continue;
            }

            if (!"CLASS".equals(
                    entity.kind)) {

                continue;
            }

            if (className.equals(
                    entity.qualifiedName)) {

                return entity;
            }
        }

        return null;
    }

    private CodeEntity findMatchingBaseMethod(
            CodeEntity baseClass,
            CodeEntity overridingMethod,
            EntityRegistry entityRegistry) {

        if (baseClass == null
                || overridingMethod == null) {

            return null;
        }

        String methodName =
                overridingMethod.name;

        if (methodName == null) {
            return null;
        }

        for (CodeEntity entity :
                entityRegistry.getAll()) {

            if (entity == null) {
                continue;
            }

            if (!"METHOD".equals(
                    entity.kind)) {

                continue;
            }

            if (!methodName.equals(
                    entity.name)) {

                continue;
            }

            String ownerClass =
                    getOwnerClassName(
                            entity.qualifiedName
                    );

            if (!baseClass.qualifiedName.equals(
                    ownerClass)) {

                continue;
            }

            /*
             * Prefer an exact parameter/signature match.
             */
            if (sameMethodSignature(
                    overridingMethod,
                    entity)) {

                return entity;
            }
        }

        /*
         * Fallback:
         *
         * If the signature information is not available,
         * use the method name within the base class.
         */
        for (CodeEntity entity :
                entityRegistry.getAll()) {

            if (entity == null) {
                continue;
            }

            if (!"METHOD".equals(
                    entity.kind)) {

                continue;
            }

            if (!methodName.equals(
                    entity.name)) {

                continue;
            }

            String ownerClass =
                    getOwnerClassName(
                            entity.qualifiedName
                    );

            if (baseClass.qualifiedName.equals(
                    ownerClass)) {

                return entity;
            }
        }

        return null;
    }

    private boolean sameMethodSignature(
            CodeEntity first,
            CodeEntity second) {

        if (first == null
                || second == null) {

            return false;
        }

        if (first.signature == null
                || second.signature == null) {

            return false;
        }

        String firstParameters =
                extractParameters(
                        first.signature
                );

        String secondParameters =
                extractParameters(
                        second.signature
                );

        return firstParameters.equals(
                secondParameters
        );
    }

    private String extractParameters(
            String signature) {

        if (signature == null) {
            return "";
        }

        int open =
                signature.indexOf('(');

        if (open < 0) {
            return "";
        }

        return signature.substring(
                open
        );
    }

    private String getOwnerClassName(
            String qualifiedName) {

        if (qualifiedName == null) {
            return null;
        }

        int separator =
                qualifiedName.lastIndexOf("::");

        if (separator < 0) {
            return null;
        }

        return qualifiedName.substring(
                0,
                separator
        );
    }
}