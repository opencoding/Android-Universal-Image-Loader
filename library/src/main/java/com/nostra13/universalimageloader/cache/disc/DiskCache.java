/*******************************************************************************
 * Copyright 2014 Sergey Tarasevich
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
package com.nostra13.universalimageloader.cache.disc;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for disk cache
 * 磁盘缓存
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.9.2
 */
public interface DiskCache {
    /**
     * Returns root directory of disk cache
     * 返回磁盘缓存的根目录
     *
     * @return Root directory of disk cache
     */
    File getDirectory();

    /**
     * Returns file of cached image
     * 根据uri从缓存中获取图片
     *
     * @param imageUri Original image URI
     * @return File of cached image or <b>null</b> if image wasn't cached
     */
    File get(String imageUri);

    /**
     * Saves image stream in disk cache.
     * Incoming image stream shouldn't be closed in this method.
     * 把图片保存在磁盘缓存上
     *
     * @param imageUri    Original image URI
     * @param imageStream Input stream of image (shouldn't be closed in this method)
     * @param listener    Listener for saving progress, can be ignored if you don't use
     *                    {@linkplain com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener
     *                    progress listener} in ImageLoader calls
     * @return <b>true</b> - if image was saved successfully; <b>false</b> - if image wasn't saved in disk cache.
     * @throws java.io.IOException
     */
    boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException;

    /**
     * Saves image bitmap in disk cache.
     * 保存bitMap对象到磁盘缓存上
     *
     * @param imageUri Original image URI
     * @param bitmap   Image bitmap
     * @return <b>true</b> - if bitmap was saved successfully; <b>false</b> - if bitmap wasn't saved in disk cache.
     * @throws IOException
     */
    boolean save(String imageUri, Bitmap bitmap) throws IOException;

    /**
     * Removes image file associated with incoming URI
     * 根据imageUri删除文件
     *
     * @param imageUri Image URI
     * @return <b>true</b> - if image file is deleted successfully; <b>false</b> - if image file doesn't exist for
     * incoming URI or image file can't be deleted.
     */
    boolean remove(String imageUri);

    /**
     * Closes disk cache, releases resources.
     * 关闭磁盘缓存，释放资源
     */
    void close();

    /**
     * Clears disk cache.
     * 清空磁盘缓存
     */
    void clear();
}
