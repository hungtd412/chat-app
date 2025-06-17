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

/**
 * Check if a websocket subscription already exists for a given destination
 * @param {string} destination - The subscription destination
 * @returns {boolean} True if the subscription exists, false otherwise
 */
function isAlreadySubscribed(destination) {
    return window.activeSubscriptions && window.activeSubscriptions[destination] !== undefined;
}

/**
 * Safely unsubscribe from a websocket destination
 * @param {string} destination - The subscription destination to unsubscribe from
 * @returns {boolean} True if unsubscribed successfully, false otherwise
 */
function unsubscribeFrom(destination) {
    if (window.activeSubscriptions && window.activeSubscriptions[destination]) {
        try {
            window.activeSubscriptions[destination].unsubscribe();
            delete window.activeSubscriptions[destination];
            console.log(`Unsubscribed from: ${destination}`);
            return true;
        } catch (e) {
            console.error(`Failed to unsubscribe from ${destination}:`, e);
        }
    }
    return false;
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
    
    // Avatar display
    const avatarHtml = !isMine ? 
        `<div class="message-avatar">
            ${message.senderAvtUrl ? 
                `<img src="${message.senderAvtUrl}" alt="${message.senderName || 'User'}" />` : 
                `<div class="avatar-placeholder">${getAvatarInitial(message.senderName)}</div>`
            }
         </div>` : '';

    const messageEl = $(`
        <div class="message ${messageClass}">
            ${avatarHtml}
            <div class="message-bubble">
                ${!isMine ? `<div class="message-sender">${message.senderName || 'User'}</div>` : ''}
                <div class="message-content">${escapeHtml(content)}</div>
                <div class="message-time">${timeString}</div>
            </div>
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
    let currentSenderId = null;
    
    sortedMessages.forEach((message, index) => {
        const date = new Date(message.createdAt);
        const messageDate = formatDate(date);
        const isMine = message.belongCurrentUser;
        const messageClass = isMine ? 'outgoing' : 'incoming';
        const timeString = formatTime(date);
        
        // Check if we should show the avatar (only for first message in a sequence from same sender)
        const showAvatar = !isMine && (message.senderId !== currentSenderId);
        
        // Add date divider if this is a new date
        if (messageDate !== currentDate) {
            currentDate = messageDate;
            messagesListEl.append(`
                <div class="date-divider">
                    <span>${messageDate}</span>
                </div>
            `);
        }
        
        // Avatar display
        const avatarHtml = !isMine && showAvatar ? 
            `<div class="message-avatar">
                ${message.senderAvtUrl ? 
                    `<img src="${message.senderAvtUrl}" alt="${message.senderName || 'User'}" />` : 
                    `<div class="avatar-placeholder">${getAvatarInitial(message.senderName)}</div>`
                }
             </div>` : 
            (!isMine ? '<div class="message-avatar-spacer"></div>' : '');
        
        // Show sender name only for the first message in a group from the same sender
        const showSenderName = !isMine && (message.senderId !== currentSenderId);
        
        const messageEl = $(`
            <div class="message ${messageClass}${showAvatar ? '' : ' subsequent-message'}">
                ${avatarHtml}
                <div class="message-bubble">
                    ${showSenderName ? `<div class="message-sender">${message.senderName || 'User'}</div>` : ''}
                    <div class="message-content">${escapeHtml(message.content)}</div>
                    <div class="message-time">${timeString}</div>
                </div>
            </div>
        `);

        messagesListEl.append(messageEl);
        currentSenderId = message.senderId;
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

/**
 * Updates the unread count for a conversation
 * @param {number} conversationId - The ID of the conversation
 * @param {boolean} increment - Whether to increment or clear the badge
 */
function updateUnreadCountUtil(conversationId, increment = true) {
    // If mainscreen's updateUnreadCount exists, use it
    if (typeof window.updateUnreadCount === 'function') {
        window.updateUnreadCount(conversationId);
        return;
    }

    const conversationItem = $(`.conversation-item[data-id="${conversationId}"]`);
    
    if (conversationItem.length > 0) {
        // Check if there's already an unread badge
        let unreadBadge = conversationItem.find('.unread-badge');
        
        if (increment) {
            if (unreadBadge.length === 0) {
                // Create badge if it doesn't exist
                conversationItem.find('.conversation-info').append('<div class="unread-badge">1</div>');
            } else {
                // Update existing badge
                const currentCount = parseInt(unreadBadge.text()) || 0;
                unreadBadge.text(currentCount + 1);
            }
        } else {
            // Clear badge
            if (unreadBadge.length > 0) {
                unreadBadge.remove();
            }
        }
        
        // Update title if available
        if (typeof window.updatePageTitle === 'function') {
            window.updatePageTitle();
        }
    }
}

/**
 * Clears the unread badge for a conversation
 * @param {number} conversationId - The ID of the conversation
 */
function clearUnreadBadgeUtil(conversationId) {
    updateUnreadCountUtil(conversationId, false);
}