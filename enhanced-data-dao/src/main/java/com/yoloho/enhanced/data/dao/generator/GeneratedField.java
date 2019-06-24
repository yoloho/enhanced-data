package com.yoloho.enhanced.data.dao.generator;

import java.io.Serializable;
import java.lang.reflect.Field;

public class GeneratedField implements Serializable {

    private static final long serialVersionUID = 8139030621460691983L;

    private Field field;
    private GeneratedValue generatedValue;

    public GeneratedField(Field field, GeneratedValue generatedValue) {
        this.field = field;
        this.generatedValue = generatedValue;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getFieldName() {
        return field.getName();
    }

    public GeneratedValue getGeneratedValue() {
        return generatedValue;
    }

    public void setGeneratedValue(GeneratedValue generatedValue) {
        this.generatedValue = generatedValue;
    }

    public GenerateStrategy getGenerateStrategy() {
        return generatedValue.strategy();
    }

    public String getGenerator() {
        return generatedValue.generator();
    }

}