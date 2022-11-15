package dev.porama.gradingcore.core.messenger;

import dev.porama.gradingcore.core.messenger.message.Message;

public interface Messenger {
    void publish(Message message);
}
