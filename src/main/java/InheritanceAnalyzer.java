import java.util.ArrayList;
import java.util.List;

public class InheritanceAnalyzer implements GraphAnalyzer {

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

        analyzeNode(
                root,
                entityRegistry,
                locationResolver,
                relationships
        );

        return relationships;
    }

    private void analyzeNode(
            AstNode node,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver,
            List<Relationship> relationships) {

        if (node == null) {
            return;
        }

        if ("CXXRecordDecl".equals(node.kind)
                && node.bases != null
                && !node.bases.isEmpty()) {

            CodeEntity derivedClass =
                    entityRegistry.findByAstId(
                            node.id
                    );

            if (derivedClass != null) {

                for (BaseSpecifier base :
                        node.bases) {

                    if (base == null) {
                        continue;
                    }

                    String baseName =
                            base.getQualifiedType();

                    if (baseName == null
                            || baseName.isBlank()) {

                        continue;
                    }

                    CodeEntity baseClass =
                            entityRegistry.findById(
                                    "CLASS:" + baseName
                            );

                    if (baseClass == null) {
                        continue;
                    }

                    Relationship relationship =
                            new Relationship(
                                    derivedClass.id,
                                    derivedClass.qualifiedName,
                                    baseClass.id,
                                    baseClass.qualifiedName,
                                    "INHERITS"
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

        if (node.inner == null) {
            return;
        }

        for (AstNode child :
                node.inner) {

            analyzeNode(
                    child,
                    entityRegistry,
                    locationResolver,
                    relationships
            );
        }
    }
}