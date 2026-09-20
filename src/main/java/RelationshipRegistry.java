import java.util.ArrayList;
import java.util.List;

public class RelationshipRegistry {

    private final List<Relationship> relationships =
            new ArrayList<>();


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

        /*
         * Prevent duplicate semantic relationships.
         *
         * Two relationships are considered identical when
         * they have the same:
         *
         * source
         * target
         * type
         */
        for (Relationship existing :
                relationships) {

            if (existing.sourceId.equals(
                    relationship.sourceId)
                    && existing.targetId.equals(
                    relationship.targetId)
                    && existing.type.equals(
                    relationship.type)) {

                return;
            }
        }

        relationships.add(
                relationship
        );
    }


    public void add(
            Relationship relationship) {

        if (relationship == null) {
            return;
        }

        /*
         * Prevent duplicate semantic relationships.
         */
        for (Relationship existing :
                relationships) {

            if (existing.sourceId.equals(
                    relationship.sourceId)
                    && existing.targetId.equals(
                    relationship.targetId)
                    && existing.type.equals(
                    relationship.type)) {

                return;
            }
        }

        relationships.add(
                relationship
        );
    }


    public List<Relationship> getRelationships() {

        return new ArrayList<>(
                relationships
        );
    }


    public int size() {

        return relationships.size();
    }
}