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

    // Load conversation details and messages
    loadConversationDetails(conversationId);
    loadMessages(conversationId);

    // Handle back button
    $('#back-button').click(function() {
        window.location.href = 'chat-list.html';
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
});

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
    
    $.ajax({
        url: 'http://localhost:9000/messages',
        type: 'POST',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json'
        },
        data: JSON.stringify({
            conversationId: conversationId,
            type: 'TEXT',
            message: messageText
        }),
        success: function(response) {
            // After successfully sending, reload messages to show new one
            loadMessages(conversationId);
        },
        error: function(xhr, status, error) {
            console.error('Error sending message:', error);
            alert('Failed to send message. Please try again.');
        }
    });
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
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
