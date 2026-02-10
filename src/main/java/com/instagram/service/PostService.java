package com.instagram.service;

import com.instagram.dto.FeedsResponse;
import com.instagram.dto.PostResponseDto;
import com.instagram.dto.UserResponseDto;
import com.instagram.entity.Post;
import com.instagram.entity.PostLike;
import com.instagram.entity.User;
import com.instagram.repository.PostLikeRepository;
import com.instagram.repository.PostRepository;
import com.instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Directory for local file storage
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Transactional
    public PostResponseDto createPost(String username, String caption, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = storeFile(file);
        // Assuming the file is served from a static path, e.g., /uploads/
        String fileUrl = fileName != null ? "/uploads/" + fileName : null;

        Post post = Post.builder()
                .user(user)
                .caption(caption)
                .imageUrl(fileUrl)
                .build();

        Post savedPost = postRepository.save(post);

        return mapToDto(savedPost, user);
    }

    public void deletePost(Long postId, String username) {

        // Get logged-in user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Ownership check
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to delete this post");
        }

        // delete image from disk
        deleteImageFile(post.getImageUrl());

        // Delete post
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<FeedsResponse> getFeed(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Post> posts = postRepository.findFeedByUser(user);

        return posts.stream()
                .map(post -> mapToFeedResponse(post, user))
                .collect(Collectors.toList());
    }
    
    private FeedsResponse mapToFeedResponse(Post post, User currentUser) {

        FeedsResponse response = new FeedsResponse();

        List<PostLike> likes = post.getLikes() != null
                ? post.getLikes()
                : Collections.emptyList();

        boolean likedByCurrentUser = likes.stream()
                .anyMatch(like ->
                        like.getUser() != null &&
                        like.getUser().getId().equals(currentUser.getId())
                );

        response.setId(post.getId());
        response.setImageUrl(post.getImageUrl());
        response.setCaption(post.getCaption());
        response.setLikeCount(likes.size());
        response.setCommentCount(0); // better than null
        response.setCreatedAt(post.getCreatedAt().toString());

        User author = post.getUser();
        response.setAuthorId(author.getId());
        response.setAuthorUsername(author.getUsername());
        response.setAuthorProfilePicture(author.getProfilePicUrl());

        response.setLikedByCurrentUser(likedByCurrentUser);

        return response;
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getPostsByUser(Long userId, String currentUsername) {

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Post> posts = postRepository.findPostsWithImageOnlyByUser(user.getId());
        // System.out.println("posts: " + posts);
        return posts.stream()
            .map(post -> mapToDto(post, currentUser))
            .collect(Collectors.toList());
    }

    @Transactional
    public void likePost(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!postLikeRepository.existsByUserAndPost(user, post)) {
            PostLike like = PostLike.builder()
                    .user(user)
                    .post(post)
                    .build();
            postLikeRepository.save(like);

            // Send Notification (only if not liking own post)
            if (!post.getUser().getId().equals(user.getId())) {
                notificationService.sendNotification(post.getUser().getId(), com.instagram.dto.NotificationDto.builder()
                        .type("LIKE")
                        .message(user.getUsername() + " liked your post")
                        .fromUserId(user.getId())
                        .fromUsername(user.getUsername())
                        .targetId(post.getId())
                        .build());
            }
        }
    }

    @Transactional
    public void unlikePost(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postLikeRepository.deleteByUserAndPost(user, post);
    }

    private String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            Path filePath = Paths.get("uploads")
                    .resolve(Paths.get(imageUrl).getFileName().toString());

            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            // log warning, don't fail delete
            System.out.println("Failed to delete image file: " + e.getMessage());
        }
    }

    private PostResponseDto mapToDto(Post post, User user) {
        // UserResponseDto userDto = new UserResponseDto();
        // userDto.setId(post.getUser().getId());
        // userDto.setUsername(post.getUser().getUsername());
        // userDto.setEmail(post.getUser().getEmail());
        // userDto.setBio(post.getUser().getBio());
        // userDto.setCreatedAt(post.getUser().getCreatedAt().toString());

        boolean isLiked = post.getLikes() != null 
            && post.getLikes().stream().anyMatch(like -> like.getUser().getId().equals(user.getId()));
        long likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        // Note: post.getLikes().size() might initialize the lazy collection.
        // For strict optimization we might query count separately,
        // but for now this is standard and simpler.

        return PostResponseDto.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                // .user(userDto)
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLiked)
                .build();
    }
}
