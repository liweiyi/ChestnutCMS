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
package com.chestnut.common.security.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 列表分页参数配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut.security")
public class SecurityProperties {

    /**
     * Argon2配置
     */
    private ArgonConfig argon2 = new ArgonConfig();

    @Getter
    @Setter
    public static class ArgonConfig {
        /**
         * Argon2配置：时间成本
         * 也被称为迭代次数，决定了算法需要执行多少次迭代。增加这个值会增加计算所需的时间，从而提高安全性。
         * 你应该选择一个足够高的值，使得计算一个哈希至少需要耗费几百毫秒，但又不会导致用户体验不佳。
         */
        private int iterations = 1;
        /**
         * Argon2配置：内存成本
         * 定义了算法在处理过程中使用的内存量（以KiB为单位）。增大这个值可以有效抵抗基于RAM限制的攻击方法。
         * 通常建议从30-40 MiB开始，并根据你的服务器资源进行调整。
         * 确保选择的值不会导致内存交换（swap），因为这会显著降低性能。
         */
        private int memory = 65536;
        /**
         * Argon2配置：并行度
         * 表示可以在计算过程中并行运行的计算单元数量。这个参数允许你利用多核处理器的优势。
         * 对于大多数应用场景来说，设置为1到4之间的值是合理的。
         */
        private int parallelism = 2;
    }
}
