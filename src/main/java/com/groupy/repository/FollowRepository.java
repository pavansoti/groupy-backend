package com.groupy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.groupy.entity.Follow;
import com.groupy.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    Boolean existsByFollowerAndFollowing(User follower, User following);
    
    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId")
    List<User> findFollowingByUserId(@Param("userId") Long userId);
    
    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId")
    List<User> findFollowersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    Long countFollowingByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    Long countFollowersByUserId(@Param("userId") Long userId);
    
    void deleteByFollowerAndFollowing(User follower, User following);
}