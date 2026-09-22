public class RelationshipLocation {

    public static void attach(
            Relationship relationship,
            AstNode node,
            SourceLocationResolver locationResolver) {

        if (relationship == null
                || node == null
                || node.range == null
                || locationResolver == null) {

            return;
        }

        Object beginObject =
                node.range.get("begin");

        if (!(beginObject instanceof java.util.Map)) {
            return;
        }

        java.util.Map<?, ?> begin =
                (java.util.Map<?, ?>) beginObject;

        Object offsetObject =
                begin.get("offset");

        if (offsetObject == null) {
            return;
        }

        int offset =
                Integer.parseInt(
                        offsetObject.toString()
                );

        SourceLocationResolver.Location location =
                locationResolver.resolve(
                        offset
                );

        // =========================================================
        // Store evidence occurrence
        // =========================================================

        RelationshipEvidence evidence =
                new RelationshipEvidence(
                        location.file,
                        location.line,
                        location.column
                );

        relationship.evidence.add(
                evidence
        );

        // =========================================================
        // Keep legacy fields for now
        // =========================================================

        relationship.file =
                location.file;

        relationship.line =
                location.line;

        relationship.column =
                location.column;
    }
}