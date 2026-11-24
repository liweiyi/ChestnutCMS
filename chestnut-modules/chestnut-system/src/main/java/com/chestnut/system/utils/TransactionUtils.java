package com.chestnut.system.utils;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

/**
 * 事务工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class TransactionUtils {

    public record TransactionTemplateOptions(int definition, boolean readOnly) {
    }

    public static <T> T exec(TransactionTemplate transactionTemplate, TransactionCallback<T> callback, TransactionTemplateOptions options) {
        assert transactionTemplate.getTransactionManager() != null;
        TransactionTemplate template =
                new TransactionTemplate(transactionTemplate.getTransactionManager());
        template.setPropagationBehavior(options.definition);
        template.setReadOnly(options.readOnly);
        return template.execute(callback);
    }

    public static void execWithoutResult(TransactionTemplate transactionTemplate, Consumer<TransactionStatus> consumer, TransactionTemplateOptions options) {
        assert transactionTemplate.getTransactionManager() != null;
        TransactionTemplate template =
                new TransactionTemplate(transactionTemplate.getTransactionManager());
        template.setPropagationBehavior(options.definition);
        template.setReadOnly(options.readOnly);
        template.executeWithoutResult(consumer);
    }

    /**
     * 独立读写事务，隔离调用方的事务快照及MyBatis一级缓存
     *
     * @param transactionTemplate 事务模版
     * @param callback 事务回调
     * @return callback回调返回值
     * @param <T> T
     */
    public static <T> T execIndependent(TransactionTemplate transactionTemplate, TransactionCallback<T> callback) {
        TransactionTemplateOptions options = new TransactionTemplateOptions(TransactionDefinition.PROPAGATION_REQUIRES_NEW, false);
        return exec(transactionTemplate, callback, options);
    }

    /**
     * 独立读写事务，隔离调用方的事务快照及MyBatis一级缓存
     */
    public static void execIndependentWithoutResult(TransactionTemplate transactionTemplate, Consumer<TransactionStatus> consumer) {
        TransactionTemplateOptions options = new TransactionTemplateOptions(TransactionDefinition.PROPAGATION_REQUIRES_NEW, false);
        execWithoutResult(transactionTemplate, consumer, options);
    }

    /**
     * 独立只读事务，隔离调用方的事务快照及MyBatis一级缓存
     *
     * @param transactionTemplate 事务模版
     * @param callback 事务回调
     * @return callback回调返回值
     * @param <T> T
     */
    public static <T> T execIndependentForReadOnly(TransactionTemplate transactionTemplate, TransactionCallback<T> callback) {
        TransactionTemplateOptions options = new TransactionTemplateOptions(TransactionDefinition.PROPAGATION_REQUIRES_NEW, true);
        return exec(transactionTemplate, callback, options);
    }

    /**
     * 独立只读事务，隔离调用方的事务快照及MyBatis一级缓存
     */
    public static void execIndependentWithoutResultForReadOnly(TransactionTemplate transactionTemplate, Consumer<TransactionStatus> consumer) {
        TransactionTemplateOptions options = new TransactionTemplateOptions(TransactionDefinition.PROPAGATION_REQUIRES_NEW, true);
        execWithoutResult(transactionTemplate, consumer, options);
    }

    /**
     * 事务提交后处理
     *
     * @param runnable 处理逻辑
     */
    public static void afterCommit(Runnable runnable) {
        // 事务提交后执行
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            }
        );
    }

    /**
     * 事务完成后处理，成功或失败均执行
     *
     * @param consumer 事务状态消费者
     */
    public static void afterCompletion(Consumer<Integer> consumer) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        consumer.accept(status);
                    }
                }
        );
    }
}
