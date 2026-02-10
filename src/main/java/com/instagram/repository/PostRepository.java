package com.instagram.repository;

import com.instagram.entity.Post;
import com.instagram.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Efficiently fetch posts from users that the given user follows
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user IN " +
            "(SELECT f.following FROM Follow f WHERE f.follower = :user) " +
            "ORDER BY p.createdAt DESC")
    List<Post> findFeedByUser(@Param("user") User user);

   @Query("""
        SELECT p
        FROM Post p
        WHERE p.user.id = :userId
        AND p.imageUrl IS NOT NULL
        AND p.imageUrl <> ''
        ORDER BY p.createdAt DESC
    """)
    List<Post> findPostsWithImageOnlyByUser(@Param("userId") Long userId);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}
