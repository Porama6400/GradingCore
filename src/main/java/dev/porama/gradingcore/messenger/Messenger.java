package dev.porama.gradingcore.messenger;

import dev.porama.gradingcore.messenger.message.Message;

public interface Messenger {
    void publish(Message message);
}
