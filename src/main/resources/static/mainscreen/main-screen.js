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
        const isPrivate = $(this).data('type') === 'PRIVATE';
        
        // Navigate to the appropriate chat window based on conversation type
        if (isPrivate) {
            window.location.href = `../chat/private-chat.html?id=${conversationId}`;
        } else {
            // For group chats, load in the current view
            loadConversationMessages(conversationId);
        }
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
        console.log('Connected to WebSocket');
        
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

// Renamed function to getUsernameFromToken for better clarity
function getUsernameFromToken(token) {
    // Extract username from JWT token
    try {
        // JWT token consists of 3 parts separated by dots
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return null;
        }
        
        // The second part contains the payload
        const payload = JSON.parse(atob(tokenParts[1]));
        return payload.sub; // Return username (subject) instead of userId
    } catch (e) {
        console.error('Error extracting username from token', e);
        return null;
    }
}

function onMessageReceived(payload) {
    console.log('Received message:');
    
    try {
        const message = JSON.parse(payload.body);
        
        // Play notification sound
        playNotificationSound();
        
        // Update conversation list to show new message
        const conversationId = message.conversationId || (message.conversation ? message.conversation.id : null);
        if (conversationId) {
            updateUnreadCount(conversationId);
        }
    } catch (e) {
        console.error('Error processing message:', e);
    }
}

function playNotificationSound() {
    // You can implement a sound notification here
    // const audio = new Audio('../sounds/notification.mp3');
    // audio.play().catch(e => console.log('Error playing notification sound:', e));
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

function loadConversationMessages(conversationId) {
    // Clear the chat container
    const chatContainer = $('#chat-container');
    chatContainer.html('<div class="loading">Loading messages...</div>');
    
    // In a real implementation, you would load messages from the server here
    // For now, just display a placeholder
    setTimeout(() => {
        chatContainer.html(`
            <div class="chat-header">
                <h3>Conversation #${conversationId}</h3>
            </div>
            <div class="messages-container">
                <div class="messages-list">
                    <div class="message-timestamp">Today</div>
                    <div class="message received">
                        <div class="message-content">Hello! This is a placeholder message.</div>
                        <div class="message-time">10:30 AM</div>
                    </div>
                    <div class="message sent">
                        <div class="message-content">This is just a demo. Real messaging will be implemented soon!</div>
                        <div class="message-time">10:32 AM</div>
                    </div>
                </div>
            </div>
            <div class="message-input-container">
                <input type="text" id="message-input" placeholder="Type a message...">
                <button id="send-message-btn">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        `);
    }, 1000);
}

function getAvatarInitial(name) {
    if (!name || name === '') {
        return '?';
    }
    const initials = name.split(' ').map(part => part[0]).join('');
    return initials.substring(0, 2).toUpperCase();
}