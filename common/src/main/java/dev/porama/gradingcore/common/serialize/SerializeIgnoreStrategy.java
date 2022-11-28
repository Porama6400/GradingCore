package dev.porama.gradingcore.common.serialize;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class SerializeIgnoreStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes field) {
        return field.getAnnotation(SerializeIgnore.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
