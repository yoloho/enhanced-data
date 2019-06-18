package com.yoloho.data.dao.generator;

import java.io.Serializable;
import java.util.UUID;

public class UUID32Generator implements Generator {

    @Override
    public Serializable generate(GeneratedContext context) {
        return UUID.randomUUID().toString().replace("-", "");
    }

}