package com.yoloho.data.dao.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UUID32GeneratorTest {
    @Test
    public void test() {
        GeneratedContext context = new GeneratedContext(new Object(), new GeneratedField(null, null));
        String result = (String)new UUID32Generator().generate(context);
        assertNotNull(result);
        assertEquals(32, result.length());
    }
}
