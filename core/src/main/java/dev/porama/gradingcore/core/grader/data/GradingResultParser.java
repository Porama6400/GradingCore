package dev.porama.gradingcore.core.grader.data;

import dev.porama.gradingcore.core.utils.SerializerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GradingResultParser {

    private static final Logger logger = LoggerFactory.getLogger(GradingResultParser.class);

    public static GradingResult parse(GradingRequest request, Map<String, byte[]> fileMap) {
//        String result = ParserUtils.parseFileMap(fileMap, "result.txt");
        String compilationLog = SerializerUtils.parseFileMap(fileMap, "compilationLog.txt");
        String statusText = SerializerUtils.parseFileMap(fileMap, "status.txt");

        GradingStatus status;
        try {
            statusText = statusText.split("[\\r\\n]")[0];
            status = GradingStatus.valueOf(statusText);
        } catch (IllegalArgumentException ignored) {
            status = GradingStatus.FAILED_CONTAINER;
            logger.error("Failed to parse grading status: {}", statusText);
        }

        GradingResult gradingResult = new GradingResult(request, status, fileMap);
        gradingResult.getMetadata().put("compilationLog", compilationLog);
        return gradingResult;
    }
}
