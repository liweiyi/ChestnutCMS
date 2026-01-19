/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.system.logs;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 固定长度FIFO队列，队列满直接移除最早添加的元素
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class LogQueue<E> extends AbstractQueue<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Object[] elements;

    private int head;

    private int tail;

    private int size;

    @Getter
    private final int capacity;

    public LogQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must great than zero.");
        }
        this.capacity = capacity;
        this.elements = new Object[this.capacity];
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        lock.readLock().lock();
        try {
            Object[] snapshot = new Object[size];
            int current = head;
            for (int i = 0; i < size; i++) {
                snapshot[i] = elements[current];
                current = (current + 1) % capacity;
            }
            return new Iterator<>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < snapshot.length;
                }

                @Override
                public E next() {
                    //noinspection unchecked
                    return (E) snapshot[index++];
                }
            };

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException("The queue entry cannot be null.");
        }
        lock.writeLock().lock();
        try {
            if (size == capacity) {
                head = (head + 1) % capacity;
                size--;
            }
            elements[tail] = e;
            tail = (tail + 1) % capacity;
            size++;
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public E poll() {
        lock.writeLock().lock();
        try {
            if (size == 0) {
                return null;
            }
            //noinspection unchecked
            E element = (E) elements[head];
            elements[head] = null;
            head = (head + 1) % capacity;
            size--;
            return element;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public E peek() {
        lock.readLock().lock();
        try {
            if (size == 0) {
                return null;
            }
            //noinspection unchecked
            return (E) elements[head];
        } finally {
            lock.readLock().unlock();
        }
    }
}
