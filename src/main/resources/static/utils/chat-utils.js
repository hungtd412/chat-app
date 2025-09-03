/**
 * Chat Utility Functions
 * Shared between various chat screens
 */

// WebSocket Functions
function getUserIdFromToken(token) {
    if (!token) return null;

    try {
        // JWT token consists of 3 parts separated by dots
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return null;
        }

        // The second part contains the payload
        const payload = JSON.parse(atob(tokenParts[1]));
        return payload.sub; // returning the userId (subject)
    } catch (e) {
        console.error('Error extracting userId from token', e);
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
    
    // Store the conversation ID globally for message handling
    window.currentConversationId = conversationId;
    
    return $.ajax({
        url: `http://localhost:9000/messages/conversation/${conversationId}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            console.log(`Loaded ${response.data ? response.data.length : 0} initial messages`);
            
            // Remove any loading indicators
            $(`#${messagesListId} .loading-messages, #${messagesListId} .loading-more-messages`).remove();
            
            if (response.data && response.data.length > 0) {
                displayMessages(response.data, messagesListId);
                
                // Store the oldest message ID for pagination
                const oldestMessage = [...response.data].sort((a, b) => a.id - b.id)[0];
                window.oldestMessageId = oldestMessage.id;
                console.log(`Initial oldest message ID: ${window.oldestMessageId}`);
                
                // Add scroll handler to load more messages
                setupScrollHandler(messagesListId);
                
                // Store whether there might be more messages
                window.hasMoreMessages = response.data.length >= 15; // 15 is default limit
            } else {
                $(`#${messagesListId}`).html('<div class="empty-messages">No messages yet</div>');
                window.hasMoreMessages = false;
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading messages:', xhr.status, error);
            $(`#${messagesListId}`).html(
                '<div class="error-messages">Error loading messages. Please try again later.</div>'
            );
        }
    });
}

/**
 * Loads more messages with pagination
 * @param {number} conversationId - The conversation ID
 * @param {number} offset - The message ID to start from (0 means start from the most recent)
 * @param {string} messagesListId - The ID of the messages list element
 */
function loadMoreMessages(conversationId, offset = 0, messagesListId = 'messages-list') {
    // If we're already loading messages, don't start another request
    if (window.isLoadingMore) {
        console.log("Already loading messages, skipping request");
        return;
    }
    
    // Set flag to prevent multiple simultaneous loads
    window.isLoadingMore = true;
    console.log(`Loading more messages for conversation ${conversationId} with offset ${offset}`);
    
    // Add loading indicator at the top
    $(`#${messagesListId}`).prepend('<div class="loading-more-messages">Loading more messages...</div>');
    
    $.ajax({
        url: `http://localhost:9000/messages/conversation/${conversationId}?offset=${offset}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            console.log(`Loaded ${response.data ? response.data.length : 0} more messages`);
            
            // Remove loading indicators
            $(`#${messagesListId} .loading-more-messages`).remove();
            
            if (response.data && response.data.length > 0) {
                // Append older messages to the top
                appendOlderMessages(response.data, messagesListId);
                
                // Update the oldest message ID
                if (response.data.length > 0) {
                    const oldestMessage = [...response.data].sort((a, b) => a.id - b.id)[0];
                    window.oldestMessageId = oldestMessage.id;
                    console.log(`New oldest message ID: ${window.oldestMessageId}`);
                }
                
                // Store whether there might be more messages
                window.hasMoreMessages = response.data.length >= 15; // 15 is default limit
            } else {
                console.log("No more messages to load");
                window.hasMoreMessages = false;
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading more messages:', xhr.status, error);
            $(`#${messagesListId} .loading-more-messages`).remove();
            $(`#${messagesListId}`).prepend(
                '<div class="error-more-messages">Failed to load more messages. Try again later.</div>'
            );
            // Remove the error after 3 seconds
            setTimeout(() => {
                $(`#${messagesListId} .error-more-messages`).fadeOut(() => {
                    $(`#${messagesListId} .error-more-messages`).remove();
                });
            }, 3000);
        },
        complete: function() {
            // Reset loading flag
            window.isLoadingMore = false;
            console.log("Message loading complete, ready for more scrolling");
        }
    });
}

/**
 * Appends older messages to the top of the messages list
 * @param {Array} messages - The messages to append
 * @param {string} messagesListId - The ID of the messages list element
 */
function appendOlderMessages(messages, messagesListId = 'messages-list') {
    if (!messages || messages.length === 0) {
        return;
    }

    // Sort messages by id in ascending order (oldest first)
    const sortedMessages = [...messages].sort((a, b) => a.id - b.id);
    
    const messagesListEl = $(`#${messagesListId}`);
    
    // Get the current scroll position before adding content
    const messagesContainer = messagesListEl.closest('.messages-container');
    const oldScrollHeight = messagesContainer.prop('scrollHeight');
    const oldScrollTop = messagesContainer.scrollTop();
    
    let currentDate = '';
    let html = '';
    
    sortedMessages.forEach(message => {
        const date = new Date(message.createdAt);
        const messageDate = formatDate(date);
        const isMine = message.belongCurrentUser;
        const messageClass = isMine ? 'outgoing' : 'incoming';
        const timeString = formatTime(date);
        
        // Add date divider if this is a new date
        if (messageDate !== currentDate) {
            // Check if this date divider already exists
            const existingDivider = messagesListEl.find(`.date-divider span:contains("${messageDate}")`);
            if (existingDivider.length === 0) {
                html += `
                    <div class="date-divider">
                        <span>${messageDate}</span>
                    </div>
                `;
            }
            currentDate = messageDate;
        }
        
        // Avatar display
        const avatarHtml = !isMine ?
            `<div class="message-avatar">
                ${message.senderAvtUrl ?
                `<img src="${message.senderAvtUrl}" alt="${message.senderName || 'User'}" />` :
                `<div class="avatar-placeholder">${getAvatarInitial(message.senderName)}</div>`
            }
             </div>` : '';
        
        html += `
            <div class="message ${messageClass}">
                ${avatarHtml}
                <div class="message-bubble">
                    ${!isMine ? `<div class="message-sender">${message.senderName || 'User'}</div>` : ''}
                    <div class="message-content">${escapeHtml(message.content)}</div>
                    <div class="message-time">${timeString}</div>
                </div>
            </div>
        `;
    });
    
    // Prepend the HTML to the messages list
    messagesListEl.prepend(html);
    
    // Maintain scroll position after adding content
    const newScrollHeight = messagesContainer.prop('scrollHeight');
    messagesContainer.scrollTop(oldScrollTop + (newScrollHeight - oldScrollHeight));
}

/**
 * Sets up a scroll handler to load more messages when scrolling to the top
 * @param {string} messagesListId - The ID of the messages list element
 */
function setupScrollHandler(messagesListId = 'messages-list') {
    const messagesContainer = $(`#${messagesListId}`).closest('.messages-container');
    
    // Remove any existing scroll handlers first
    messagesContainer.off('scroll.loadMore');
    
    // Add scroll handler
    messagesContainer.on('scroll.loadMore', function() {
        // If we're near the top (within 50px) and we have more messages to load
        if (messagesContainer.scrollTop() < 50 && window.hasMoreMessages && !window.isLoadingMore) {
            console.log("Near top of container, loading more messages...");
            
            // Load more messages, starting from the oldest message ID
            if (window.currentConversationId && window.oldestMessageId) {
                loadMoreMessages(window.currentConversationId, window.oldestMessageId, messagesListId);
            } else {
                console.error("Cannot load more messages: missing conversation ID or oldest message ID");
            }
        }
    });

    // Debug logging for scroll position
    messagesContainer.on('scroll', function() {
        console.log(`Scroll position: ${messagesContainer.scrollTop()}, Container height: ${messagesContainer.height()}, ScrollHeight: ${messagesContainer.prop('scrollHeight')}`);
    });
}

// Make functions available globally
window.sendMessage = sendMessage;
window.updateLatestMessage = updateLatestMessage;
window.loadMoreMessages = loadMoreMessages;
window.setupScrollHandler = setupScrollHandler;

// Make sendMessage function available - it seems to be missing
function sendMessage(conversationId, stompClient, messageInputId = 'message-input') {
    const messageInput = $(`#${messageInputId}`);
    const content = messageInput.val().trim();
    
    if (!content) return; // Don't send empty messages
    
    if (!stompClient || !stompClient.connected) {
        console.error("WebSocket connection not available");
        return;
    }
    
    const message = {
        conversationId: conversationId,
        content: content,
        type: 'TEXT'
    };
    
    console.log('Sending message via WebSocket:', message);
    
    // Include auth token in headers
    const headers = {
        'Authorization': 'Bearer ' + localStorage.getItem('access_token')
    };
    
    stompClient.send("/app/chat.send", headers, JSON.stringify(message));
    
    // Clear input field
    messageInput.val('');
}

// Add missing updateLatestMessage function
function updateLatestMessage(conversationId, message) {
    const conversationElement = $(`.conversation-item[data-id="${conversationId}"]`);
    if (conversationElement.length === 0) return;
    
    // Update latest message text
    const latestMessageEl = conversationElement.find('.conversation-latest-message');
    if (latestMessageEl.length) {
        latestMessageEl.text(message.content);
    }
    
    // Update timestamp
    const timeEl = conversationElement.find('.conversation-time');
    if (timeEl.length) {
        const date = new Date(message.createdAt || new Date());
        timeEl.text(formatTime(date));
    }
    
    // Move conversation to top of list
    moveConversationToTop(conversationId);
}

// Define moveConversationToTop if not elsewhere defined
function moveConversationToTop(conversationId) {
    const conversationElement = $(`.conversation-item[data-id="${conversationId}"]`);
    if (conversationElement.length === 0) return;
    
    const conversationsList = conversationElement.parent();
    // Detach and prepend the conversation to move it to the top
    conversationElement.detach();
    conversationsList.prepend(conversationElement);
}