package dev.porama.gradingcore.core.postprocessor;

import dev.porama.gradingcore.core.grader.data.GradingResult;

public interface PostProcessor {
    String getIdentifier();

    boolean apply(GradingResult result);
}
