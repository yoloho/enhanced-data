package com.yoloho.enhanced.data.dao.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.yoloho.enhanced.data.dao.generator.GeneratedContext;
import com.yoloho.enhanced.data.dao.generator.GeneratedField;
import com.yoloho.enhanced.data.dao.generator.UUID32Generator;

public class UUID32GeneratorTest {
    @Test
    public void test() {
        GeneratedContext context = new GeneratedContext(new Object(), new GeneratedField(null, null));
        String result = (String)new UUID32Generator().generate(context);
        assertNotNull(result);
        assertEquals(32, result.length());
    }
}
