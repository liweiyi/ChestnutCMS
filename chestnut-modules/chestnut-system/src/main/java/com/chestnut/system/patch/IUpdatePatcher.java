package com.chestnut.system.patch;

/**
 * 补丁程序
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IUpdatePatcher {

    /**
     * 补丁唯一标识，使用三段非负整数版本号，例如 1.6.1。
     * 每段不含前导零且不超过 Integer.MAX_VALUE；不同补丁不得使用相同版本号。
     */
    String getVersion();

    /**
     * 同步完成补丁，失败必须抛出异常，不得异步执行或吞掉失败。
     * 事务由实现自行管理。业务提交与成功记录写入并非同一事务，
     * 因而即使业务已提交，记录保存失败或进程退出后仍会重试；实现必须支持幂等执行。
     * 执行时普通单例已初始化，但 Web 服务和 Runner 尚未启动，不能依赖它们的启动结果。
     */
    void update();
}
