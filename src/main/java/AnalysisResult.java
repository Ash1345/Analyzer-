public class AnalysisResult {

    private final Project project;
    private final CodeGraph graph;
    private final AnalysisStatistics statistics;

    public AnalysisResult(
            Project project,
            CodeGraph graph,
            AnalysisStatistics statistics) {

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

        if (statistics == null) {
            throw new IllegalArgumentException(
                    "Statistics cannot be null"
            );
        }

        this.project = project;
        this.graph = graph;
        this.statistics = statistics;
    }

    public Project getProject() {
        return project;
    }

    public CodeGraph getGraph() {
        return graph;
    }

    public AnalysisStatistics getStatistics() {
        return statistics;
    }
}