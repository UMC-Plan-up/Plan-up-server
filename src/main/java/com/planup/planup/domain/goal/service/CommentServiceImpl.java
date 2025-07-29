package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    @Override
    public List<Comment> getComments(Long goalId) {
        return List.of();
    }
}
