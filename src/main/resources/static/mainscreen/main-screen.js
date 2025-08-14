$(document).ready(function() {
    // Check if user is logged in by verifying both tokens in localStorage
    const accessToken = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');

    if (!accessToken || !refreshToken) {
        window.location.href = '../signin/signin.html';
        return;
    }

    // Load chat-list component into sidebar
    $('#sidebar-container').load('../chat/chat-list.html', function() {
        console.log('Chat list component loaded');
        // After loading the component, initialize conversations
        loadConversations().then(() => {
            // Check if there's a conversation ID in the URL
            const urlParams = new URLSearchParams(window.location.search);
            const conversationId = urlParams.get('conversation');

            if (conversationId) {
                // Find the conversation item and simulate a click
                const conversationItem = $(`.conversation-item[data-id="${conversationId}"]`);
                if (conversationItem.length > 0) {
                    console.log(`Opening conversation ${conversationId} from URL parameter`);
                    conversationItem.click();
                } else {
                    console.log(`Conversation ${conversationId} not found in list, will load directly`);
                    // If we couldn't find the conversation in the list, load it directly
                    loadConversationDetails(conversationId);
                }
            }
        });
        connectToWebSocket();
    });

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

        // Update URL with conversation ID without reloading the page
        const url = new URL(window.location.href);
        url.searchParams.set('conversation', conversationId);
        window.history.pushState({ conversationId }, '', url);

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

    // Logout button click handler
    $('#logout-button').click(function() {
        handleLogout();
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

        // Get userId from token
        const userId = getUserIdFromToken(token);

        if (userId) {
            // Subscribe to personal queue for private messages using userId
            stompClient.subscribe(`/user11/${userId}/queue/messages`, onMessageReceived);
            console.log(`Subscribed to /user11/${userId}/queue/messages`);

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

        // Move conversation to top regardless of whether it's active or not
        moveConversationToTop(conversationId);

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

// Make loadConversations return a promise so we can chain operations
function loadConversations() {
    return new Promise((resolve, reject) => {
        // If we already have conversations loaded, use them
        if (userConversations.length > 0) {
            displayConversations(userConversations);
            resolve(userConversations);
            return;
        }

        // Otherwise fetch conversations
        $.ajax({
            url: 'http://localhost:9000/conversations/current-user',
            type: 'GET',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('access_token')
            },
            success: function(response) {
                // Store conversations in local variable
                userConversations = response.data || [];
                displayConversations(userConversations);
                resolve(userConversations);
            },
            error: function(xhr, status, error) {
                console.error('Error loading conversations:', error);
                $('#conversation-list').html('<div class="error">Error loading conversations. Please try again later.</div>');
                reject(error);
            }
        });
    });
}

// Function to load conversation details when not in the list
function loadConversationDetails(conversationId) {
    $.ajax({
        url: `http://localhost:9000/conversations/${conversationId}`,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            if (response.data) {
                const conversation = response.data;

                // Update URL with conversation ID without reloading the page
                const url = new URL(window.location.href);
                url.searchParams.set('conversation', conversationId);
                window.history.pushState({ conversationId }, '', url);

                // Delegate loading the chat component
                loadChatInContainer(
                    $('#chat-container'),
                    conversation.id,
                    conversation.type,
                    conversation.type === 'PRIVATE' ? conversation.friendName : conversation.title,
                    stompClient
                );
            }
        },
        error: function(xhr, status, error) {
            console.error(`Error loading conversation ${conversationId}:`, error);
            $('#chat-container').html('<div class="error">Could not load conversation. Please try again later.</div>');
        }
    });
}

function displayConversations(conversations) {
    // Check if the function exists and call it directly
    if (window.displayConversationList) {
        window.displayConversationList(conversations);
    } else {
        console.error('displayConversationList function not found');
    }
}

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

        // Move the conversation to the top when sending a message too
        moveConversationToTop(conversationId);

        stompClient.send("/app/chat.send", headers, JSON.stringify(message));
    } else {
        console.error('WebSocket not connected, cannot send message');
        alert('Connection error. Please refresh the page and try again.');
        messageInput.val(messageText); // Restore the message text
    }
}

function handleLogout() {
    console.log("Logout button clicked");
    const accessToken = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');

    // Disconnect WebSocket before logout
    if (window.stompClient && window.stompClient.connected) {
        window.stompClient.disconnect();
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
}