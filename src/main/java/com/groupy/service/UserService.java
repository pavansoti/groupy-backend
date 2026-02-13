package com.groupy.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.groupy.dto.ChangePasswordRequest;
import com.groupy.dto.UserDto;
import com.groupy.dto.UserRequestDto;
import com.groupy.dto.UserResponseDto;
import com.groupy.dto.UserSearchDto;
import com.groupy.entity.User;
import com.groupy.exception.ResourceNotFoundException;
import com.groupy.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

//    private final Path fileStorageLocation =
//            Paths.get("uploads").toAbsolutePath().normalize();

    // Ensure uploads folder exists
//    @PostConstruct
//    public void init() {
//        try {
//            Files.createDirectories(fileStorageLocation);
//        } catch (IOException e) {
//            throw new RuntimeException("Could not create upload directory", e);
//        }
//    }

    public User createUser(User user) {
        log.info("Creating user with username: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findByIdWithPosts(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    public void changePassword(String username, ChangePasswordRequest request) {

        // Find user
        User user = getUserByUsername(username);

        // Check current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Check new password match
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        // Prevent reusing old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be same as current password");
        }

        // 5Encode and save
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponseDto getUserResponseById(Long id) {

        User authenticatedUser = getAuthenticatedUser();

        User user = getUserById(id);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(user.getId());
        responseDto.setUsername(user.getUsername());
        responseDto.setEmail(user.getEmail());
        responseDto.setBio(user.getBio());
        responseDto.setCreatedAt(user.getCreatedAt().toString());
        responseDto.setImageUrl(user.getImageUrl());
        responseDto.setGender(user.getGender());
        responseDto.setPrivateAccount(user.getPrivateAccount());

        responseDto.setPostCount(user.getPosts() != null ? user.getPosts().stream().filter(post -> post.getImageUrl() != null).toList().size() : 0);
        responseDto.setFollowerCount(user.getFollowers() != null ? user.getFollowers().size() : 0);
        responseDto.setFollowingCount(user.getFollowing() != null ? user.getFollowing().size() : 0);

         // Set setFollowing based on following count and requested user id
        responseDto.setFollowing(user.getFollowers() != null 
            && user.getFollowers().size() > 0 
            && user.getFollowers()
                .stream()
                .anyMatch(follow -> follow.getFollower().getId().equals(authenticatedUser.getId())));

        return responseDto;
    }

    // public UserResponseDto getUserWithFollowStats(Long id) {
    //     User user = getUserById(id);

    //     UserResponseDto responseDto = new UserResponseDto();
    //     responseDto.setId(user.getId());
    //     responseDto.setUsername(user.getUsername());
    //     responseDto.setEmail(user.getEmail());
    //     responseDto.setBio(user.getBio());
    //     responseDto.setCreatedAt(user.getCreatedAt().toString());

    //     return responseDto;
    // }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, UserRequestDto userDetails) {
        User user = getUserById(id);
        
     // Check username availability (only if changed)
        if (!user.getUsername().equals(userDetails.getUsername())) {
            if (existsByUsername(userDetails.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(userDetails.getUsername());
        }

        // Check email availability (only if changed)
        if (!user.getEmail().equals(userDetails.getEmail())) {
            if (existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(userDetails.getEmail());
        }

        // Update optional fields
        user.setBio(userDetails.getBio());
        user.setGender(userDetails.getGender());
        user.setPrivateAccount(userDetails.getPrivateAccount());
        

        log.info("Updating user with id: {}", id);

        User updatedUser = userRepository.save(user);

        updatedUser.setFollowers(null);
        updatedUser.setFollowing(null);
        updatedUser.setPosts(null);

        return updatedUser;
    }
    
    public User updateUserBio(Long id, UserRequestDto userDetails) {
        User user = getUserById(id);

        user.setBio(userDetails.getBio());

        log.info("Updating user with id: {}", id);

        User updatedUser = userRepository.save(user);

        updatedUser.setFollowers(null);
        updatedUser.setFollowing(null);
        updatedUser.setPosts(null);

        return updatedUser;
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);

        // delete profile pic if exists
        deleteFile(user.getImagePublicId());

        userRepository.delete(user);
        log.info("Deleted user with id: {}", id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserSearchDto> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<User> users = userRepository.searchByUsernameOrEmail(query.trim());

        return users.stream()
                .limit(20)
                .map(this::convertToUserSearchDto)
                .collect(Collectors.toList());
    }

    private UserSearchDto convertToUserSearchDto(User user) {

        boolean isFollowing = user.getFollowers() != null ? user.getFollowers().
                stream().
                anyMatch(follow -> follow.getFollower().getId().equals(getAuthenticatedUser().getId())) : false;

        return new UserSearchDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getImageUrl(),
                user.getFollowers() != null ? user.getFollowers().size() : 0,
                isFollowing
        );
    }

    // ================= PROFILE PIC UPDATE =================

    @Transactional
    public User updateProfilePic(Long id, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }
        
        String imageUrl = null;
        String publicId = null;
        
        if (file != null && !file.isEmpty()) {
        	        	
        	Map uploadResult = cloudinaryService.uploadImage(file);
            imageUrl = uploadResult.get("secure_url").toString();
            publicId = uploadResult.get("public_id").toString();
        }

        User user = getUserById(id);

        // delete old pic if exists
        deleteFile(user.getImagePublicId());

        user.setImageUrl(imageUrl);
        user.setImagePublicId(publicId);
        user.setFollowers(null);
        user.setFollowing(null);
        user.setPosts(null);

        return userRepository.save(user);
    }

    // ================= FILE STORAGE =================

//    private String storeFile(MultipartFile file) {
//
//        try {
//            String originalFileName = file.getOriginalFilename();
//            String extension = "";
//
//            if (originalFileName != null && originalFileName.contains(".")) {
//                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//            }
//
//            String fileName = UUID.randomUUID() + extension;
//
//            Path targetLocation = fileStorageLocation.resolve(fileName).normalize();
//
//            Files.copy(file.getInputStream(), targetLocation,
//                    StandardCopyOption.REPLACE_EXISTING);
//
//            return fileName;
//
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to store file", e);
//        }
//    }

    private void deleteFile(String imagePublicId) {

        if (imagePublicId == null || imagePublicId.isBlank()) {
            return;
        }
        
        cloudinaryService.deleteImage(imagePublicId);

//        try {
//            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//
//            Path filePath = fileStorageLocation
//                    .resolve(fileName)
//                    .normalize();
//
//            Files.deleteIfExists(filePath);
//
//        } catch (IOException e) {
//            log.warn("Failed to delete file: {}", fileUrl);
//        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
