package dev.porama.gradingcore.core.grader.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GradingResult  {
    private Map<String, byte[]> files = new ConcurrentHashMap<>();
}
