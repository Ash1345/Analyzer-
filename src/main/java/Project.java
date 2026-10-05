import java.util.ArrayList;
import java.util.List;

public class Project {

    private final List<TranslationUnit> translationUnits;

    public Project() {

        this.translationUnits =
                new ArrayList<>();
    }

    public void addTranslationUnit(
            TranslationUnit translationUnit) {

        if (translationUnit == null) {
            return;
        }

        translationUnits.add(
                translationUnit
        );
    }

    public List<TranslationUnit> getTranslationUnits() {

        return new ArrayList<>(
                translationUnits
        );
    }
}