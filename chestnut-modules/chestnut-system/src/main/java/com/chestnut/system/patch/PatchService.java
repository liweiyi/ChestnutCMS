package com.chestnut.system.patch;

import com.chestnut.system.domain.SysUpdatePatcher;
import com.chestnut.system.mapper.SysUpdatePatcherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 更新补丁执行服务
 *
 * 按版本顺序逐个执行
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@Slf4j
@Lazy(false)
@DependsOnDatabaseInitialization
@RequiredArgsConstructor
public class PatchService implements SmartInitializingSingleton {

    private static final String LOCK_KEY = "chestnut:update:patcher";

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)");

    private final RedissonClient redissonClient;

    private final List<IUpdatePatcher> updatePatchers;

    private final SysUpdatePatcherMapper updatePatcherMapper;

    /**
     * 普通单例及数据库初始化完成后同步执行，先于内嵌 Web 服务和 Runner 启动。
     * 不统一开启事务：补丁自行管理事务，成功记录只在 update() 正常返回后写入。
     */
    @Override
    public void afterSingletonsInstantiated() {
        RLock lock = null;
        boolean acquired = false;
        Throwable failure = null;
        String version = null;
        int executed = 0;
        int skipped = 0;
        try {
            List<VersionedPatcher> sorted = sortedPatchers();
            if (!sorted.isEmpty()) {
                lock = redissonClient.getLock(LOCK_KEY);
                log.info("Waiting for system update lock; patches: {}", sorted.size());
                // 不指定 leaseTime，使用 Redisson watchdog 自动续期。
                lock.lockInterruptibly();
                acquired = true;
            }
            for (VersionedPatcher patcher : sorted) {
                version = patcher.version();
                requireLockOwnership(lock);
                if (updatePatcherMapper.selectById(version) != null) {
                    skipped++;
                    log.info("Skipping completed system update patch {}", version);
                    continue;
                }

                Instant startTime = Instant.now();
                long startNanos = System.nanoTime();
                log.info("Executing system update patch {}", version);
                patcher.patcher().update();

                requireLockOwnership(lock);
                SysUpdatePatcher record = new SysUpdatePatcher();
                record.setPatcherId(version);
                record.setStartTime(startTime);
                record.setCostSeconds(Math.toIntExact(
                        TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startNanos)));
                if (updatePatcherMapper.insert(record) != 1) {
                    throw new IllegalStateException("Failed to save successful system update patch " + version);
                }
                executed++;
                log.info("Completed system update patch {}, cost: {}s", version, record.getCostSeconds());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            IllegalStateException interrupted = new IllegalStateException("Interrupted waiting for system update lock", e);
            failure = interrupted;
            log.error("System update interrupted; aborting application startup", interrupted);
            throw interrupted;
        } catch (RuntimeException | Error e) {
            failure = e;
            log.error("System update failed (patch: {}); aborting application startup", version, e);
            throw e;
        } finally {
            if (acquired) {
                try {
                    requireLockOwnership(lock);
                    lock.unlock();
                } catch (RuntimeException | Error e) {
                    if (failure != null && failure != e) {
                        failure.addSuppressed(e);
                    }
                    log.error("Failed to release system update lock", e);
                    if (failure == null) {
                        throw e;
                    }
                }
            }
        }
        log.info("System update complete; executed: {}, skipped: {}", executed, skipped);
    }

    private List<VersionedPatcher> sortedPatchers() {
        Set<String> versions = new HashSet<>();
        return updatePatchers.stream().map(patcher -> {
            String version = patcher.getVersion();
            if (version == null || version.length() > 32 || !VERSION_PATTERN.matcher(version).matches()) {
                throw new IllegalStateException("Invalid system update patch version: " + version);
            }
            if (!versions.add(version)) {
                throw new IllegalStateException("Duplicate system update patch version: " + version);
            }
            try {
                int[] parts = Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
                return new VersionedPatcher(version, parts, patcher);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("System update patch version segment exceeds integer range: " + version, e);
            }
        }).sorted((first, second) -> Arrays.compare(first.parts(), second.parts())).toList();
    }

    private void requireLockOwnership(RLock lock) {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("System update lock is no longer held by the current thread");
        }
    }

    private record VersionedPatcher(String version, int[] parts, IUpdatePatcher patcher) {
    }
}
