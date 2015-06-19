package com.springframework.core.io.support;

import com.springframework.core.io.InputStreamSource;
import com.springframework.core.io.Resource;
import com.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public class EncodedResource implements InputStreamSource {
    private final Resource resource;
    private final Charset charset;
    private final String encoding;


    public EncodedResource(){
        this(null,null,null);
    }

    public EncodedResource(Resource resource){
        this(resource,null,null);
    }

    public EncodedResource(Resource resource, String encoding) {
        this(resource,null,encoding);
    }

    public EncodedResource(Resource resource, Charset charset) {
        this(resource,charset,null);

    }

    public EncodedResource(Resource resource, Charset charset, String encoding) {
        super();
        this.resource = resource;
        this.charset = charset;
        this.encoding = encoding;
    }

    public Resource getResource() {
        return resource;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EncodedResource)) {
            return false;
        }
        EncodedResource otherResource = (EncodedResource) other;
        return (this.resource.equals(otherResource.resource) &&
                ObjectUtils.nullSafeEquals(this.charset, otherResource.charset) &&
                ObjectUtils.nullSafeEquals(this.encoding, otherResource.encoding));
    }

    /**
     * Open a {@code java.io.Reader} for the specified resource, using the specified
     * {@link #getCharset() Charset} or {@linkplain #getEncoding() encoding}
     * (if any).
     * @throws IOException if opening the Reader failed
     */
    public Reader getReader() throws IOException {
        if (this.charset != null) {
            return new InputStreamReader(this.resource.getInputStream(), this.charset);
        }
        else if (this.encoding != null) {
            return new InputStreamReader(this.resource.getInputStream(), this.encoding);
        }
        else {
            return new InputStreamReader(this.resource.getInputStream());
        }
    }

    /**
     * Determine whether a {@link Reader} is required as opposed to an {@link InputStream},
     * i.e. whether an {@linkplain #getEncoding() encoding} or a {@link #getCharset() Charset}
     * has been specified.
     * @see #getReader()
     * @see #getInputStream()
     */
    public boolean requiresReader() {
        return (this.encoding != null || this.charset != null);
    }

    @Override
    public int hashCode() {
        return this.resource.hashCode();
    }

    @Override
    public String toString() {
        return this.resource.toString();
    }
}
