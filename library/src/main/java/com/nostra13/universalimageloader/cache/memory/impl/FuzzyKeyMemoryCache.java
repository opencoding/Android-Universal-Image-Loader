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
package com.nostra13.universalimageloader.cache.memory.impl;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;

import java.util.Collection;
import java.util.Comparator;

/**
 * Decorator for {@link MemoryCache}. Provides special feature for cache: some different keys are considered as
 * equals (using {@link Comparator comparator}). And when you try to put some value into cache by key so entries with
 * "equals" keys will be removed from cache before.<br />
 * <b>NOTE:</b> Used for internal needs. Normally you don't need to use this class.
 * <p/>
 * 在put()时，若放入的key即图片的uri，已存在于缓存中，那么先删除缓存中的记录，再进行put新的
 * 封装缓存类
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.0.0
 */
public class FuzzyKeyMemoryCache implements MemoryCache {

    private final MemoryCache cache;
    private final Comparator<String> keyComparator;

    public FuzzyKeyMemoryCache(MemoryCache cache, Comparator<String> keyComparator) {
        this.cache = cache;
        this.keyComparator = keyComparator;
    }

    @Override
    public boolean put(String key, Bitmap value) {
        // Search equal key and remove this entry
        synchronized (cache) {
            String keyToRemove = null;
            for (String cacheKey : cache.keys()) {
                if (keyComparator.compare(key, cacheKey) == 0) {
                    keyToRemove = cacheKey;
                    break;
                }
            }
            //删除已经存在的，重新存储
            if (keyToRemove != null) {
                cache.remove(keyToRemove);
            }
        }
        return cache.put(key, value);
    }

    @Override
    public Bitmap get(String key) {
        return cache.get(key);
    }

    @Override
    public Bitmap remove(String key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Collection<String> keys() {
        return cache.keys();
    }
}
