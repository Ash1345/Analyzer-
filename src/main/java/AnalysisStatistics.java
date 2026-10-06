public class AnalysisStatistics {

    private final int sourceFileCount;
    private final int translationUnitCount;
    private final int entityCount;
    private final int relationshipCount;
    private final int evidenceCount;

    public AnalysisStatistics(
            int sourceFileCount,
            int translationUnitCount,
            int entityCount,
            int relationshipCount,
            int evidenceCount) {

        this.sourceFileCount =
                sourceFileCount;

        this.translationUnitCount =
                translationUnitCount;

        this.entityCount =
                entityCount;

        this.relationshipCount =
                relationshipCount;

        this.evidenceCount =
                evidenceCount;
    }

    public int getSourceFileCount() {
        return sourceFileCount;
    }

    public int getTranslationUnitCount() {
        return translationUnitCount;
    }

    public int getEntityCount() {
        return entityCount;
    }

    public int getRelationshipCount() {
        return relationshipCount;
    }

    public int getEvidenceCount() {
        return evidenceCount;
    }
}