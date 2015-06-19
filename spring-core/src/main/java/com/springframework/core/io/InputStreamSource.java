package com.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public interface InputStreamSource {


    InputStream getInputStream() throws IOException;

}
