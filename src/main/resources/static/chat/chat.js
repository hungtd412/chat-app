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
        
        // Load conversation details and messages
        loadConversationDetails(conversationId);
        loadMessages(conversationId);

        // Handle back button
        $('#back-button').click(function() {
            window.location.href = '../mainscreen/main-screen.html';
        });

        // Handle send message - use shared function from chat-utils.js
        $('#send-button').click(function() {
            sendMessage(conversationId, window.stompClient, 'message-input');
        });

        // Send message on Enter key - use shared function from chat-utils.js
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
}

// Track subscriptions to avoid duplicates
window.activeSubscriptions = window.activeSubscriptions || {};

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

/**
 * Loads a chat component into the specified container
 * 
 * @param {jQuery} container - The jQuery element to load the chat into
 * @param {number} conversationId - The ID of the conversation
 * @param {string} conversationType - The type of conversation (PRIVATE or GROUP)
 * @param {string} displayName - The name to display in the chat header
 * @param {object} stompClientInstance - The STOMP client instance
 */
function loadChatInContainer(container, conversationId, conversationType, displayName, stompClientInstance) {
    // Clear the chat container and show loading
    container.html('<div class="loading">Loading chat...</div>');

    // Load the chat component HTML template
    $.get('../chat/chat.html', function(template) {
        // Replace the chat container with the loaded template
        container.html(template);

        // Set data attributes for the chat frame
        $('#chat-frame').attr('data-conversation-id', conversationId);
        $('#chat-frame').attr('data-conversation-type', conversationType);

        // Set initial UI content
        $('#chat-title').text(displayName);
        $('#status-text').text(conversationType === 'PRIVATE' ? 'Online' : 'Group chat');

        // Remove back button when in main screen
        $('#back-button').hide();

        // Setup event handlers for send button - using shared function from chat-utils.js
        $('#send-button').attr('data-conversation-id', conversationId);
        $('#send-button').click(function() {
            sendMessage(conversationId, stompClientInstance, 'message-input');
        });

        // Setup event handler for enter key in message input - using shared function from chat-utils.js
        $('#message-input').keypress(function(e) {
            if (e.which === 13) { // Enter key
                sendMessage(conversationId, stompClientInstance, 'message-input');
            }
        });

        // Initialize the chat component
        initializeChat(conversationId, conversationType);
    })
    .fail(function(xhr, status, error) {
        console.error('Error loading chat template:', error);
        container.html(`
            <div class="error-container">
                <div class="error-icon">
                    <i class="fas fa-exclamation-circle"></i>
                </div>
                <h3>Cannot render the conversation</h3>
                <p>There was a problem loading the chat interface. Please try again later.</p>
                <button class="retry-button" onclick="loadChatInContainer($('#chat-container'), '${conversationId}', '${conversationType}', '${displayName}', window.stompClient)">
                    <i class="fas fa-sync"></i> Retry
                </button>
            </div>
        `);
    });
}

// Remove duplicate sendMessage function - it's now defined in chat-utils.js