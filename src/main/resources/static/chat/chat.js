// Main chat functionality
$(document).ready(function() {
    // Check if user is logged in
    const token = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');
    if (!token || !refreshToken) {
        window.location.href = '../signin/signin.html';
        return;
    }
    console.log("page loading!")
    // Get conversation ID from URL parameter (if in standalone mode)
    const urlParams = new URLSearchParams(window.location.search);
    const conversationId = urlParams.get('id');
    
    if (conversationId) {
        // We're in standalone mode
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
            sendMessage(conversationId, window.stompClient, 'message-input');
        });

        // Send message on Enter key
        $('#message-input').keypress(function(e) {
            if (e.which === 13) { // Enter key
                sendMessage(conversationId, window.stompClient, 'message-input');
            }
        });
    }
});

// Functions exported for use in main-screen.js
function initializeChat(conversationId, conversationType) {
    console.log('Initializing chat for conversation:', conversationId);
    
    // Store the conversation ID globally for message handling
    window.currentConversationId = conversationId;
    
    // Load messages for this conversation
    loadMessages(conversationId);
    
    // Find the chat elements - might be either send-button or send-message-btn
    const inputField = $('#message-input');
    const sendButton = $('#send-button').length ? $('#send-button') : $('#send-message-btn');
    
    console.log('Input field exists:', inputField.length > 0);
    console.log('Send button exists:', sendButton.length > 0);
    
    // Subscribe to the specific conversation topic for group chats
    if (conversationType === 'GROUP' && window.stompClient && window.stompClient.connected) {
        subscribeToGroupChat(conversationId);
    }
}

// Track subscriptions to avoid duplicates
window.activeSubscriptions = window.activeSubscriptions || {};

function subscribeToGroupChat(conversationId) {
    if (!window.stompClient || !window.stompClient.connected) return;
    
    // Check if we're already subscribed to this topic
    const topicId = `/topic/conversation.${conversationId}`;
    if (window.activeSubscriptions[topicId]) {
        console.log(`Already subscribed to topic: ${topicId}`);
        return;
    }
    
    // Subscribe and store the subscription
    const subscription = window.stompClient.subscribe(topicId, function(payload) {
        handleIncomingMessage(payload, conversationId);
    });
    
    // Store subscription reference for future checks
    window.activeSubscriptions[topicId] = subscription;
    console.log(`Subscribed to topic for conversation ${conversationId}`);
}

function handleIncomingMessage(payload, currentConversationId) {
    console.log('Message received via WebSocket:', payload);
    
    try {
        const message = JSON.parse(payload.body);
        console.log('Parsed message:', message);
        
        // Get the currently active conversation ID
        const activeConversationId = window.currentConversationId || 
                                    $('#chat-frame').data('conversation-id');
        
        console.log('Active conversation:', activeConversationId);
        console.log('Message conversation:', message.conversationId);
        
        // Only process message if it's for the current conversation
        if (message.conversationId && message.conversationId == activeConversationId) {
            console.log('Appending message to current conversation');
            appendNewMessage(message);
        } else {
            console.log('Message is for a different conversation');
        }
    } catch (error) {
        console.error('Error processing received message:', error);
    }
}

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
        const conversationId = getConversationIdFromUI();

        if (username) {
            // Check if we're already subscribed to the user queue
            const queueId = `/user/${username}/queue/messages`;
            if (!window.activeSubscriptions[queueId]) {
                // Subscribe to the user's private queue using username
                const subscription = window.stompClient.subscribe(queueId, function(payload) {
                    handleIncomingMessage(payload, conversationId);
                });
                
                // Store subscription reference
                window.activeSubscriptions[queueId] = subscription;
                console.log(`Subscribed to personal queue for user ${username}`);
            } else {
                console.log(`Already subscribed to personal queue: ${queueId}`);
            }
        }
    
        // Subscribe to the conversation topic (for group chats)
        if (conversationId) {
            subscribeToGroupChat(conversationId);
        }
    } catch (e) {
        console.error('Error subscribing to WebSocket topics:', e);
    }
}

function getConversationIdFromUI() {
    // Get conversation ID from URL parameter or from the UI element
    const urlParams = new URLSearchParams(window.location.search);
    const conversationIdFromURL = urlParams.get('id');
    
    if (conversationIdFromURL) {
        return conversationIdFromURL;
    }
    
    const chatFrame = $('#chat-frame');
    return chatFrame.length > 0 ? chatFrame.data('conversation-id') : null;
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