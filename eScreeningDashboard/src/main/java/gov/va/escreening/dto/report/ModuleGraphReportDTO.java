package gov.va.escreening.dto.report;

import java.io.Serializable;
import java.util.List;

/**
 * Created by kliu on 3/1/15.
 */
public class ModuleGraphReportDTO implements Serializable{

    private String veteranLastNameAndLastFourSSN;
    private String moduleName;
    private String scoreName;
    private String score;
    private String scoreMeaning;

    private String imageFileName;

    private String scoreHistoryTitle;

    private List<ScoreHistoryDTO> scoreHistory;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getScoreName() {
        return scoreName;
    }

    public void setScoreName(String scoreName) {
        this.scoreName = scoreName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getScoreMeaning() {
        return scoreMeaning;
    }

    public void setScoreMeaning(String scoreMeaning) {
        this.scoreMeaning = scoreMeaning;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getScoreHistoryTitle() {
        return scoreHistoryTitle;
    }

    public void setScoreHistoryTitle(String scoreHistoryTitle) {
        this.scoreHistoryTitle = scoreHistoryTitle;
    }

    public List<ScoreHistoryDTO> getScoreHistory() {
        return scoreHistory;
    }

    public void setScoreHistory(List<ScoreHistoryDTO> scoreHistory) {
        this.scoreHistory = scoreHistory;
    }

    public String getVeteranLastNameAndLastFourSSN() {
        return veteranLastNameAndLastFourSSN;
    }

    public void setVeteranLastNameAndLastFourSSN(String veteranLastNameAndLastFourSSN) {
        this.veteranLastNameAndLastFourSSN = veteranLastNameAndLastFourSSN;
    }
}
