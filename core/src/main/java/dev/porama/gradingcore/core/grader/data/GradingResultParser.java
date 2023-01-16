package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.core.utils.ParserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GradingResultParser {

    private static final Logger logger = LoggerFactory.getLogger(GradingResultParser.class);

    public static GradingResult parse(int submissionId, Map<String, byte[]> fileMap) {
//        String result = ParserUtils.parseFileMap(fileMap, "result.txt");
        String compilationLog = ParserUtils.parseFileMap(fileMap, "compilationLog.txt");
        String statusText = ParserUtils.parseFileMap(fileMap, "status.txt");

        GradingStatus status;
        try {
            statusText = statusText.split("[\\r\\n]")[0];
            status = GradingStatus.valueOf(statusText);
        } catch (IllegalArgumentException ignored) {
            status = GradingStatus.FAILED_CONTAINER;
            logger.error("Failed to parse grading status: {}", statusText);
        }

        return new GradingResult(submissionId, status, compilationLog, fileMap);
    }
}
