package dev.porama.gradingcore.core.messenger.message;

import lombok.Data;

@Data
public class NestMessageWrapper<T> {
    private String pattern;
    private T data;

    public NestMessageWrapper(String pattern, T data) {
        this.pattern = pattern;
        this.data = data;
    }
}
