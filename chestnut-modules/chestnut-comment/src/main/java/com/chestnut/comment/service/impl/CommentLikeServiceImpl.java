package com.chestnut.comment.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.comment.domain.CommentLike;
import com.chestnut.comment.mapper.CommentLikeMapper;
import com.chestnut.comment.service.ICommentLikeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements ICommentLikeService {
}