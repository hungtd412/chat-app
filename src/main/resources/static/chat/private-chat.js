$(document).ready(function() {
    // Check if user is logged in
    const token = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');
    if (!token || !refreshToken) {
        window.location.href = '../signin/signin.html';
        return;
    }

    // Get conversation ID from URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const conversationId = urlParams.get('id');
    
    if (!conversationId) {
        alert('Invalid conversation ID');
        window.location.href = 'chat-list.html';
        return;
    }

    // Global WebSocket client
    window.stompClient = window.stompClient || null;
    
    // Try to reconnect to the WebSocket if needed
    ensureWebSocketConnection();

    // Load conversation details and messages
    loadConversationDetails(conversationId);
    loadMessages(conversationId);

    // Handle back button
    $('#back-button').click(function() {
        window.location.href = '../mainscreen/main-screen.html';
    });

    // Handle send message
    $('#send-button').click(function() {
        sendMessage(conversationId);
    });

    // Send message on Enter key
    $('#message-input').keypress(function(e) {
        if (e.which === 13) { // Enter key
            sendMessage(conversationId);
        }
    });

    function ensureWebSocketConnection() {
        // Check if we have a global stompClient or need to create a new one
        if (window.stompClient && window.stompClient.connected) {
            console.log('Using existing WebSocket connection');
            subscribeToTopics();
        } else {
            console.log('Creating new WebSocket connection');
            connectToWebSocket();
        }
    }

    function connectToWebSocket() {
        // Get JWT token for authentication
        const token = localStorage.getItem('access_token');
        if (!token) {
            console.error('No token found, cannot establish WebSocket connection');
            return;
        }
        
        // Create the SockJS connection to the WebSocket
        // Fix the WebSocket URL to match the server configuration
        const socket = new SockJS('http://localhost:9000/ws');
        window.stompClient = Stomp.over(socket);
        
        // Disable debug messages
        window.stompClient.debug = null;
        
        // Add JWT authentication header
        const headers = {
            'Authorization': 'Bearer ' + token
        };
        
        // Connect to the WebSocket and subscribe to personal queue
        window.stompClient.connect(headers, function(frame) {
            console.log('Connected to WebSocket');
            subscribeToTopics();
        }, function(error) {
            console.error('WebSocket connection error: ', error);
            // Reconnect after delay
            setTimeout(connectToWebSocket, 5000);
        });
    }

    function subscribeToTopics() {
        if (!window.stompClient) return;
        
        try {
            // Get username from token
            const username = getUsernameFromToken(localStorage.getItem('access_token'));

            if (username) {
                // Subscribe to the user's private queue using username
                window.stompClient.subscribe(`/user/${username}/queue/messages`, onMessageReceived);
                console.log(`Subscribed to personal queue for user ${username} : /queue/${username}/queue/messages\``);
            }
        
            // Subscribe to the conversation topic (for group chats)
            window.stompClient.subscribe(`/topic/conversation.${conversationId}`, onMessageReceived);
            console.log(`Subscribed to topic for conversation ${conversationId}`);
        } catch (e) {
            console.error('Error subscribing to WebSocket topics:', e);
        }
    }

    function onMessageReceived(payload) {
        console.log('Message received via WebSocket:', payload);
        
        try {
            const message = JSON.parse(payload.body);
            console.log('Parsed message:', message);
            console.log(message.conversationId)
            // Only process message if it's for the current conversation
            if (message.conversationId && message.conversationId == conversationId) {
                appendNewMessage(message);
            }

        } catch (error) {
            console.error('Error processing received message:', error);
        }
    }

    function appendNewMessage(message) {
        console.log('Appending message to chat:', message);
        const username = getUsernameFromToken(localStorage.getItem('access_token'));
        const isMine = message.belongCurrentUser;
        console.log(isMine)
        
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
});

// Renamed function to getUsernameFromToken to better reflect what it actually does
function getUsernameFromToken(token) {
    if (!token) return null;
    
    try {
        // JWT token consists of 3 parts separated by dots
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return null;
        }
        
        // The second part contains the payload
        const payload = JSON.parse(atob(tokenParts[1]));
        return payload.sub; // returning the username (subject)
    } catch (e) {
        console.error('Error extracting username from token', e);
        return null;
    }
}

function loadConversationDetails(conversationId) {
    $.ajax({
        url: `http://localhost:9000/conversations/${conversationId}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            const conversation = response.data;
            if (conversation.type === 'PRIVATE') {
                $('#chat-title').text(conversation.friendName || 'Private Chat');
                $('.status').text('Online'); // Can be updated with actual status
            } else {
                $('#chat-title').text(conversation.title || 'Group Chat');
                $('.status').text(`${conversation.participantCount || 0} members`); // Can be updated with actual count
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading conversation details:', error);
            $('#chat-title').text('Chat');
            $('.status').text('Error loading details');
        }
    });
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
    if (window.stompClient && window.stompClient.connected) {
        console.log('Sending message via WebSocket');
        const message = {
            conversationId: conversationId,
            type: 'TEXT',
            content: messageText // Changed from message to content to match MessageRequest
        };
        
        // Add authorization headers when sending the message
        const headers = {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        };
        console.log(headers)
        
        window.stompClient.send("/app/chat.send", headers, JSON.stringify(message));
    } else {
        console.error('WebSocket not connected, cannot send message');
        alert('Connection error. Please refresh the page and try again.');
        messageInput.val(messageText); // Restore the message text
    }
}

function appendNewMessage(message) {
    console.log('Appending message to chat:', message);
    const username = getUsernameFromToken(localStorage.getItem('access_token'));
    const isMine = message.belongCurrentUser || (message.senderId == username);
    
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