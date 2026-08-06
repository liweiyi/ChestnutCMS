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
package com.chestnut.common.async;

import com.chestnut.common.async.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncTaskContextTest {

    @Test
    void exposesTaskToHelpersAndCleansScopeAfterSuccess() {
        Locale locale = Locale.CANADA_FRENCH;
        AtomicInteger observedPercent = new AtomicInteger();

        AsyncTask task = new AsyncTask(locale) {
            @Override
            public void run0() {
                AsyncTaskManager.setTaskProgressInfo(40, "processing");
                assertEquals(40, AsyncTaskManager.getTaskProgressPercent());
                assertEquals(locale, AsyncTaskManager.getLocale());

                AsyncTaskManager.addErrMessage("warning");
                AsyncTaskManager.setTaskTenPercentProgressInfo("next step");
                observedPercent.set(AsyncTaskManager.getTaskProgressPercent());
            }
        };

        task.run();

        assertEquals(46, observedPercent.get());
        assertEquals(TaskStatus.SUCCESS, task.getStatus());
        assertEquals(100, task.getPercent());
        assertEquals("next step", task.getProgressMessage());
        assertEquals("1.warning", task.getErrMessages().get(0));
        assertNotNull(task.getEndTime());

        assertEquals(0, AsyncTaskManager.getTaskProgressPercent());
        AsyncTaskManager.setTaskPercent(10);
        assertEquals(100, task.getPercent());
    }

    @Test
    void cleansScopeWhenTaskFails() {
        AsyncTask task = new AsyncTask() {
            @Override
            public void run0() {
                AsyncTaskManager.setTaskProgressInfo(35, "before failure");
                throw new IllegalStateException("expected failure");
            }
        };

        task.run();

        assertEquals(TaskStatus.FAILED, task.getStatus());
        assertEquals(100, task.getPercent());
        assertEquals("before failure", task.getProgressMessage());
        assertTrue(task.getErrMessages().get(0).contains("expected failure"));
        assertNotNull(task.getEndTime());
        assertEquals(0, AsyncTaskManager.getTaskProgressPercent());
    }

    @Test
    void restoresOuterTaskAfterNestedTaskCompletes() {
        AtomicInteger innerObservedPercent = new AtomicInteger();
        AtomicInteger outerPercentBeforeNestedTask = new AtomicInteger();
        AtomicInteger outerPercentAfterNestedTask = new AtomicInteger();

        AsyncTask innerTask = new AsyncTask() {
            @Override
            public void run0() {
                assertEquals(0, AsyncTaskManager.getTaskProgressPercent());
                AsyncTaskManager.setTaskPercent(70);
                innerObservedPercent.set(AsyncTaskManager.getTaskProgressPercent());
            }
        };
        AsyncTask outerTask = new AsyncTask() {
            @Override
            public void run0() {
                AsyncTaskManager.setTaskPercent(20);
                outerPercentBeforeNestedTask.set(AsyncTaskManager.getTaskProgressPercent());
                innerTask.run();
                outerPercentAfterNestedTask.set(AsyncTaskManager.getTaskProgressPercent());
            }
        };

        outerTask.run();

        assertEquals(20, outerPercentBeforeNestedTask.get());
        assertEquals(70, innerObservedPercent.get());
        assertEquals(20, outerPercentAfterNestedTask.get());
        assertEquals(TaskStatus.SUCCESS, innerTask.getStatus());
        assertEquals(TaskStatus.SUCCESS, outerTask.getStatus());
        assertEquals(0, AsyncTaskManager.getTaskProgressPercent());
    }

    @Test
    void isolatesTasksRunningOnWorkerThreads() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger firstObservedPercent = new AtomicInteger();
        AtomicInteger secondObservedPercent = new AtomicInteger();
        AsyncTask firstTask = concurrentTask(25, firstObservedPercent, barrier);
        AsyncTask secondTask = concurrentTask(75, secondObservedPercent, barrier);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> firstFuture = executor.submit(firstTask);
            Future<?> secondFuture = executor.submit(secondTask);
            firstFuture.get(5, TimeUnit.SECONDS);
            secondFuture.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }

        assertEquals(25, firstObservedPercent.get());
        assertEquals(75, secondObservedPercent.get());
        assertEquals(TaskStatus.SUCCESS, firstTask.getStatus());
        assertEquals(TaskStatus.SUCCESS, secondTask.getStatus());
        assertEquals(0, AsyncTaskManager.getTaskProgressPercent());
    }

    @Test
    void resolvesInterruptAgainstScopedTask() {
        AtomicBoolean continuedAfterInterruptCheck = new AtomicBoolean(false);
        AsyncTask task = new AsyncTask() {
            @Override
            public void run0() throws Exception {
                interrupt();
                AsyncTaskManager.checkInterrupt();
                continuedAfterInterruptCheck.set(true);
            }
        };
        task.setInterruptible(true);

        task.run();

        assertEquals(TaskStatus.INTERRUPTED, task.getStatus());
        assertFalse(continuedAfterInterruptCheck.get());
        assertEquals(0, AsyncTaskManager.getTaskProgressPercent());
    }

    private static AsyncTask concurrentTask(int percent, AtomicInteger observedPercent, CyclicBarrier barrier) {
        return new AsyncTask() {
            @Override
            public void run0() throws Exception {
                AsyncTaskManager.setTaskPercent(percent);
                barrier.await(5, TimeUnit.SECONDS);
                observedPercent.set(AsyncTaskManager.getTaskProgressPercent());
            }
        };
    }
}
