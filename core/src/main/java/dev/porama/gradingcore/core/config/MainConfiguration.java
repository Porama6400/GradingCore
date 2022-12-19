package dev.porama.gradingcore.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainConfiguration {
    private boolean debug;
    private String messengerUri;
    private int parallelism;
}
