/**
 * Chat Utility Functions
 * Shared between various chat screens
 */

// WebSocket Functions
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

// Message Display Functions
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

function appendNewMessage(message, messagesListId = 'messages-list') {
    console.log('Appending message to chat:', message);
    const isMine = message.belongCurrentUser;
    
    const date = new Date(message.createdAt || new Date());
    const messageDate = formatDate(date);
    const timeString = formatTime(date);
    
    // Find the correct messages list to append to
    const messagesList = $(`#${messagesListId}`);
    if (messagesList.length === 0) {
        console.error(`Messages list with ID ${messagesListId} not found`);
        return;
    }
    
    // Check if we need to add a new date divider
    const lastDateDivider = messagesList.find('.date-divider:last span').text();
    if (!lastDateDivider || lastDateDivider !== messageDate) {
        messagesList.append(`
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
    
    messagesList.append(messageEl);
    
    // Scroll to bottom of messages
    const messagesContainer = messagesList.closest('.messages-container');
    messagesContainer.scrollTop(messagesContainer.prop('scrollHeight'));
    
    console.log('Message appended successfully');
}

function displayMessages(messages, messagesListId = 'messages-list') {
    if (!messages || messages.length === 0) {
        $(`#${messagesListId}`).html('<div class="empty-messages">No messages yet</div>');
        return;
    }

    const messagesListEl = $(`#${messagesListId}`);
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

// Avatar Functions
function getAvatarInitial(name) {
    if (!name || name === '') {
        return '?';
    }
    const initials = name.split(' ').map(part => part[0]).join('');
    return initials.substring(0, 2).toUpperCase();
}

// API Functions
function loadMessages(conversationId, messagesListId = 'messages-list') {
    $(`#${messagesListId}`).html('<div class="loading-messages">Loading messages...</div>');
    
    return $.ajax({
        url: `http://localhost:9000/messages/conversation/${conversationId}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            displayMessages(response.data, messagesListId);
        },
        error: function(xhr, status, error) {
            console.error('Error loading messages:', error);
            $(`#${messagesListId}`).html('<div class="error-messages">Error loading messages. Please try again later.</div>');
        }
    });
}

// WebSocket Functions
function sendMessage(conversationId, stompClient, messageInputId = 'message-input') {
    const messageInput = $(`#${messageInputId}`);
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