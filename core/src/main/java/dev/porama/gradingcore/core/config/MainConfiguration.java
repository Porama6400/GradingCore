package dev.porama.gradingcore.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainConfiguration {
    private boolean debug;
    private String messengerUri;
    private String influxUrl;
    private String influxToken;
    private String influxOrg;
    private String influxBucket;
    private String hostname;
    private int parallelism;
    private int maxRequeue;
    private int tickInterval;
    private int timeSlotWidth;
}
