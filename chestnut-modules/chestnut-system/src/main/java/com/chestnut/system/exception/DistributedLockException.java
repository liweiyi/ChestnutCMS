package com.chestnut.system.exception;

import java.io.Serial;

/**
 * 分布式锁异常，区分正常锁竞争、线程中断和锁基础设施故障。
 * 业务回调的异常不会转换为此异常。
 */
public class DistributedLockException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 发生故障或竞争的锁键。 */
    private final String lockKey;

    /** 锁操作失败原因。 */
    private final Reason reason;

    /**
     * 创建无底层原因的锁异常，例如正常竞争失败。
     *
     * @param lockKey 锁键
     * @param reason 失败原因
     */
    public DistributedLockException(String lockKey, Reason reason) {
        this(lockKey, reason, null);
    }

    /**
     * 创建锁异常并保留底层原因。
     *
     * @param lockKey 锁键
     * @param reason 失败原因
     * @param cause 底层异常，允许为 null
     */
    public DistributedLockException(String lockKey, Reason reason, Throwable cause) {
        super("Distributed lock [" + lockKey + "]: " + reason, cause);
        this.lockKey = lockKey;
        this.reason = reason;
    }

    /**
     * 获取关联的业务锁键。
     *
     * @return 发生故障或竞争的锁键
     */
    public String getLockKey() {
        return lockKey;
    }

    /**
     * 获取锁操作的失败原因。
     *
     * @return 可用于区分正常竞争、取消和基础设施故障的失败原因
     */
    public Reason getReason() {
        return reason;
    }

    /** 锁操作失败原因，不包含业务回调自身的失败。 */
    public enum Reason {
        /** 未在等待时间内获取到锁。 */
        CONTENTION,
        /** 获取锁前或等待过程中线程被中断。 */
        INTERRUPTED,
        /** 创建或获取锁时发生 Redis 等基础设施故障。 */
        ACQUIRE_FAILED,
        /** 释放时已不再持有锁，例如固定租期已届满。 */
        LOCK_LOST,
        /** 释放锁时发生基础设施故障。 */
        RELEASE_FAILED
    }
}
