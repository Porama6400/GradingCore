package dev.porama.gradingcore.core.postprocessor.impl;

import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.grader.data.GradingStatus;
import dev.porama.gradingcore.core.postprocessor.PostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeLimitPostProcessor implements PostProcessor {

    private final Logger logger = LoggerFactory.getLogger(TimeLimitPostProcessor.class);
    private final Pattern userTimePattern = Pattern.compile("User time \\(seconds\\): ([0-9.]+)");
    private final Pattern systemTimePattern = Pattern.compile("System time \\(seconds\\): ([0-9.]+)");

    @Override
    public String getIdentifier() {
        return "timing.txt::time";
    }

    @Override
    public boolean apply(GradingResult result) {
        Map<String, byte[]> files = result.getFiles();
        if (files == null) return false;

        byte[] bytes = files.get("timing.txt");
        if (bytes == null) return false;

        String content = new String(bytes);
        Matcher userTimeMatcher = userTimePattern.matcher(content);
        Matcher systemTimeMatcher = systemTimePattern.matcher(content);
        if (!userTimeMatcher.find()) return false;
        if (!systemTimeMatcher.find()) return false;

        String userTimeString = userTimeMatcher.group(1);
        String systemTimeString = systemTimeMatcher.group(1);
        try {
            double time = (Double.parseDouble(userTimeString) + Double.parseDouble(systemTimeString)) * 1000;
            result.getMetadata().put("time", time);

            GradingRequest request = result.getRequest();
            if(request.getSettings() == null) return false;

            String timeLimitString = request.getSettings().get("softLimitTime");
            if (timeLimitString == null) return false;
            double timeLimit = 0; // in milliseconds
            try{
                timeLimit = Double.parseDouble(timeLimitString);
            }
            catch (NumberFormatException ex) {
                logger.error("Failed parsing time limit:" + timeLimitString, ex);
                return false;
            }

            if(time > timeLimit){
                result.setStatus(GradingStatus.TIMEOUT_EXECUTION);
            }
        } catch (NumberFormatException ex) {
            logger.error("Failed parsing time amount:" + userTimeString, ex);
            return false;
        }
        return true;
    }
}
