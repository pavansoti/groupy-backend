package com.groupy.service;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.groupy.dto.CommentDto;
import com.groupy.dto.PaginationResponse;
import com.groupy.entity.Comment;
import com.groupy.entity.Post;
import com.groupy.entity.User;
import com.groupy.repository.CommentRepository;
import com.groupy.repository.PostRepository;
import com.groupy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentDto createComment(Long postId, Long userId, String message) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .message(message)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return mapToDto(savedComment);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
    
    public PaginationResponse getComments(Long postId, int page, int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        Page<Comment> commentPage = commentRepository
                .findByPostIdOrderByCreatedAtDesc(postId, pageable);

        List<CommentDto> commentDtos = commentPage.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        return new PaginationResponse(
                commentDtos,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.hasNext(),
                commentPage.getTotalElements()
        );
    }
    
    public CommentDto updateComment(Long commentId, String content, Principal principal) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Get logged-in user
        String username = principal.getName();

        // Security check (only owner can edit)
        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only edit your own comment");
        }

        comment.setMessage(content);

        Comment updatedComment = commentRepository.save(comment);

        return mapToDto(updatedComment);
    }
    
    private CommentDto mapToDto(Comment comment) {

        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .imageUrl(comment.getUser().getImageUrl())
                .message(comment.getMessage())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}