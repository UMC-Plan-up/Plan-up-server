package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getComments(Long goalId);
}
