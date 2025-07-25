package com.hungtd.chatapp.configuration;

import com.hungtd.chatapp.entity.*;
import com.hungtd.chatapp.enums.FriendRequestStatus;
import com.hungtd.chatapp.enums.Role;
import com.hungtd.chatapp.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppInitConfig {

    PasswordEncoder passwordEncoder;
    ConversationRepository conversationRepository;
    ParticipantRepository participantRepository;
    MessageRepository messageRepository;
    FriendRepository friendRepository;
    FriendRequestRepository friendRequestRepository;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            // Create admin user if not exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                User user = User.builder()
                        .email("admin@gm.con")
                        .username("admin")
                        .password(passwordEncoder.encode("123"))
                        .firstName("admin")
                        .lastName("admin")
                        .roles(new HashSet<>(Collections.singletonList(Role.ADMIN.name())))
                        .build();

                userRepository.save(user);
                log.warn("default admin created with admin/123");
            }

            // Create 5 default users if no regular users exist
            if (userRepository.count() <= 1) { // Only admin exists or no users
                List<User> defaultUsers = List.of(
                        User.builder()
                                .email("hung@example.com")
                                .username("hung")
                                .password(passwordEncoder.encode("123"))
                                .firstName("Hùng")
                                .lastName("Trần")
                                .dob(LocalDate.of(1990, 1, 15))
                                .isActive(true)
                                .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                                .avtUrl("https://res.cloudinary.com/da1zrkrmi/image/upload/v1749956764/default-avatar_nhpbje.jpg")
                                .cloudinaryAvtId("default-avatar_nhpbje")
                                .build(),
                        User.builder()
                                .email("trang@example.com")
                                .username("trang")
                                .password(passwordEncoder.encode("123"))
                                .firstName("Trang")
                                .lastName("Trần")
                                .dob(LocalDate.of(1992, 5, 20))
                                .isActive(true)
                                .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                                .avtUrl("https://res.cloudinary.com/da1zrkrmi/image/upload/v1749956764/default-avatar_nhpbje.jpg")
                                .cloudinaryAvtId("default-avatar_nhpbje")
                                .build(),
                        User.builder()
                                .email("thuy@example.com")
                                .username("thuy")
                                .password(passwordEncoder.encode("123"))
                                .firstName("Thủy")
                                .lastName("Nguyễn")
                                .dob(LocalDate.of(1988, 9, 10))
                                .isActive(true)
                                .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                                .avtUrl("https://res.cloudinary.com/da1zrkrmi/image/upload/v1749956764/default-avatar_nhpbje.jpg")
                                .cloudinaryAvtId("default-avatar_nhpbje")
                                .build(),
                        User.builder()
                                .email("vinh@example.com")
                                .username("vinh")
                                .password(passwordEncoder.encode("123"))
                                .firstName("Vinh")
                                .lastName("Trần")
                                .dob(LocalDate.of(1995, 3, 25))
                                .isActive(true)
                                .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                                .avtUrl("https://res.cloudinary.com/da1zrkrmi/image/upload/v1749956764/default-avatar_nhpbje.jpg")
                                .cloudinaryAvtId("default-avatar_nhpbje")
                                .build(),
                        User.builder()
                                .email("nam@example.com")
                                .username("nam")
                                .password(passwordEncoder.encode("123"))
                                .firstName("Nam")
                                .lastName("Hoàng")
                                .dob(LocalDate.of(1991, 7, 8))
                                .isActive(true)
                                .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                                .avtUrl("https://res.cloudinary.com/da1zrkrmi/image/upload/v1749956764/default-avatar_nhpbje.jpg")
                                .cloudinaryAvtId("default-avatar_nhpbje")
                                .build()
                );

                userRepository.saveAll(defaultUsers);
                log.warn("Created 5 default users: hung, trang, thuy, vinh, nam (all with password '123')");
            }

            // After creating users, create additional data in the correct order
            // First create friendships, then create conversations only between friends
            createFriendships(userRepository);
            createConversationsForFriends(userRepository);
            createFriendRequests(userRepository);
        };
    }

    private void createFriendships(UserRepository userRepository) {
        if (friendRepository.count() > 0) {
            log.info("Friends data already exists, skipping creation");
            return;
        }

        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals("admin"))
                .toList();
                
        if (users.size() < 5) {
            log.warn("Not enough users to create friendships");
            return;
        }

        List<Friend> friends = new ArrayList<>();

        // Create friendship between users
        // Hung (0) is friends with everyone
        for (int i = 1; i < 5; i++) {
            Friend friendship = Friend.builder()
                    .userId1(users.get(0).getId())
                    .userId2(users.get(i).getId())
                    .build();
            friends.add(friendship);
        }

        // Trang (1) is friends with Thuy (2) and Vinh (3)
        Friend friendship1 = Friend.builder()
                .userId1(users.get(1).getId())
                .userId2(users.get(2).getId())
                .build();

        Friend friendship2 = Friend.builder()
                .userId1(users.get(1).getId())
                .userId2(users.get(3).getId())
                .build();

        // Thuy (2) is friends with Vinh (3)
        Friend friendship3 = Friend.builder()
                .userId1(users.get(2).getId())
                .userId2(users.get(3).getId())
                .build();

        friends.add(friendship1);
        friends.add(friendship2);
        friends.add(friendship3);

        friendRepository.saveAll(friends);
        log.info("Created {} friendships", friends.size());
    }

    private void createConversationsForFriends(UserRepository userRepository) {
        if (conversationRepository.count() > 0) {
            log.info("Conversation data already exists, skipping creation");
            return;
        }

        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals("admin"))
                .toList();

        if (users.size() < 5) {
            log.warn("Not enough users to create conversations");
            return;
        }

        // Get all friendships to determine who can chat with whom
        List<Friend> allFriendships = friendRepository.findAll();
        
        List<Conversation> conversations = new ArrayList<>();
        Map<Conversation, List<User>> conversationParticipants = new HashMap<>();

        // Create private conversations only between friends
        User hung = users.get(0); // Hung (index 0)
        
        // Create private conversations for Hung with friends
        for (int i = 1; i < 5; i++) {
            User otherUser = users.get(i);
            // Check if they are friends
            boolean areFriends = allFriendships.stream().anyMatch(f -> 
                (f.getUserId1().equals(hung.getId()) && f.getUserId2().equals(otherUser.getId())) || 
                (f.getUserId1().equals(otherUser.getId()) && f.getUserId2().equals(hung.getId()))
            );
            
            if (areFriends) {
                Conversation privateConv = Conversation.builder()
                        .title("")
                        .type(Conversation.Type.PRIVATE)
                        .build();

                conversations.add(privateConv);
                conversationParticipants.put(privateConv, List.of(hung, otherUser));
            }
        }

        // Create private conversations for other friends
        for (int i = 1; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                User user1 = users.get(i);
                User user2 = users.get(j);
                
                // Skip if either user is Hung (already handled above)
                if (user1.equals(hung) || user2.equals(hung)) {
                    continue;
                }
                
                // Check if they are friends
                boolean areFriends = allFriendships.stream().anyMatch(f -> 
                    (f.getUserId1().equals(user1.getId()) && f.getUserId2().equals(user2.getId())) || 
                    (f.getUserId1().equals(user2.getId()) && f.getUserId2().equals(user1.getId()))
                );
                
                if (areFriends) {
                    Conversation privateConv = Conversation.builder()
                            .title("")
                            .type(Conversation.Type.PRIVATE)
                            .build();

                    conversations.add(privateConv);
                    conversationParticipants.put(privateConv, List.of(user1, user2));
                }
            }
        }

        // Create a group conversation with Hung and his friends
        List<User> hungFriends = users.stream()
            .filter(user -> !user.equals(hung) && allFriendships.stream().anyMatch(f -> 
                (f.getUserId1().equals(hung.getId()) && f.getUserId2().equals(user.getId())) || 
                (f.getUserId1().equals(user.getId()) && f.getUserId2().equals(hung.getId()))
            ))
            .toList();
            
        if (!hungFriends.isEmpty()) {
            List<User> hungGroup = new ArrayList<>();
            hungGroup.add(hung);
            hungGroup.addAll(hungFriends);
            
            Conversation groupConv = Conversation.builder()
                    .title("Nhóm bạn thân")
                    .imageUrl("https://res.cloudinary.com/da1zrkrmi/image/upload/v1751256457/default_group_image_la4psw.png")
                    .cloudinaryImageId("default_group_image_la4psw")
                    .type(Conversation.Type.GROUP)
                    .build();

            conversations.add(groupConv);
            conversationParticipants.put(groupConv, hungGroup);
        }

        // Create another group with friends Trang, Thuy, Vinh if they are all friends
        User trang = users.get(1);
        User thuy = users.get(2);
        User vinh = users.get(3);
        
        boolean trangThuyFriends = allFriendships.stream().anyMatch(f -> 
            (f.getUserId1().equals(trang.getId()) && f.getUserId2().equals(thuy.getId())) || 
            (f.getUserId1().equals(thuy.getId()) && f.getUserId2().equals(trang.getId()))
        );
        
        boolean trangVinhFriends = allFriendships.stream().anyMatch(f -> 
            (f.getUserId1().equals(trang.getId()) && f.getUserId2().equals(vinh.getId())) || 
            (f.getUserId1().equals(vinh.getId()) && f.getUserId2().equals(trang.getId()))
        );
        
        boolean thuyVinhFriends = allFriendships.stream().anyMatch(f -> 
            (f.getUserId1().equals(thuy.getId()) && f.getUserId2().equals(vinh.getId())) || 
            (f.getUserId1().equals(vinh.getId()) && f.getUserId2().equals(thuy.getId()))
        );
        
        if (trangThuyFriends && trangVinhFriends && thuyVinhFriends) {
            Conversation smallGroupConv = Conversation.builder()
                    .title("Bạn thân thiết")
                    .imageUrl("https://res.cloudinary.com/da1zrkrmi/image/upload/v1751256457/default_group_image_la4psw.png")
                    .cloudinaryImageId("default_group_image_la4psw")
                    .type(Conversation.Type.GROUP)
                    .build();

            conversations.add(smallGroupConv);
            conversationParticipants.put(smallGroupConv, List.of(trang, thuy, vinh));
        }

        // Save conversations first
        conversationRepository.saveAll(conversations);

        // Create participants and messages
        for (Map.Entry<Conversation, List<User>> entry : conversationParticipants.entrySet()) {
            Conversation conversation = entry.getKey();
            List<User> participants = entry.getValue();

            // Add participants
            for (User user : participants) {
                // For private conversations, all participants are members
                // For group conversations, first user is admin, others are members
                Participant.Type participantType =
                        (conversation.getType() == Conversation.Type.PRIVATE) ?
                                Participant.Type.MEMBER :
                                (user.equals(participants.get(0)) ? Participant.Type.ADMIN : Participant.Type.MEMBER);

                Participant participant = Participant.builder()
                        .conversationId(conversation.getId())
                        .userId(user.getId())
                        .type(participantType)
                        .build();

                participantRepository.save(participant);
            }

            // Add some messages to the conversation
            createMessagesForConversation(conversation, participants);
        }

        log.info("Created {} conversations with participants and messages", conversations.size());
    }

    private void createMessagesForConversation(Conversation conversation, List<User> participants) {
        List<Message> messages = new ArrayList<>();
        Random random = new Random();

        // Generate 5-20 messages per conversation
        int messageCount = 5 + random.nextInt(16);

        for (int i = 0; i < messageCount; i++) {
            User sender = participants.get(random.nextInt(participants.size()));

            Message message = Message.builder()
                    .conversation(conversation)
                    .senderId(sender.getId())
                    .type(Message.Type.TEXT)
                    .message(generateRandomMessage(sender.getFirstName(), conversation))
                    .build();

            messages.add(message);
        }

        messageRepository.saveAll(messages);
    }

    private String generateRandomMessage(String senderName, Conversation conversation) {
        List<String> messages = List.of(
                "Xin chào mọi người!",
                "Hôm nay bạn khỏe không?",
                "Mình khỏe, cảm ơn vì đã hỏi thăm!",
                "Cuối tuần này chúng ta gặp nhau nhé.",
                "Xin lỗi, mình không thể đến được.",
                "Có ai xem bộ phim mới chưa?",
                "Mấy giờ chúng ta gặp nhau?",
                "Đây là tin nhắn thử nghiệm.",
                "Mình đang làm việc trên dự án này.",
                "Ai có thể giúp mình vấn đề này được không?",
                "Mình sẽ có mặt trong 10 phút nữa.",
                "Đừng quên cuộc họp ngày mai nhé.",
                "Chúc mừng sinh nhật!",
                "Chúc mừng thành tích của bạn!",
                "Mình nhớ mọi người quá!",
                "Chúng ta cùng đi ăn trưa nhé.",
                "Xem trang web mới này mình tìm được này.",
                "Tuần sau mình đi nghỉ mát."
        );

        // Simply return a random message
        return messages.get(new Random().nextInt(messages.size()));
    }

    private void createFriendRequests(UserRepository userRepository) {
        if (friendRequestRepository.count() > 0) {
            log.info("Friend request data already exists, skipping creation");
            return;
        }

        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals("admin"))
                .toList();

        // Create some pending friend requests
        // Nam (4) sends friend request to Trang (1)
        FriendRequest request1 = FriendRequest.builder()
                .sender(users.get(4))
                .receiver(users.get(1))
                .status(FriendRequestStatus.PENDING)
                .build();

        // Thuy (2) sends friend request to Nam (4)
        FriendRequest request2 = FriendRequest.builder()
                .sender(users.get(2))
                .receiver(users.get(4))
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.saveAll(List.of(request1, request2));
        log.info("Created friend requests");
    }
}