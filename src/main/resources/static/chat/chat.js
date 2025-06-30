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
    
    // Show/hide group edit buttons based on conversation type
    if (conversationType === 'GROUP') {
        $('#group-edit-buttons').show();
        setupGroupEditButtons(conversationId);
    } else {
        $('#group-edit-buttons').hide();
    }
}

// Set up event handlers for group edit buttons
function setupGroupEditButtons(conversationId) {
    // Edit group title button
    $('#edit-group-title-btn').off('click').on('click', function() {
        // Get current title
        const currentTitle = $('#chat-title').text();
        $('#group-title-input').val(currentTitle);
        $('#edit-group-title-modal').show();
    });
    
    // Edit group image button
    $('#edit-group-image-btn').off('click').on('click', function() {
        $('#image-preview').empty();
        $('#edit-group-image-modal').show();
    });
    
    // Close modals when clicking the X
    $('.close-modal').off('click').on('click', function() {
        $(this).closest('.modal').hide();
    });
    
    // Close modals when clicking outside
    $('.modal').off('click').on('click', function(event) {
        if (event.target === this) {
            $(this).hide();
        }
    });
    
    // Preview selected image
    $('#group-image-input').off('change').on('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#image-preview').html(`<img src="${e.target.result}" alt="Preview">`);
            };
            reader.readAsDataURL(file);
        }
    });
    
    // Save group title
    $('#save-group-title-btn').off('click').on('click', function() {
        const newTitle = $('#group-title-input').val().trim();
        if (newTitle) {
            updateGroupTitle(conversationId, newTitle);
        }
    });
    
    // Save group image
    $('#save-group-image-btn').off('click').on('click', function() {
        const fileInput = document.getElementById('group-image-input');
        if (fileInput.files.length > 0) {
            const file = fileInput.files[0];
            updateGroupImage(conversationId, file);
        } else {
            alert('Please select an image first');
        }
    });
}

// Update group title via API
function updateGroupTitle(conversationId, newTitle) {
    // Show loading on the button
    showButtonLoading('#save-group-title-btn', 'Saving...');
    
    // Also show status message
    $('#title-loading-status').text('Updating group title...').show();
    
    $.ajax({
        url: `http://localhost:9000/conversations/${conversationId}/title`,
        type: 'PATCH',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
            'Content-Type': 'application/json'
        },
        data: JSON.stringify({
            title: newTitle
        }),
        success: function(response) {
            console.log('Group title updated successfully:', response);
            
            // Hide loading
            hideButtonLoading('#save-group-title-btn');
            
            // Update UI
            $('#chat-title').text(newTitle);
            $('#edit-group-title-modal').hide();
            
            // Show success message in the status area instead of a popup
            $('#title-loading-status').removeClass('error').addClass('success').text('Group title updated successfully!');
            
            // Hide the status message after 3 seconds
            setTimeout(function() {
                $('#title-loading-status').fadeOut();
            }, 3000);
        },
        error: function(xhr, status, error) {
            console.error('Error updating group title:', error);
            
            // Hide loading
            hideButtonLoading('#save-group-title-btn');
            
            // Show error in status area
            $('#title-loading-status').removeClass('success').addClass('error').text(xhr.responseJSON?.message || 'Failed to update group title');
            
            // Don't automatically hide error messages
        }
    });
}

// Update group image via API
function updateGroupImage(conversationId, imageFile) {
    // Create FormData object for file upload
    const formData = new FormData();
    formData.append('image', imageFile);
    
    // Show loading on the button
    showButtonLoading('#save-group-image-btn', 'Uploading...');
    
    // Also show status message
    $('#image-loading-status').text('Uploading image...').show();
    
    $.ajax({
        url: `http://localhost:9000/conversations/${conversationId}/image`,
        type: 'PATCH',
        data: formData,
        processData: false, // Don't process the data
        contentType: false, // Don't set content type (browser will set it with boundary)
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            console.log('Group image updated successfully:', response);
            
            // Hide loading
            hideButtonLoading('#save-group-image-btn');
            
            // Show success message in the status area instead of a popup
            $('#image-loading-status').removeClass('error').addClass('success').text('Group image updated successfully!');
            
            // Refresh the conversation list to show the updated image
            if (typeof window.loadConversations === 'function') {
                window.loadConversations();
            }
            
            // Hide the status message after 3 seconds, then close the modal
            setTimeout(function() {
                $('#image-loading-status').fadeOut();
                // After fading out the message, close the modal
                setTimeout(function() {
                    $('#edit-group-image-modal').hide();
                }, 300);
            }, 3000);
        },
        error: function(xhr, status, error) {
            console.error('Error updating group image:', error);
            
            // Hide loading
            hideButtonLoading('#save-group-image-btn');
            
            // Show error in status area
            $('#image-loading-status').removeClass('success').addClass('error').text(xhr.responseJSON?.message || 'Failed to upload image');
            
            // Don't automatically hide error messages
        }
    });
}

// Show notification toast message - modified to only handle errors
function showNotification(message, type = 'success') {
    // For success messages, just return (don't show any popup)
    if (type === 'success') {
        return;
    }
    
    // For errors, use existing error popup if available
    if (typeof window.showErrorPopup === 'function' && type === 'error') {
        window.showErrorPopup(message);
        return;
    }
    
    // Fallback error notification
    const toast = $(`<div class="notification-toast ${type}">${message}</div>`);
    $('body').append(toast);
    
    setTimeout(function() {
        toast.addClass('show');
        
        setTimeout(function() {
            toast.removeClass('show');
            setTimeout(function() {
                toast.remove();
            }, 300);
        }, 3000);
    }, 100);
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
        
        // Load the loading utility script
        $.getScript('../utils/loading-util.js', function() {
            console.log('Loading utility loaded successfully');
        });
        
        // Set data attributes for the chat frame
        $('#chat-frame').attr('data-conversation-id', conversationId);
        $('#chat-frame').attr('data-conversation-type', conversationType);

        // Set initial UI content
        $('#chat-title').text(displayName);
        $('#status-text').text(conversationType === 'PRIVATE' ? 'Online' : 'Group chat');

        // Remove back button when in main screen
        $('#back-button').hide();

        // Setup event handlers for send button
        $('#send-button').attr('data-conversation-id', conversationId);
        $('#send-button').click(function() {
            sendMessage(conversationId, stompClientInstance, 'message-input');
        });

        // Setup event handler for enter key in message input
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