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
package com.chestnut.contentcore.publish.log;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.status.ErrorStatus;
import com.chestnut.system.logs.LogQueue;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PublishLogAppender
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class PublishLogAppender<E> extends AppenderBase<E> {

    private static final int MAX_QUEUE_SIZE = 1000;
    private final LogQueue<LogEntry> logQueue = new LogQueue<>(MAX_QUEUE_SIZE);
    private final AtomicLong indexCounter = new AtomicLong(0);

    @Getter
    @Setter
    protected Layout<E> layout;

    @Getter
    @Setter
    protected Encoder<E> encoder;

    @Getter
    @Setter
    protected Charset charset = StandardCharsets.UTF_8;

    @Getter
    private static PublishLogAppender<?> instance;

    @Override
    public void start() {
        if (this.layout == null && encoder == null) {
            this.addStatus(new ErrorStatus("No pattern set for the appender named \"" + this.name + "\".", this));
        } else {
            if (encoder instanceof LayoutWrappingEncoder<?> layoutEncoder && layoutEncoder.getCharset() == null) {
                layoutEncoder.setCharset(charset);
            }
            super.start();
            instance = this;
        }
    }

    @Override
    protected void append(E evt) {
        long currentIndex = indexCounter.incrementAndGet();
        LogEntry entry = new LogEntry(
                currentIndex,
                encodeMessage(evt)
        );
        logQueue.add(entry);
    }

    private String encodeMessage(E logEvent) {
        if (this.encoder != null) {
            Charset encoderCharset = charset;
            if (this.encoder instanceof LayoutWrappingEncoder<?> layoutEncoder
                    && layoutEncoder.getCharset() != null) {
                encoderCharset = layoutEncoder.getCharset();
            }
            return new String(this.encoder.encode(logEvent), encoderCharset);
        }
        return this.layout.doLayout(logEvent);
    }

    public List<LogEntry> getLogsSince(int sinceIndex) {
        List<LogEntry> list = new ArrayList<>();
        for (LogEntry entry : logQueue) {
            if (entry.index() > sinceIndex) {
                list.add(entry);
            }
        }
        return list;
    }

    public record LogEntry(long index, String message) {}
}
