package com.yoloho.cache;

import java.util.List;

public interface DemoService {
    String getValue();

    int getNewValue();

    int getNewValue(int n);

    int compose();

    List<Item> array();

    void update(int n);
}
