package dev.porama.gradingcore.core.postprocessor;

import dev.porama.gradingcore.core.grader.data.GradingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PostProcessorService {
    private final List<PostProcessor> postProcessorList = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(PostProcessorService.class);

    public void apply(GradingResult result) {
        for (PostProcessor postProcessor : postProcessorList) {
            try {
                boolean applied = postProcessor.apply(result);
                logger.debug("Post processor " + postProcessor.getIdentifier() + (applied ? " applied" : " did not applied"));
            } catch (Exception ex) {
                logger.error("Error applying post processor " + postProcessor.getIdentifier() + " to " + result.getId(), ex);
            }
        }
    }

    public void add(PostProcessor postProcessor) {
        postProcessorList.add(postProcessor);
    }
}
