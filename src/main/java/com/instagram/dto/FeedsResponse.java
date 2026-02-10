package com.instagram.dto;

import lombok.Data;

@Data
public class FeedsResponse {
	Long id;
	Long authorId;
	String authorUsername;
	String authorProfilePicture;
	String imageUrl;
	String caption;
	Integer likeCount;
	Integer commentCount;
	String createdAt;
	boolean likedByCurrentUser;
}
