package com.planup.planup.domain.goal.repository;


import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    void deleteByGoalId(Long goalId);

    //query Method
    List<Comment> findByGoalIdAndStatusOrderByCreatedAtAsc(Long goalId, CommentStatus commentStatus);
}
