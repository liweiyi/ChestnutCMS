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
package com.chestnut.system.domain.vo.server;

import com.chestnut.common.utils.Arith;
import lombok.Getter;
import lombok.Setter;

/**
 * 內存相关信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class Mem {

    /**
     * 内存总量
     */
    private double total;

    /**
     * 已用内存
     */
    private double used;

    /**
     * 剩余内存
     */
    private double free;

    public double getTotal()
    {
        return Arith.div(total, (1024 * 1024 * 1024), 2);
    }

    public double getUsed()
    {
        return Arith.div(used, (1024 * 1024 * 1024), 2);
    }

    public double getFree()
    {
        return Arith.div(free, (1024 * 1024 * 1024), 2);
    }

    public double getUsage()
    {
        return Arith.mul(Arith.div(used, total, 4), 100);
    }
}
