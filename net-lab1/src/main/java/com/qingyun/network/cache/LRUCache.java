package com.qingyun.network.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


/**
 * @description： 基于LRU淘汰策略的缓存系统
 * @author: 張青云
 * @create: 2021-10-27 20:23
 **/
public class LRUCache implements Cache {
    /**
     * 默认的代理服务器要缓存的请求的最大容量
     */
    private static final int MAX_CAPACITY = 1024;

    //  读写锁
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    //  读锁
    private final WriteLock writeLock = lock.writeLock();

    //  写锁
    private final ReadLock readLock = lock.readLock();

    private final LRU<String, byte[]> lru;


    public LRUCache() {
        lru = new LRU<>(MAX_CAPACITY);
    }

    public LRUCache(int maxCapacity) {
        lru = new LRU<>(maxCapacity);
    }

    @Override
    public void addCache(String url, byte[] content) {
        writeLock.lock();
        try {
            lru.put(url, content);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public byte[] getContent(String url) {
        readLock.lock();
        try {
            return lru.get(url);
        } finally {
            readLock.unlock();
        }
    }


    /**
     * 具体的LRU数据结构 <br/>
     * 不是线程安全的，需要自行解决线程安全问题
     */
    static class LRU<K, V> extends LinkedHashMap<K, V> {
        //  最大的容量
        private final int maxCapacity;

        public LRU(int maxCapacity) {
            //  accessOrder参数为true时，当调用get和put方法时会将访问到的元素放到双向链表的尾部
            super(16, 0.75f, true);
            this.maxCapacity = maxCapacity;
        }

        //  实现LRU的关键方法，如果map里面的元素个数大于了缓存最大容量，则返回true，然后会删除链表的顶端元素eldest
        @Override
        public boolean removeEldestEntry(Map.Entry<K, V> eldest){
            return size() > maxCapacity;
        }
    }
}
