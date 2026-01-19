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
package com.chestnut.media.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.media.domain.BCmsAudio;
import com.chestnut.media.mapper.BCmsAudioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * BCmsAudioDAO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class BCmsAudioDAO extends ServiceImpl<BCmsAudioMapper, BCmsAudio> {

}
