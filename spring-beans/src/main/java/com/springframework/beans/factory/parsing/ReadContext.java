package com.springframework.beans.factory.parsing;

import com.springframework.core.io.Resource;

/**
 * Created by hadoop on 2015/5/7 0007.
 */
public class ReadContext {
    private final Resource resource;

    public ReadContext(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
}
