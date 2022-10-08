package dev.porama.gradingcore.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainConfiguration {
    private boolean debug;
    private String messengerUri;
}
