package com.planup.planup.domain.goal.repository;


import com.planup.planup.domain.goal.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    void deleteByGoalId(Long goalId);
}
