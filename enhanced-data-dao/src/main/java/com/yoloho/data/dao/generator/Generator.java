package com.yoloho.data.dao.generator;

import java.io.Serializable;

public interface Generator {

    Serializable generate(GeneratedContext context);

}
