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
package com.chestnut.common.log.aspectj;

import com.chestnut.common.log.ILogType;
import com.chestnut.common.log.annotation.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogAspectContextTest {

    private static final String NORMAL_TYPE = "LogType_TestNormal";
    private static final String OUTER_TYPE = "LogType_TestOuter";
    private static final String INNER_TYPE = "LogType_TestInner";
    private static final String CONTEXT_KEY = "scope";

    @Test
    void exposesContextThroughoutInvocationAndCleansItAfterSuccess() throws Throwable {
        AtomicReference<Object> afterResult = new AtomicReference<>();
        ILogType logType = new TestLogType(NORMAL_TYPE) {
            @Override
            public void beforeProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime) {
                LogAspect.put(CONTEXT_KEY, "normal");
                LogAspect.put("number", 42L);
            }

            @Override
            public void afterProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime,
                                     Object result, Throwable exception) {
                assertEquals("normal", LogAspect.getString(CONTEXT_KEY));
                assertEquals(42L, LogAspect.getLongValue("number"));
                assertNull(exception);
                afterResult.set(result);
            }
        };
        LogAspect aspect = new LogAspect(Map.of(NORMAL_TYPE, logType));
        ProceedingJoinPoint joinPoint = joinPoint(() -> {
            assertEquals("normal", LogAspect.getString(CONTEXT_KEY));
            assertEquals("normal", LogAspect.get(CONTEXT_KEY).orElseThrow());
            return "result";
        });

        Object result = aspect.around(joinPoint, annotation("normalOperation"));

        assertEquals("result", result);
        assertEquals("result", afterResult.get());
        assertContextIsUnbound();
    }

    @Test
    void cleansContextAfterFailureWhileMakingItAvailableToAfterProceed() throws Throwable {
        RuntimeException expected = new RuntimeException("expected failure");
        AtomicReference<Throwable> afterException = new AtomicReference<>();
        ILogType logType = new TestLogType(NORMAL_TYPE) {
            @Override
            public void beforeProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime) {
                LogAspect.put(CONTEXT_KEY, "failure");
            }

            @Override
            public void afterProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime,
                                     Object result, Throwable exception) {
                assertEquals("failure", LogAspect.getString(CONTEXT_KEY));
                assertNull(result);
                afterException.set(exception);
            }
        };
        LogAspect aspect = new LogAspect(Map.of(NORMAL_TYPE, logType));
        ProceedingJoinPoint joinPoint = joinPoint(() -> {
            throw expected;
        });

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> aspect.around(joinPoint, annotation("normalOperation")));

        assertSame(expected, actual);
        assertSame(expected, afterException.get());
        assertContextIsUnbound();
    }

    @Test
    void restoresOuterContextAfterNestedInvocation() throws Throwable {
        AtomicReference<String> innerContextBeforePut = new AtomicReference<>();
        AtomicReference<String> outerContextAfterInner = new AtomicReference<>();
        AtomicReference<String> outerAfterProceedContext = new AtomicReference<>();
        AtomicReference<String> innerAfterProceedContext = new AtomicReference<>();

        ILogType outerLogType = new TestLogType(OUTER_TYPE) {
            @Override
            public void beforeProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime) {
                LogAspect.put(CONTEXT_KEY, "outer");
            }

            @Override
            public void afterProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime,
                                     Object result, Throwable exception) {
                outerAfterProceedContext.set(LogAspect.getString(CONTEXT_KEY));
            }
        };
        ILogType innerLogType = new TestLogType(INNER_TYPE) {
            @Override
            public void beforeProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime) {
                innerContextBeforePut.set(LogAspect.getString(CONTEXT_KEY));
                LogAspect.put(CONTEXT_KEY, "inner");
            }

            @Override
            public void afterProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime,
                                     Object result, Throwable exception) {
                innerAfterProceedContext.set(LogAspect.getString(CONTEXT_KEY));
            }
        };
        LogAspect aspect = new LogAspect(Map.of(OUTER_TYPE, outerLogType, INNER_TYPE, innerLogType));
        ProceedingJoinPoint innerJoinPoint = joinPoint(() -> {
            assertEquals("inner", LogAspect.getString(CONTEXT_KEY));
            return "inner result";
        });
        ProceedingJoinPoint outerJoinPoint = joinPoint(() -> {
            assertEquals("outer", LogAspect.getString(CONTEXT_KEY));
            aspect.around(innerJoinPoint, annotation("innerOperation"));
            outerContextAfterInner.set(LogAspect.getString(CONTEXT_KEY));
            return "outer result";
        });

        Object result = aspect.around(outerJoinPoint, annotation("outerOperation"));

        assertEquals("outer result", result);
        assertEquals("", innerContextBeforePut.get());
        assertEquals("inner", innerAfterProceedContext.get());
        assertEquals("outer", outerContextAfterInner.get());
        assertEquals("outer", outerAfterProceedContext.get());
        assertContextIsUnbound();
    }

    @Test
    void isolatesConcurrentInvocationsOnWorkerThreads() throws Throwable {
        CyclicBarrier barrier = new CyclicBarrier(2);
        ILogType logType = new TestLogType(NORMAL_TYPE) {
            @Override
            public void beforeProceed(ProceedingJoinPoint joinPoint, Log log, LocalDateTime logTime) {
                LogAspect.put(CONTEXT_KEY, joinPoint.getArgs()[0]);
            }
        };
        LogAspect aspect = new LogAspect(Map.of(NORMAL_TYPE, logType));
        Log log = annotation("normalOperation");
        ProceedingJoinPoint firstJoinPoint = concurrentJoinPoint("first", barrier);
        ProceedingJoinPoint secondJoinPoint = concurrentJoinPoint("second", barrier);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Object> firstResult = executor.submit(() -> invoke(aspect, firstJoinPoint, log));
            Future<Object> secondResult = executor.submit(() -> invoke(aspect, secondJoinPoint, log));
            assertEquals("first", firstResult.get(5, TimeUnit.SECONDS));
            assertEquals("second", secondResult.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }

        assertContextIsUnbound();
    }

    @Test
    void proceedsWithoutCreatingContextWhenLogTypeIsMissing() throws Throwable {
        LogAspect aspect = new LogAspect(Map.of());
        ProceedingJoinPoint joinPoint = joinPoint(() -> "plain result");

        Object result = aspect.around(joinPoint, annotation("normalOperation"));

        assertEquals("plain result", result);
        assertContextIsUnbound();
    }

    private static ProceedingJoinPoint concurrentJoinPoint(String value, CyclicBarrier barrier) {
        return new TestProceedingJoinPoint(new Object[]{value}, () -> {
            barrier.await(5, TimeUnit.SECONDS);
            return LogAspect.getString(CONTEXT_KEY);
        });
    }

    private static ProceedingJoinPoint joinPoint(Invocation invocation) {
        return new TestProceedingJoinPoint(new Object[0], invocation);
    }

    private static Object invoke(LogAspect aspect, ProceedingJoinPoint joinPoint, Log log) {
        try {
            return aspect.around(joinPoint, log);
        } catch (Throwable exception) {
            throw new AssertionError(exception);
        }
    }

    private static Log annotation(String methodName) throws NoSuchMethodException {
        Method method = LogAspectContextTest.class.getDeclaredMethod(methodName);
        return method.getAnnotation(Log.class);
    }

    private static void assertContextIsUnbound() {
        assertFalse(LogAspect.get(CONTEXT_KEY).isPresent());
        assertEquals("", LogAspect.getString(CONTEXT_KEY));
        assertEquals(0L, LogAspect.getLongValue(CONTEXT_KEY));
        assertThrows(IllegalStateException.class, () -> LogAspect.put(CONTEXT_KEY, "outside"));
    }

    @Log(type = NORMAL_TYPE)
    private void normalOperation() {
    }

    @Log(type = OUTER_TYPE)
    private void outerOperation() {
    }

    @Log(type = INNER_TYPE)
    private void innerOperation() {
    }

    private abstract static class TestLogType implements ILogType {

        private final String type;

        private TestLogType(String type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }
    }

    @FunctionalInterface
    private interface Invocation {

        Object proceed() throws Throwable;
    }

    private static final class TestProceedingJoinPoint implements ProceedingJoinPoint {

        private final Object[] args;

        private final Invocation invocation;

        private TestProceedingJoinPoint(Object[] args, Invocation invocation) {
            this.args = args;
            this.invocation = invocation;
        }

        @Override
        public void set$AroundClosure(AroundClosure aroundClosure) {
        }

        @Override
        public Object proceed() throws Throwable {
            return invocation.proceed();
        }

        @Override
        public Object proceed(Object[] args) throws Throwable {
            return invocation.proceed();
        }

        @Override
        public String toShortString() {
            return "testJoinPoint";
        }

        @Override
        public String toLongString() {
            return "testJoinPoint";
        }

        @Override
        public Object getThis() {
            return this;
        }

        @Override
        public Object getTarget() {
            return this;
        }

        @Override
        public Object[] getArgs() {
            return args;
        }

        @Override
        public Signature getSignature() {
            return null;
        }

        @Override
        public SourceLocation getSourceLocation() {
            return null;
        }

        @Override
        public String getKind() {
            return JoinPoint.METHOD_EXECUTION;
        }

        @Override
        public StaticPart getStaticPart() {
            return null;
        }

        @Override
        public String toString() {
            return "testJoinPoint";
        }
    }
}
