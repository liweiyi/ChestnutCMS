/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.comment.controller.front;

import com.chestnut.comment.domain.vo.CommentVO;
import com.chestnut.comment.service.ICommentApiService;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberCommentApiController extends BaseRestController {

    private final ICommentApiService commentApiService;

    @GetMapping("/api/comment/user/{uid}")
    public R<?> getMemberCommentList(@PathVariable Long uid,
                                     @RequestParam String type,
                                     @RequestParam(required = false, defaultValue = "10") @Min(1) Integer limit,
                                     @RequestParam(required = false, defaultValue = "0") @Min(0) Long offset) {
        List<CommentVO> comments = commentApiService.getCommentListByMember(type, uid, limit, offset, false);
        return R.ok(comments);
    }
}
