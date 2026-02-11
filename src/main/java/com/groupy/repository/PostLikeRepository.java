package com.groupy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.groupy.entity.Post;
import com.groupy.entity.PostLike;
import com.groupy.entity.User;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUserAndPost(User user, Post post);

    Optional<PostLike> findByUserAndPost(User user, Post post);

    void deleteByUserAndPost(User user, Post post);
}
