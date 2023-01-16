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

public class MemoryLimitPostProcessor implements PostProcessor {

    private final Logger logger = LoggerFactory.getLogger(MemoryLimitPostProcessor.class);
    private final Pattern pattern = Pattern.compile("Maximum resident set size \\(kbytes\\): ([0-9]+)");

    @Override
    public String getIdentifier() {
        return "timing.txt::memory";
    }

    @Override
    public boolean apply(GradingResult result) {
        Map<String, byte[]> files = result.getFiles();
        if (files == null) return false;

        byte[] bytes = files.get("timing.txt");
        if (bytes == null) return false;

        String content = new String(bytes);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return false;

        String memoryString = matcher.group(1);
        try {
            long memory = Long.parseLong(memoryString); // in kB
            result.getMetadata().put("memory", memory);

            GradingRequest request = result.getRequest();
            if (request.getSettings() == null) return false;

            String memoryLimitString = request.getSettings().get("softLimitMemory");
            if (memoryLimitString == null) return false;
            double memoryLimit = 0; // in MB
            try {
                memoryLimit = Double.parseDouble(memoryLimitString);
            } catch (NumberFormatException ex) {
                logger.error("Failed parsing memory limit:" + memoryLimitString, ex);
                return false;
            }

            if ((memory / 1000D) > memoryLimit) {
                result.setStatus(GradingStatus.FAILED_MEMORY_LIMIT);
            }
        } catch (NumberFormatException ex) {
            logger.error("Failed parsing memory amount:" + memoryString, ex);
            return false;
        }

        return true;
    }
}
