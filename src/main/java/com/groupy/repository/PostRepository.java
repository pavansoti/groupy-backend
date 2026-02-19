package com.groupy.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.groupy.entity.Post;
import com.groupy.entity.User;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Efficiently fetch posts from users that the given user follows
//    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user IN " +
//            "(SELECT f.following FROM Follow f WHERE f.follower = :user) " +
//            "ORDER BY p.createdAt DESC")
//    List<Post> findFeedByUser(@Param("user") User user);
    
	// get feeds of self and other following users
//	@Query("""
//        SELECT p FROM Post p
//        WHERE p.user = :user
//           OR p.user IN (
//                SELECT f.following FROM Follow f
//                WHERE f.follower = :user
//           )
//        ORDER BY p.createdAt DESC
//    """)
//    List<Post> findFeedByUser(@Param("user") User user);
	
	@Query("""
		    SELECT DISTINCT p FROM Post p
			LEFT JOIN Follow f ON f.following = p.user
			LEFT JOIN p.likes l
			WHERE 
			    (
			        p.user = :user 
			        OR f.follower = :user 
			        OR COALESCE(p.user.privateAccount, false) = false
			    )
			    AND (
			        :onlyLiked = false 
			        OR (l.user = :user AND p.user <> :user)
			    )
		""")
	Slice<Post> findFeedByUserOptimized(
	        @Param("user") User user,
	        @Param("onlyLiked") boolean onlyLiked,
	        Pageable pageable
	);

   @Query("""
        SELECT p
        FROM Post p
        WHERE p.user.id = :userId
        AND p.imageUrl IS NOT NULL
        AND p.imageUrl <> ''
    """)
   Slice<Post> findPostsWithImageOnlyByUser(@Param("userId") Long userId, Pageable pageable);
   
   @Query("""
	    SELECT DISTINCT p
	    FROM Post p
	    JOIN p.likes l
	    WHERE l.user.id = :userId
	      AND p.user.id <> :userId
	      AND p.imageUrl IS NOT NULL
	      AND p.imageUrl <> ''
	""")
   	Slice<Post> findLikedPostsWithImageOnlyByOtherUsers(@Param("userId") Long userId, Pageable pageable);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}
