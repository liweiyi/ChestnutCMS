package com.chestnut.system.service.impl;

import com.chestnut.system.exception.DistributedLockException;
import com.chestnut.system.exception.DistributedLockException.Reason;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 同步分布式锁执行服务，统一管理获取锁、执行业务和释放锁。
 *
 * <p>默认立即尝试获取可重入锁，使用 Redisson 看门狗续期。{@code lock} 系列在竞争失败时
 * 抛出 {@link DistributedLockException}；{@code tryLock} 系列返回是否获取成功，或执行调用方
 * 指定的未获锁处理。Redis 故障、中断和释放失败均抛出异常，不作为普通竞争失败处理。</p>
 * <p>通过构造器注入 LockService。示例中的 LockOptions、LockType、LockResult 为本类的
 * 静态嵌套类型，调用方可直接导入这些类型。</p>
 *
 * <pre>{@code
 * lockService.lock("order:submit:" + orderId, () -> orderService.submit(orderId));
 * Order order = lockService.lockWithResult("order:" + orderId,
 *         LockOptions.waitFor(3, TimeUnit.SECONDS), () -> orderService.load(orderId));
 * boolean executed = lockService.tryLock("job:daily", () -> runJob());
 * Order cached = lockService.tryLockWithResult("order:" + orderId,
 *         () -> refreshOrder(orderId), () -> loadCachedOrder(orderId));
 * }</pre>
 *
 * <p>等待、租期和锁类型按需组合：</p>
 * <pre>{@code
 * // 一直等待，线程中断时终止等待；默认自动续期。
 * lockService.lock("job:daily", LockOptions.blocking(), () -> runJob());
 * // 最多等待 5 秒，获取成功后持有 30 秒，固定租期不会自动续期。
 * lockService.lock("job:short", LockOptions.fixedLease(5, 30, TimeUnit.SECONDS), () -> runShortJob());
 * // 公平锁按请求顺序排队。
 * lockService.lock("queue:process", LockOptions.blocking().withLockType(LockType.FAIR), () -> process());
 * // 读写锁使用同一个键；多个读者可并发，写者独占。
 * String config = lockService.lockWithResult("config:site",
 *         LockOptions.waitFor(3, TimeUnit.SECONDS).withLockType(LockType.READ), () -> readConfig());
 * lockService.lock("config:site", LockOptions.waitFor(3, TimeUnit.SECONDS).withLockType(LockType.WRITE),
 *         () -> updateConfig());
 * // 业务结果允许为 null，必须用 acquired 判断是否执行成功。
 * LockResult<Order> result = lockService.tryLockWithResult("order:" + orderId, () -> findOrder(orderId));
 * if (result.acquired()) {
 *     useOrder(result.value());
 * }
 * // 未获锁时也可抛出自己的业务异常；此回调在锁外执行。
 * lockService.tryLockWithResult("order:" + orderId, () -> submitOrder(orderId),
 *         () -> { throw new IllegalStateException("订单正在处理中"); });
 * }</pre>
 *
 * <p>锁键不能为空或全空白，回调和策略不能为空；参数错误在调用 Redis 前抛出。
 * {@link DistributedLockException#getReason()} 可区分竞争失败、中断、获取失败、丢锁和释放失败，
 * {@link DistributedLockException#getLockKey()} 提供锁键。中断后会保留线程中断标记。
 * 业务异常原样抛出；业务与释放同时失败时，释放异常附加在业务异常的 suppressed 列表中。</p>
 *
 * <p>回调必须在当前线程同步完成，不应只启动异步任务后立即返回。需要保护数据库提交时，
 * 应在回调内调用事务服务或使用 TransactionTemplate，使事务在回调返回前提交。</p>
 * <p>同一业务锁的键和锁类型必须保持一致；读锁与写锁使用相同的键。锁可以重入，每次调用
 * 仅释放自己获取的一次持有。读锁不可直接升级为写锁。</p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class LockService {

    private static final LockOptions DEFAULT_OPTIONS = LockOptions.noWait();

    private final RedissonClient redissonClient;

    /**
     * 立即尝试获取锁并执行无返回值业务，未获锁时抛出竞争异常。
     *
     * @param lockKey 分布式锁键，建议包含业务名称和资源 ID
     * @param logic 在当前线程同步执行的业务
     * @throws DistributedLockException 锁竞争失败、中断或锁操作故障
     */
    public void lock(String lockKey, Runnable logic) {
        lock(lockKey, DEFAULT_OPTIONS, logic);
    }

    /**
     * 按指定策略执行无返回值业务，未获锁时抛出竞争异常。
     *
     * @param lockKey 分布式锁键
     * @param options 等待时间、租期和锁类型策略
     * @param logic 在当前线程同步执行的业务
     * @throws DistributedLockException 锁竞争失败、中断或锁操作故障
     */
    public void lock(String lockKey, LockOptions options, Runnable logic) {
        lockWithResult(lockKey, options, asSupplier(logic));
    }

    /**
     * 立即尝试获取锁并返回业务结果，结果允许为 null，未获锁时抛出竞争异常。
     *
     * @param lockKey 分布式锁键
     * @param function 在当前线程同步执行并返回结果的业务
     * @param <T> 业务结果类型
     * @return 业务结果，允许为 null
     * @throws DistributedLockException 锁竞争失败、中断或锁操作故障
     */
    public <T> T lockWithResult(String lockKey, Supplier<T> function) {
        return lockWithResult(lockKey, DEFAULT_OPTIONS, function);
    }

    /**
     * 按指定策略执行业务并返回结果，未获锁时抛出竞争异常。
     * 业务抛出的异常原样传递，释放失败时将释放异常附加为 suppressed exception。
     *
     * @param lockKey 分布式锁键
     * @param options 等待时间、租期和锁类型策略
     * @param function 在当前线程同步执行并返回结果的业务
     * @param <T> 业务结果类型
     * @return 业务结果，允许为 null
     * @throws DistributedLockException 锁竞争失败、中断或锁操作故障
     */
    public <T> T lockWithResult(String lockKey, LockOptions options, Supplier<T> function) {
        LockResult<T> result = execute(lockKey, options, function);
        if (!result.acquired()) {
            throw new DistributedLockException(lockKey, Reason.CONTENTION);
        }
        return result.value();
    }

    /**
     * 立即尝试执行无返回值业务，未获锁时返回 false，且不执行业务。
     *
     * @param lockKey 分布式锁键
     * @param logic 在当前线程同步执行的业务
     * @return true 表示业务及锁释放均成功；false 表示未获锁
     * @throws DistributedLockException 中断或锁操作故障
     */
    public boolean tryLock(String lockKey, Runnable logic) {
        return tryLock(lockKey, DEFAULT_OPTIONS, logic);
    }

    /**
     * 按指定策略尝试执行无返回值业务，未获锁时返回 false。
     *
     * @param lockKey 分布式锁键
     * @param options 等待时间、租期和锁类型策略
     * @param logic 在当前线程同步执行的业务
     * @return true 表示业务及锁释放均成功；false 表示未获锁
     * @throws DistributedLockException 中断或锁操作故障
     */
    public boolean tryLock(String lockKey, LockOptions options, Runnable logic) {
        return execute(lockKey, options, asSupplier(logic)).acquired();
    }

    /**
     * 立即尝试执行有返回值业务，通过 acquired 区分未获锁与业务成功返回 null。
     *
     * @param lockKey 分布式锁键
     * @param function 在当前线程同步执行并返回结果的业务
     * @param <T> 业务结果类型
     * @return 执行状态与业务结果，未获锁时为 (false, null)
     * @throws DistributedLockException 中断或锁操作故障
     */
    public <T> LockResult<T> tryLockWithResult(String lockKey, Supplier<T> function) {
        return tryLockWithResult(lockKey, DEFAULT_OPTIONS, function);
    }

    /**
     * 按指定策略尝试执行有返回值业务，未获锁时返回 {@code (false, null)}。
     *
     * @param lockKey 分布式锁键
     * @param options 等待时间、租期和锁类型策略
     * @param function 在当前线程同步执行并返回结果的业务
     * @param <T> 业务结果类型
     * @return 执行状态与业务结果，必须用 acquired 判断是否执行
     * @throws DistributedLockException 中断或锁操作故障
     */
    public <T> LockResult<T> tryLockWithResult(String lockKey, LockOptions options, Supplier<T> function) {
        return execute(lockKey, options, function);
    }

    /**
     * 立即尝试执行有返回值业务，仅在未获锁时执行 onLockUnavailable。
     * 可在该回调中返回降级结果或抛出业务异常。
     *
     * @param lockKey 分布式锁键
     * @param function 在当前线程同步执行并返回结果的业务
     * @param onLockUnavailable 竞争失败后的锁外处理，不处理 Redis、中断或业务异常
     * @param <T> 业务结果类型
     * @return 锁内业务结果或未获锁处理结果，均允许为 null
     * @throws DistributedLockException 中断或锁操作故障
     */
    public <T> T tryLockWithResult(String lockKey, Supplier<T> function, Supplier<T> onLockUnavailable) {
        return tryLockWithResult(lockKey, DEFAULT_OPTIONS, function, onLockUnavailable);
    }

    /**
     * 按指定策略尝试执行业务，仅在锁竞争失败时执行 onLockUnavailable。
     * 未获锁处理不受该锁保护；业务、Redis、中断和释放异常不会触发该处理。
     *
     * @param lockKey 分布式锁键
     * @param options 等待时间、租期和锁类型策略
     * @param function 在当前线程同步执行并返回结果的业务
     * @param onLockUnavailable 竞争失败后的锁外处理，可返回结果或抛出业务异常
     * @param <T> 业务结果类型
     * @return 锁内业务结果或未获锁处理结果，均允许为 null
     * @throws DistributedLockException 中断或锁操作故障
     */
    public <T> T tryLockWithResult(String lockKey, LockOptions options, Supplier<T> function,
                                  Supplier<T> onLockUnavailable) {
        Objects.requireNonNull(onLockUnavailable, "onLockUnavailable must not be null");
        LockResult<T> result = execute(lockKey, options, function);
        return result.acquired() ? result.value() : onLockUnavailable.get();
    }

    private <T> LockResult<T> execute(String lockKey, LockOptions options, Supplier<T> function) {
        Objects.requireNonNull(lockKey, "lockKey must not be null");
        if (lockKey.isBlank()) {
            throw new IllegalArgumentException("lockKey must not be blank");
        }
        Objects.requireNonNull(options, "options must not be null");
        Objects.requireNonNull(function, "function must not be null");

        RLock lock;
        try {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Thread interrupted before acquiring the lock");
            }
            lock = switch (options.lockType()) {
                case REENTRANT -> redissonClient.getLock(lockKey);
                case FAIR -> redissonClient.getFairLock(lockKey);
                case READ -> redissonClient.getReadWriteLock(lockKey).readLock();
                case WRITE -> redissonClient.getReadWriteLock(lockKey).writeLock();
            };
            if (!acquire(lock, options)) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Thread interrupted while acquiring the lock");
                }
                return new LockResult<>(false, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DistributedLockException(lockKey, Reason.INTERRUPTED, e);
        } catch (RuntimeException e) {
            throw new DistributedLockException(lockKey, Reason.ACQUIRE_FAILED, e);
        }

        Throwable businessFailure = null;
        try {
            // Redis 初次获取可能不可中断，成功后再检查，已取消的业务仍需释放刚获取的锁。
            if (Thread.currentThread().isInterrupted()) {
                throw new DistributedLockException(lockKey, Reason.INTERRUPTED,
                        new InterruptedException("Thread interrupted while acquiring the lock"));
            }
            return new LockResult<>(true, function.get());
        } catch (RuntimeException | Error e) {
            businessFailure = e;
            throw e;
        } finally {
            try {
                release(lockKey, lock);
            } catch (RuntimeException | Error releaseFailure) {
                if (businessFailure == null) {
                    throw releaseFailure;
                }
                if (businessFailure != releaseFailure) {
                    businessFailure.addSuppressed(releaseFailure);
                }
            }
        }
    }

    private boolean acquire(RLock lock, LockOptions options) throws InterruptedException {
        if (options.waitTime() == -1) {
            if (options.leaseTime() == -1) {
                lock.lockInterruptibly();
            } else {
                lock.lockInterruptibly(options.leaseTime(), options.timeUnit());
            }
            return true;
        }
        if (options.leaseTime() == -1) {
            return lock.tryLock(options.waitTime(), options.timeUnit());
        }
        return lock.tryLock(options.waitTime(), options.leaseTime(), options.timeUnit());
    }

    private void release(String lockKey, RLock lock) {
        try {
            // unlock 原子校验当前线程的所有权，避免 isHeldByCurrentThread 与 unlock 间的竞态。
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
            throw new DistributedLockException(lockKey, Reason.LOCK_LOST, e);
        } catch (RuntimeException e) {
            throw new DistributedLockException(lockKey, Reason.RELEASE_FAILED, e);
        }
    }

    private Supplier<Void> asSupplier(Runnable logic) {
        Objects.requireNonNull(logic, "logic must not be null");
        return () -> {
            logic.run();
            return null;
        };
    }

    /**
     * 锁类型。READ 与 WRITE 来自同一个 Redisson 读写锁。
     */
    public enum LockType {
        /** 普通可重入互斥锁，默认类型。 */
        REENTRANT,
        /** 按请求顺序排队的可重入互斥锁。 */
        FAIR,
        /** 共享读锁，可与同一个键的其他读锁并发。 */
        READ,
        /** 独占写锁，与同一个键的读锁和写锁互斥。 */
        WRITE
    }

    /**
     * 锁执行策略，等待时间和租期使用同一个 timeUnit。
     *
     * <p>固定租期不会自动续期，租期届满后业务仍可能继续执行，必须确保业务在租期内完成。
     * 释放时发现锁已失去所有权会抛出 LOCK_LOST，但无法撤销已经执行的业务。
     * 同一个锁重入时应保持租期策略一致，避免改变外层调用的续期行为。</p>
     * <p>Redisson 使用毫秒精度，等待时间的小数毫秒部分会被截断；小于 1 毫秒的正等待时间
     * 会变成立即尝试。固定租期不足 1 毫秒则直接拒绝。</p>
     *
     * @param waitTime 最长等待时间，0 表示立即尝试，-1 表示无限等待（可中断）
     * @param leaseTime 固定租期，-1 表示看门狗自动续期，正值必须至少为 1 毫秒
     * @param timeUnit 等待时间和租期的单位
     * @param lockType 锁类型
     */
    public record LockOptions(long waitTime, long leaseTime, TimeUnit timeUnit, LockType lockType) {

        /**
         * 校验策略。通常使用 noWait、waitFor、blocking 或 fixedLease 工厂方法创建。
         *
         * @param waitTime 最长等待时间，-1 为无限等待，0 为立即尝试
         * @param leaseTime 租期，-1 为看门狗续期，正值至少为 1 毫秒
         * @param timeUnit 等待时间和租期的单位
         * @param lockType 锁类型
         * @throws IllegalArgumentException 等待时间或租期无效
         */
        public LockOptions {
            Objects.requireNonNull(timeUnit, "timeUnit must not be null");
            Objects.requireNonNull(lockType, "lockType must not be null");
            if (waitTime < -1) {
                throw new IllegalArgumentException("waitTime must be -1 or non-negative");
            }
            if (leaseTime != -1 && (leaseTime <= 0 || timeUnit.toMillis(leaseTime) == 0)) {
                throw new IllegalArgumentException("leaseTime must be -1 or at least 1 millisecond");
            }
        }

        /**
         * 立即尝试，使用看门狗续期。
         *
         * @return 普通可重入锁策略
         */
        public static LockOptions noWait() {
            return new LockOptions(0, -1, TimeUnit.MILLISECONDS, LockType.REENTRANT);
        }

        /**
         * 限时等待，使用看门狗续期。
         *
         * @param waitTime 非负等待时间，0 表示立即尝试
         * @param timeUnit 等待时间单位
         * @return 普通可重入锁策略
         */
        public static LockOptions waitFor(long waitTime, TimeUnit timeUnit) {
            if (waitTime < 0) {
                throw new IllegalArgumentException("waitTime must be non-negative");
            }
            return new LockOptions(waitTime, -1, timeUnit, LockType.REENTRANT);
        }

        /**
         * 无限等待（可中断），使用看门狗续期。
         *
         * @return 普通可重入锁策略
         */
        public static LockOptions blocking() {
            return new LockOptions(-1, -1, TimeUnit.MILLISECONDS, LockType.REENTRANT);
        }

        /**
         * 指定等待时间和固定租期。waitTime 可为 -1；leaseTime 必须为正，且至少为 1 毫秒。
         *
         * @param waitTime 最长等待时间，-1 为无限等待，0 为立即尝试
         * @param leaseTime 获取成功后开始计算的租期，不自动续期
         * @param timeUnit 等待时间与租期共用的单位
         * @return 普通可重入锁策略
         */
        public static LockOptions fixedLease(long waitTime, long leaseTime, TimeUnit timeUnit) {
            if (leaseTime <= 0) {
                throw new IllegalArgumentException("fixed leaseTime must be positive");
            }
            return new LockOptions(waitTime, leaseTime, timeUnit, LockType.REENTRANT);
        }

        /**
         * 使用相同的等待和租期策略，选择公平锁或读写锁。
         *
         * @param lockType 锁类型
         * @return 新策略，原策略保持不变
         */
        public LockOptions withLockType(LockType lockType) {
            return new LockOptions(waitTime, leaseTime, timeUnit, lockType);
        }
    }

    /**
     * 尝试执行的结果。acquired 为 false 时未执行业务；为 true 时 value 仍允许为 null。
     * 只有业务和锁释放均正常完成，才返回成功结果。
     *
     * @param acquired 是否成功获取锁并完成业务及释放
     * @param value 业务结果，允许为 null
     * @param <T> 业务结果类型
     */
    public record LockResult<T>(boolean acquired, T value) {
    }
}
