package com.springframework.core.io;

import com.springframework.util.ClassUtils;
import com.springframework.util.ObjectUtils;
import com.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public class ClassPathResource extends AbstractFileResolvingResource {
    private final String path;
    private  Class<?> clazz;

    private ClassLoader classLoader;


    public ClassPathResource(String path) {
        this(path,null,null);
    }
    public ClassPathResource(String path, Class<?> clazz) {
        this(path,clazz,null);
    }
    public ClassPathResource(String path, ClassLoader classLoader) {
        this(path,null,classLoader);
    }


    public ClassPathResource(String path, Class<?> clazz, ClassLoader classLoader) {
        this.path = path;
        this.clazz = clazz;
        this.classLoader = classLoader;
    }

    /**
     * This implementation checks for the resolution of a resource URL.
     */
    @Override
    public boolean exists() {
        return (resolveURL() != null);
    }

    /**
     * Resolves a URL for the underlying class path resource.
     */
    protected URL resolveURL() {
        if (this.clazz != null) {
            return this.clazz.getResource(this.path);
        }
        else if (this.classLoader != null) {
            return this.classLoader.getResource(this.path);
        }
        else {
            return ClassLoader.getSystemResource(this.path);
        }
    }


    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if(clazz!=null){
            is = clazz.getResourceAsStream(path);
        }else if(classLoader!=null){
            is = classLoader.getResourceAsStream(path);
        }
        else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        return is;
    }

    @Override
    public URL getURL() throws IOException {
        URL url = resolveURL();
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder("class path resource [");
        String pathToUse = path;
        if (this.clazz != null && !pathToUse.startsWith("/")) {
            builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
            builder.append('/');
        }
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        builder.append(pathToUse);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public String getFilename() {
        return StringUtils.getFilename(this.path);
    }

    /**
     * This implementation compares the underlying class path locations.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassPathResource) {
            ClassPathResource otherRes = (ClassPathResource) obj;
            return (this.path.equals(otherRes.path) &&
                    ObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader) &&
                    ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
        }
        return false;
    }

    /**
     * This implementation returns the hash code of the underlying
     * class path location.
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

    public String getPath() {
        return path;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
