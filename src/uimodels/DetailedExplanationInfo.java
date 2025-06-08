package uimodels;

import java.util.List;
import java.util.Map;

public class DetailedExplanationInfo {
    public List<Integer> getPostTimeDecreased() {
        return postTimeDecreased;
    }

    public Map<Integer, List<Integer>> getVisitInvocations() {
        return visitInvocations;
    }

    public void setPostTimeDecreased(List<Integer> postTimeDecreased) {
        this.postTimeDecreased = postTimeDecreased;
    }

    public void setVisitInvocations(Map<Integer, List<Integer>> visitInvocations) {
        this.visitInvocations = visitInvocations;
    }

    private List<Integer> postTimeDecreased;
    private Map<Integer, List<Integer>> visitInvocations;
}
