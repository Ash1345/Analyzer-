import java.util.ArrayList;
import java.util.List;

public class RelationshipRegistry {

    private final List<Relationship> relationships =
            new ArrayList<>();


    // =========================================================
    // Add relationship using individual fields
    // =========================================================

    public void add(
            String sourceId,
            String source,
            String targetId,
            String target,
            String type) {

        Relationship relationship =
                new Relationship(
                        sourceId,
                        source,
                        targetId,
                        target,
                        type
                );

        add(relationship);
    }


    // =========================================================
    // Add relationship
    // =========================================================

    public void add(
            Relationship relationship) {

        if (relationship == null) {
            return;
        }

        for (Relationship existing :
                relationships) {

            if (existing.sourceId.equals(
                    relationship.sourceId)
                    && existing.targetId.equals(
                    relationship.targetId)
                    && existing.type.equals(
                    relationship.type)) {

                mergeEvidence(
                        existing,
                        relationship
                );

                return;
            }
        }

        relationships.add(
                relationship
        );
    }


    // =========================================================
    // Merge evidence from duplicate relationship
    // =========================================================

    private void mergeEvidence(
            Relationship existing,
            Relationship incoming) {

        if (incoming.evidence == null
                || incoming.evidence.isEmpty()) {

            return;
        }

        if (existing.evidence == null) {

            existing.evidence =
                    new ArrayList<>();
        }

        for (RelationshipEvidence incomingEvidence :
                incoming.evidence) {

            if (incomingEvidence == null) {
                continue;
            }

            boolean alreadyExists = false;

            for (RelationshipEvidence existingEvidence :
                    existing.evidence) {

                if (sameEvidence(
                        existingEvidence,
                        incomingEvidence)) {

                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {

                existing.evidence.add(
                        incomingEvidence
                );
            }
        }
    }


    // =========================================================
    // Compare two evidence occurrences
    // =========================================================

    private boolean sameEvidence(
            RelationshipEvidence first,
            RelationshipEvidence second) {

        if (first == null || second == null) {
            return first == second;
        }

        return java.util.Objects.equals(
                first.file,
                second.file
        )
                && java.util.Objects.equals(
                first.line,
                second.line
        )
                && java.util.Objects.equals(
                first.column,
                second.column
        );
    }


    // =========================================================
    // Get all relationships
    // =========================================================

    public List<Relationship> getRelationships() {

        return new ArrayList<>(
                relationships
        );
    }


    // =========================================================
    // Number of relationships
    // =========================================================

    public int size() {

        return relationships.size();
    }
}