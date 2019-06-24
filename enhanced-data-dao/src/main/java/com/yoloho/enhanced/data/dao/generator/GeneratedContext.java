package com.yoloho.enhanced.data.dao.generator;

public class GeneratedContext implements java.io.Serializable {

    private static final long serialVersionUID = 3763534659393847693L;

    private Object entity;
    private GeneratedField generatedField;

    public GeneratedContext(Object entity, GeneratedField field) {
        this.entity = entity;
        this.generatedField = field;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public GeneratedField getGeneratedField() {
        return generatedField;
    }

    public void setGeneratedField(GeneratedField generatedField) {
        this.generatedField = generatedField;
    }

}