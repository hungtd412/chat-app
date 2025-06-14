$(document).ready(function() {
    // Check if user is logged in by verifying both tokens in localStorage
    const accessToken = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');
    
    if (!accessToken || !refreshToken) {
        window.location.href = '../signin/signin.html';
        return;
    }

    // Load conversations and establish WebSocket connection
    loadConversations();
    connectToWebSocket();

    // Search functionality
    $('#search-input').on('input', function() {
        const searchTerm = $(this).val().toLowerCase();
        $('.conversation-item').each(function() {
            const text = $(this).find('.conversation-name').text().toLowerCase();
            if (text.includes(searchTerm)) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });
    });

    // Click handler for conversation selection
    $(document).on('click', '.conversation-item', function() {
        $('.conversation-item').removeClass('active');
        $(this).addClass('active');
        
        const conversationId = $(this).data('id');
        const conversationType = $(this).data('type');
        const displayName = $(this).find('.conversation-name').text();
        
        // Load chat component in the main content area
        loadChatComponent(conversationId, conversationType, displayName);
    });

    // New conversation button click handler
    $('#new-conversation-btn').click(function() {
        // Navigate to new conversation page or show modal
        alert('New conversation feature coming soon!');
    });

    // Make sure logout button is visible and attached
    console.log("Logout button element:", $('#logout-button').length);
    
    // Logout button click handler
    $('#logout-button').click(function() {
        console.log("Logout button clicked");
        const accessToken = localStorage.getItem('access_token');
        const refreshToken = localStorage.getItem('refresh_token');

        // Disconnect WebSocket before logout
        if (stompClient && stompClient.connected) {
            stompClient.disconnect();
        }
        
        $.ajax({
            url: 'http://localhost:9000/auth/log-out',
            type: 'POST',
            contentType: 'application/json',
            headers: {
                'Authorization': 'Bearer ' + accessToken
            },
            data: JSON.stringify({
                accessToken: accessToken,
                refreshToken: refreshToken
            }),
            success: function(response) {
                // Clear tokens from local storage
                localStorage.removeItem('access_token');
                localStorage.removeItem('refresh_token');
                // Redirect to login page
                window.location.href = '../signin/signin.html';
            },
            error: function(xhr, status, error) {
                console.error('Logout error:', error);
                
                // Even if the server request fails, still clear tokens and redirect
                localStorage.removeItem('access_token');
                localStorage.removeItem('refresh_token');
                window.location.href = '../signin/signin.html';
            }
        });
    });

    // Send message button click handler
    $(document).on('click', '#send-message-btn', function() {
        const conversationId = $(this).data('conversation-id');
        sendMessage(conversationId, stompClient);
    });

    // Send message on Enter key
    $(document).on('keypress', '#message-input', function(e) {
        if (e.which === 13) { // Enter key
            const conversationId = $('#send-message-btn').data('conversation-id');
            sendMessage(conversationId, stompClient);
        }
    });
});

let stompClient = null;

function connectToWebSocket() {
    // Get JWT token for authentication
    const token = localStorage.getItem('access_token');
    if (!token) {
        console.error('No token found, cannot establish WebSocket connection');
        return;
    }
    
    // Fix the WebSocket URL to include the full URL with port
    const socket = new SockJS('http://localhost:9000/ws');
    stompClient = Stomp.over(socket);
    
    // Store in global window object for access from other pages
    window.stompClient = stompClient;
    
    // Disable debug messages
    stompClient.debug = null;
    
    // Add JWT authentication header
    const headers = {
        'Authorization': 'Bearer ' + token
    };
    
    // Connect to the WebSocket and subscribe to personal queue
    stompClient.connect(headers, function(frame) {
        // Get username from token
        const username = getUsernameFromToken(token);

        if (username) {
            // Subscribe to personal queue for private messages using username
            stompClient.subscribe(`/user/${username}/queue/messages`, onMessageReceived);
            console.log(`Subscribed to /user/${username}/queue/messages`);
        }
    }, function(error) {
        console.error('WebSocket connection error: ', error);
        // Reconnect after delay
        setTimeout(connectToWebSocket, 5000);
    });
}

function onMessageReceived(payload) {
    try {
        const message = JSON.parse(payload.body);
        console.log('Received message in main-screen:', message);
        
        const conversationId = message.conversationId;
        
        // Check if we're currently viewing this conversation
        const activeConversationId = $('#chat-frame').data('conversation-id');
        
        if (activeConversationId && activeConversationId == conversationId) {
            // If we're viewing this conversation, append the message
            appendNewMessage(message);
            
            // Play a subtle notification sound for new messages
            if (!message.belongCurrentUser) {
                playNotificationSound();
            }
        } else if (!message.belongCurrentUser) {
            // Otherwise, update unread count and play notification
            playNotificationSound();
            updateUnreadCount(conversationId);
        }
    } catch (e) {
        console.error('Error processing message:', e);
    }
}

function playNotificationSound() {
    const audio = new Audio('../sound/new-message-notification.mp3');
    audio.play().catch(e => console.log('Error playing notification sound:', e));
}

function updateUnreadCount(conversationId) {
    // Implement logic to update unread message count for a conversation
    const conversationItem = $(`.conversation-item[data-id="${conversationId}"]`);
    if (conversationItem.length > 0) {
        let unreadBadge = conversationItem.find('.unread-badge');
        
        if (unreadBadge.length === 0) {
            // Create badge if it doesn't exist
            conversationItem.find('.conversation-info').append('<div class="unread-badge">1</div>');
        } else {
            // Update existing badge
            const count = parseInt(unreadBadge.text()) + 1;
            unreadBadge.text(count);
        }
    }
}

function loadConversations() {
    $.ajax({
        url: 'http://localhost:9000/conversations',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            displayConversations(response.data);
        },
        error: function(xhr, status, error) {
            console.error('Error loading conversations:', error);
            $('#conversation-list').html('<div class="error">Error loading conversations. Please try again later.</div>');
        }
    });
}

function displayConversations(conversations) {
    if (!conversations || conversations.length === 0) {
        $('#conversation-list').html('<div class="empty-list">No conversations yet</div>');
        return;
    }

    const conversationList = $('#conversation-list');
    conversationList.empty();

    conversations.forEach(conversation => {
        const isGroup = conversation.type === 'GROUP';
        const displayName = isGroup ? conversation.title : conversation.friendName;
        const avatarInitial = getAvatarInitial(displayName);
        const avatarClass = isGroup ? 'avatar group-avatar' : 'avatar';

        const conversationItem = `
            <div class="conversation-item" data-id="${conversation.id}" data-type="${conversation.type}">
                <div class="${avatarClass}">
                    ${avatarInitial}
                </div>
                <div class="conversation-info">
                    <div class="conversation-name">${displayName}</div>
                </div>
            </div>
        `;

        conversationList.append(conversationItem);
    });
}

function loadChatComponent(conversationId, conversationType, displayName) {
    // Clear the chat container and show loading
    const chatContainer = $('#chat-container');
    chatContainer.html('<div class="loading">Loading chat...</div>');

    // Load the chat component HTML template
    $.get('../chat/chat.html', function(template) {
        // Replace the chat container with the loaded template
        chatContainer.html(template);

        // Set data attributes for the chat frame
        $('#chat-frame').attr('data-conversation-id', conversationId);
        $('#chat-frame').attr('data-conversation-type', conversationType);

        // Set initial UI content
        $('#chat-title').text(displayName);
        $('#status-text').text(conversationType === 'PRIVATE' ? 'Online' : 'Group chat');

        // Remove back button since we're in the main screen
        $('#back-button').hide();

        // Setup event handlers for send button
        $('#send-button').attr('data-conversation-id', conversationId);
        $('#send-button').click(function() {
            sendMessage(conversationId, stompClient, 'message-input');
        });

        // Setup event handler for enter key in message input
        $('#message-input').keypress(function(e) {
            if (e.which === 13) { // Enter key
                sendMessage(conversationId, stompClient, 'message-input');
            }
        });

        initializeChat(conversationId, conversationType);
    })
    .fail(function(xhr, status, error) {
        console.error('Error loading chat template:', error);
        chatContainer.html(`
            <div class="error-container">
                <div class="error-icon">
                    <i class="fas fa-exclamation-circle"></i>
                </div>
                <h3>Cannot render the conversation</h3>
                <p>There was a problem loading the chat interface. Please try again later.</p>
                <button class="retry-button" onclick="loadChatComponent('${conversationId}', '${conversationType}', '${displayName}')">
                    <i class="fas fa-sync"></i> Retry
                </button>
            </div>
        `);
    });
}