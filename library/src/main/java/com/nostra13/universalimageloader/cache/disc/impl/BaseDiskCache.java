/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.cache.disc.impl;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base disk cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see FileNameGenerator
 * @since 1.0.0
 */
public abstract class BaseDiskCache implements DiskCache {
    // 32 Kb 默认的缓存大小为32k
    /**
     * {@value
     */
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    //默认压缩后的图片格式为PNG
    /**
     * {@value
     */
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;

    //图片显示的质量为100,不进行压缩
    /**
     * {@value
     */
    public static final int DEFAULT_COMPRESS_QUALITY = 100;

    private static final String ERROR_ARG_NULL = " argument must be not null";
    private static final String TEMP_IMAGE_POSTFIX = ".tmp";
    /**
     * 缓存文件的保存Directory
     */
    protected final File cacheDir;
    /**
     * 后备缓存的Diectory，当cacheDir不存在的情况下就是用
     */
    protected final File reserveCacheDir;
    /**
     * 文件名名称生成器
     */
    protected final FileNameGenerator fileNameGenerator;

    protected int bufferSize = DEFAULT_BUFFER_SIZE;

    protected Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
    protected int compressQuality = DEFAULT_COMPRESS_QUALITY;

    /**
     * @param cacheDir Directory for file caching
     */
    public BaseDiskCache(File cacheDir) {
        this(cacheDir, null);
    }

    /**
     * @param cacheDir        Directory for file caching
     * @param reserveCacheDir null-ok; Reserve directory for file caching. It's used when the primary directory isn't available.
     */
    public BaseDiskCache(File cacheDir, File reserveCacheDir) {
        this(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator());
    }

    /**
     * @param cacheDir          Directory for file caching
     * @param reserveCacheDir   null-ok; Reserve directory for file caching. It's used when the primary directory isn't available.
     * @param fileNameGenerator {@linkplain com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator
     *                          Name generator} for cached files
     */
    public BaseDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir" + ERROR_ARG_NULL);
        }
        if (fileNameGenerator == null) {
            throw new IllegalArgumentException("fileNameGenerator" + ERROR_ARG_NULL);
        }

        this.cacheDir = cacheDir;
        this.reserveCacheDir = reserveCacheDir;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    public File getDirectory() {
        return cacheDir;
    }

    @Override
    public File get(String imageUri) {
        return getFile(imageUri);
    }

    @Override
    public boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException {
        File imageFile = getFile(imageUri);
        File tmpFile = new File(imageFile.getAbsolutePath() + TEMP_IMAGE_POSTFIX);
        boolean loaded = false;
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), bufferSize);
            try {
                loaded = IoUtils.copyStream(imageStream, os, listener, bufferSize);
            } finally {
                IoUtils.closeSilently(os);
            }
        } finally {
            if (loaded && !tmpFile.renameTo(imageFile)) {
                loaded = false;
            }
            if (!loaded) {
                tmpFile.delete();
            }
        }
        return loaded;
    }

    @Override
    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        //获取imageUri的File对象，该对象封装了缓存路径和图片保存后的名称
        File imageFile = getFile(imageUri);

        //获取临时保存文件的tmpFile对象
        File tmpFile = new File(imageFile.getAbsolutePath() + TEMP_IMAGE_POSTFIX);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), bufferSize);
        boolean savedSuccessfully = false;
        try {
            //调用compress把bitMap压缩到tempFile中
            savedSuccessfully = bitmap.compress(compressFormat, compressQuality, os);

        } finally {
            IoUtils.closeSilently(os);

            //如果保存成功并且tempFile的文件没有成功移动到imageFile的话，就删除temFile
            if (savedSuccessfully && !tmpFile.renameTo(imageFile)) {
                savedSuccessfully = false;
            }
            if (!savedSuccessfully) {
                tmpFile.delete();
            }
        }
        bitmap.recycle();
        return savedSuccessfully;
    }

    @Override
    public boolean remove(String imageUri) {
        return getFile(imageUri).delete();
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    /**
     * Returns file object (not null) for incoming image URI. File object can reference to non-existing file.
     */
    protected File getFile(String imageUri) {
        String fileName = fileNameGenerator.generate(imageUri);
        File dir = cacheDir;
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            if (reserveCacheDir != null && (reserveCacheDir.exists() || reserveCacheDir.mkdirs())) {
                dir = reserveCacheDir;
            }
        }
        return new File(dir, fileName);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public void setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
    }
}