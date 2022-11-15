package dev.porama.gradingcore.utils;

import dev.porama.gradingcore.core.utils.ConfigUtils;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class ConfigUtilsTest {
    @Test
    public void test() {
        HashMap<String, byte[]> files = new HashMap<>();
        files.put("a.json", new byte[]{0, 1, 2, 3, 4, 5});
        files.put("b.json", new byte[]{5, 1, 2, 3, 4, 5});
        GradingRequest gradingRequest = new GradingRequest("java", files);
        System.out.println(ConfigUtils.toJson(gradingRequest));
    }

}