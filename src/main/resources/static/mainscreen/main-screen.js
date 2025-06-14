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
        
        // Load conversation in the chat container
        loadConversation(conversationId, conversationType, displayName);
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
        sendMessage(conversationId);
    });

    // Send message on Enter key
    $(document).on('keypress', '#message-input', function(e) {
        if (e.which === 13) { // Enter key
            const conversationId = $('#send-message-btn').data('conversation-id');
            sendMessage(conversationId);
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
        console.log('Parsed message:', message);
        
        // Play notification sound
        playNotificationSound();
        
        // Update conversation list to show new message
        const conversationId = message.conversationId;
        
        if (conversationId) {
            // Check if we're currently viewing this conversation
            const activeConversationId = $('#send-message-btn').data('conversation-id');
            
            if (activeConversationId && activeConversationId == conversationId) {
                // If we're viewing this conversation, append the message
                appendNewMessage(message);
            } else {
                // Otherwise, update unread count
                updateUnreadCount(conversationId);
            }
        }
    } catch (e) {
        console.error('Error processing message:', e);
    }
}

function appendNewMessage(message) {
    console.log('Appending message to chat:', message);
    const isMine = message.belongCurrentUser;
    
    const date = new Date(message.createdAt || new Date());
    const messageDate = formatDate(date);
    const timeString = formatTime(date);
    
    // Check if we need to add a new date divider
    const lastDateDivider = $('.date-divider:last span').text();
    if (!lastDateDivider || lastDateDivider !== messageDate) {
        $('#messages-list').append(`
            <div class="date-divider">
                <span>${messageDate}</span>
            </div>
        `);
    }
    
    const messageClass = isMine ? 'outgoing' : 'incoming';
    const content = message.content;
    
    const messageEl = $(`
        <div class="message ${messageClass}">
            ${!isMine ? `<div class="message-sender">${message.senderName || 'User'}</div>` : ''}
            <div class="message-content">${escapeHtml(content)}</div>
            <div class="message-time">${timeString}</div>
        </div>
    `);
    
    $('#messages-list').append(messageEl);
    
    // Scroll to bottom of messages
    const messagesContainer = $('.messages-container');
    messagesContainer.scrollTop(messagesContainer.prop('scrollHeight'));
}

function playNotificationSound() {
    // You can implement a sound notification here
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

function loadConversation(conversationId, conversationType, displayName) {
    // Clear the chat container and show loading
    const chatContainer = $('#chat-container');
    chatContainer.html('<div class="loading">Loading messages...</div>');
    
    // Set the chat UI with the information we already have
    const isPrivate = conversationType === 'PRIVATE';
    const statusText = isPrivate ? 'Online' : 'Group chat';
    
    // Create chat UI
    chatContainer.html(`
        <div class="chat-header">
            <div class="chat-header-info">
                <h3 id="chat-title">${displayName}</h3>
                <div class="status">${statusText}</div>
            </div>
        </div>
        <div class="messages-container">
            <div id="messages-list" class="messages-list">
                <!-- Messages will be loaded here -->
            </div>
        </div>
        <div class="chat-input-container">
            <input type="text" id="message-input" placeholder="Type a message...">
            <button id="send-message-btn" data-conversation-id="${conversationId}">
                <i class="fas fa-paper-plane"></i>
            </button>
        </div>
    `);
    
    // Load messages for this conversation
    loadMessages(conversationId);
    
    // Subscribe to conversation topic for group chats
    if (conversationType === 'GROUP') {
        if (stompClient && stompClient.connected) {
            stompClient.subscribe(`/topic/conversation.${conversationId}`, onMessageReceived);
            console.log(`Subscribed to topic for conversation ${conversationId}`);
        }
    }
}

function loadMessages(conversationId) {
    $('#messages-list').html('<div class="loading-messages">Loading messages...</div>');
    
    $.ajax({
        url: `http://localhost:9000/messages/conversation/${conversationId}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            displayMessages(response.data);
        },
        error: function(xhr, status, error) {
            console.error('Error loading messages:', error);
            $('#messages-list').html('<div class="error-messages">Error loading messages. Please try again later.</div>');
        }
    });
}

function displayMessages(messages) {
    if (!messages || messages.length === 0) {
        $('#messages-list').html('<div class="empty-messages">No messages yet</div>');
        return;
    }

    const messagesListEl = $('#messages-list');
    messagesListEl.empty();
    
    // Sort messages by id in ascending order (oldest first)
    // Even though API returns in descending order, for display we want ascending
    const sortedMessages = [...messages].sort((a, b) => a.id - b.id);
    
    let currentDate = '';
    sortedMessages.forEach(message => {
        const date = new Date(message.createdAt);
        const messageDate = formatDate(date);
        
        // Add date divider if this is a new date
        if (messageDate !== currentDate) {
            currentDate = messageDate;
            messagesListEl.append(`
                <div class="date-divider">
                    <span>${messageDate}</span>
                </div>
            `);
        }
        
        const messageClass = message.belongCurrentUser ? 'outgoing' : 'incoming';
        const timeString = formatTime(date);
        
        const messageEl = $(`
            <div class="message ${messageClass}">
                ${!message.belongCurrentUser ? `<div class="message-sender">${message.senderName || 'User'}</div>` : ''}
                <div class="message-content">${escapeHtml(message.content)}</div>
                <div class="message-time">${timeString}</div>
            </div>
        `);
        
        messagesListEl.append(messageEl);
    });
    
    // Scroll to bottom of messages
    const messagesContainer = $('.messages-container');
    messagesContainer.scrollTop(messagesContainer.prop('scrollHeight'));
}

function sendMessage(conversationId) {
    const messageInput = $('#message-input');
    const messageText = messageInput.val().trim();
    
    if (!messageText) return;
    
    // Clear input before sending to make UI more responsive
    messageInput.val('');

    // Check if we can use WebSocket
    if (stompClient && stompClient.connected) {
        console.log('Sending message via WebSocket');
        const message = {
            conversationId: conversationId,
            type: 'TEXT',
            content: messageText
        };
        
        // Add authorization headers when sending the message
        const headers = {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        };
        
        stompClient.send("/app/chat.send", headers, JSON.stringify(message));
    } else {
        console.error('WebSocket not connected, cannot send message');
        alert('Connection error. Please refresh the page and try again.');
        messageInput.val(messageText); // Restore the message text
    }
}

function formatDate(date) {
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    
    if (date.toDateString() === today.toDateString()) {
        return 'Today';
    } else if (date.toDateString() === yesterday.toDateString()) {
        return 'Yesterday';
    } else {
        // Format: June 12, 2023
        return date.toLocaleDateString('en-US', { 
            month: 'long', 
            day: 'numeric', 
            year: 'numeric' 
        });
    }
}

function formatTime(date) {
    // Format: 10:30 AM
    return date.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: true 
    });
}

function escapeHtml(text) {
    if (text === null || text === undefined) {
        return '';
    }
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function getAvatarInitial(name) {
    if (!name || name === '') {
        return '?';
    }
    const initials = name.split(' ').map(part => part[0]).join('');
    return initials.substring(0, 2).toUpperCase();
}