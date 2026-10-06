public class AnalysisStatisticsCalculator {

    public static AnalysisStatistics calculate(
            Project project,
            CodeGraph graph) {

        if (project == null) {
            throw new IllegalArgumentException(
                    "Project cannot be null"
            );
        }

        if (graph == null) {
            throw new IllegalArgumentException(
                    "Graph cannot be null"
            );
        }


        // =====================================================
        // Source files
        // =====================================================

        int sourceFileCount =
                project
                        .getTranslationUnits()
                        .size();


        // =====================================================
        // Translation units
        // =====================================================

        int translationUnitCount =
                project
                        .getTranslationUnits()
                        .size();


        // =====================================================
        // Entities
        // =====================================================

        int entityCount =
                graph.entities.size();


        // =====================================================
        // Relationships
        // =====================================================

        int relationshipCount =
                graph.relationships.size();


        // =====================================================
        // Evidence
        // =====================================================

        int evidenceCount = 0;

        for (Relationship relationship :
                graph.relationships) {

            if (relationship.evidence != null) {

                evidenceCount +=
                        relationship.evidence.size();
            }
        }


        // =====================================================
        // Create statistics
        // =====================================================

        return new AnalysisStatistics(
                sourceFileCount,
                translationUnitCount,
                entityCount,
                relationshipCount,
                evidenceCount
        );
    }
}