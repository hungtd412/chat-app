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
        
        // Clear unread badge when conversation is clicked
        clearUnreadBadge($(this));
        
        // Delegate loading the chat component to chat.js
        loadChatInContainer(
            $('#chat-container'), 
            conversationId, 
            conversationType, 
            displayName,
            stompClient
        );
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
// Store conversations locally
let userConversations = [];

// Track subscriptions to avoid duplicates
window.activeSubscriptions = window.activeSubscriptions || {};

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

function onMessageReceived(payload) {
    try {
        const message = JSON.parse(payload.body);
        console.log('Received message in main-screen:', message);
        
        const conversationId = message.conversationId;
        
        // Check if we're currently viewing this conversation
        const activeConversationId = $('#chat-frame').data('conversation-id');
        
        if (activeConversationId && activeConversationId === conversationId) {
            // If we're viewing this conversation, delegate message handling to chat.js
            handleIncomingMessage(payload, conversationId);
        } else if (!message.belongCurrentUser) {
            // Only increment unread count for messages we didn't send
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
    console.log("updateUnreadCount for conversation", conversationId);
    // Find the conversation item in the list
    const conversationItem = $(`.conversation-item[data-id="${conversationId}"]`);
    
    if (conversationItem.length > 0) {
        // Check if there's already an unread badge
        let unreadBadge = conversationItem.find('.unread-badge');
        
        if (unreadBadge.length === 0) {
            // Create badge if it doesn't exist
            conversationItem.find('.conversation-info').append('<div class="unread-badge">1</div>');
            
            // Move the conversation to the top of the list if not already there
            const conversationList = $('#conversation-list');
            if (conversationItem.index() > 0) {
                conversationList.prepend(conversationItem);
            }
        } else {
            // Update existing badge
            const currentCount = parseInt(unreadBadge.text()) || 0;
            unreadBadge.text(currentCount + 1);
        }
    } else {
        // If conversation not in the list (e.g., a new conversation), refresh the conversation list
        loadConversations();
    }
    
    // Update total unread count in page title
    updatePageTitle();
}

/**
 * Clears the unread badge for a conversation
 * @param {JQuery} conversationItem - The conversation item element
 */
function clearUnreadBadge(conversationItem) {
    const unreadBadge = conversationItem.find('.unread-badge');
    if (unreadBadge.length > 0) {
        unreadBadge.remove();
        updatePageTitle();
    }
}

/**
 * Updates the page title with total unread messages count
 */
function updatePageTitle() {
    let totalUnread = 0;
    
    // Count all unread messages across conversations
    $('.unread-badge').each(function() {
        totalUnread += parseInt($(this).text()) || 0;
    });
    
    // Update page title
    if (totalUnread > 0) {
        document.title = `(${totalUnread}) Chat Application`;
    } else {
        document.title = 'Chat Application';
    }
}

function loadConversations() {
    // If we already have conversations loaded, use them
    if (userConversations.length > 0) {
        displayConversations(userConversations);
        return;
    }

    // Otherwise fetch conversations
    $.ajax({
        url: 'http://localhost:9000/conversations',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            // Store conversations in local variable
            userConversations = response.data || [];
            displayConversations(userConversations);
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
                    <!-- The unread badge will be added here when needed -->
                </div>
            </div>
        `;

        conversationList.append(conversationItem);
    });
    
    // Check if there are any stored unread counts to restore
    restoreUnreadCounts();
}

/**
 * Store unread counts in session storage before page refresh/navigation
 */
function storeUnreadCounts() {
    const unreadCounts = {};
    
    $('.conversation-item').each(function() {
        const id = $(this).data('id');
        const badge = $(this).find('.unread-badge');
        if (badge.length > 0) {
            unreadCounts[id] = badge.text();
        }
    });
    
    if (Object.keys(unreadCounts).length > 0) {
        sessionStorage.setItem('unreadCounts', JSON.stringify(unreadCounts));
    }
}

/**
 * Restore unread counts from session storage after page load
 */
function restoreUnreadCounts() {
    const storedCounts = sessionStorage.getItem('unreadCounts');
    if (storedCounts) {
        try {
            const unreadCounts = JSON.parse(storedCounts);
            
            for (const [id, count] of Object.entries(unreadCounts)) {
                const conversationItem = $(`.conversation-item[data-id="${id}"]`);
                if (conversationItem.length > 0) {
                    conversationItem.find('.conversation-info').append(`<div class="unread-badge">${count}</div>`);
                    conversationItem.addClass('unread');
                }
            }
            
            // Update the page title
            updatePageTitle();
            
        } catch (e) {
            console.error('Error restoring unread counts:', e);
        }
    }
}

// Store unread counts when page is unloaded
$(window).on('beforeunload', function() {
    storeUnreadCounts();
});

// Add this to the ready function
$(document).ready(function() {
    // ...existing code...
    
    // Make unread counts persist across page refreshes
    window.addEventListener('unload', storeUnreadCounts);
});