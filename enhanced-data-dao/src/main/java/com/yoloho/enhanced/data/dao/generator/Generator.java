package com.yoloho.enhanced.data.dao.generator;

import java.io.Serializable;

public interface Generator {

    Serializable generate(GeneratedContext context);

}
