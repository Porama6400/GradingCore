package dev.porama.gradingcore.messenger.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RabbitConfig {
    private String username;
    private String password;
    private String host;
    private String virtualHost;
    private int port;
}
