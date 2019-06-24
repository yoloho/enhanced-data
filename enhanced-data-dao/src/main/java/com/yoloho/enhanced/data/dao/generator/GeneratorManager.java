package com.yoloho.enhanced.data.dao.generator;

import java.util.HashMap;
import java.util.Map;

import com.yoloho.enhanced.common.util.ReflectUtils;

public class GeneratorManager {

    private static Map<GenerateStrategy, Generator> defaultGenerators = new HashMap<>();
    private static Map<String, Generator> customGenerators = new HashMap<>();

    static {
        defaultGenerators.put(GenerateStrategy.uuid32, new UUID32Generator());
    }

    public static void fillField(Object entity, GeneratedField generatedField) {
        try {
            Object value = generateValue(entity, generatedField);
            ReflectUtils.setValue(generatedField.getField(), entity, value);
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    public static Object generateValue(Object entity, GeneratedField field) {
        if (field.getGenerateStrategy() != null) {
            if (field.getGenerateStrategy() == GenerateStrategy.custom) {
                return customGenerators.get(field.getGenerator()).generate(new GeneratedContext(entity, field));
            } else {
                return defaultGenerators.get(field.getGenerateStrategy()).generate(new GeneratedContext(entity, field));
            }
        } else {
            return null;
        }
    }

}