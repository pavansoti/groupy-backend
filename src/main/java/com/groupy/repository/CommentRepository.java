package com.groupy.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.groupy.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostId(Long postId);
    
    Page<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

}