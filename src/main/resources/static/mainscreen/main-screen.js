$(document).ready(function() {
    // Check if user is logged in by verifying both tokens in localStorage
    const accessToken = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token');
    
    if (!accessToken || !refreshToken) {
        window.location.href = '../signin/signin.html';
        return;
    }

    loadConversations();

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
        const isPrivate = $(this).data('type') === 'PRIVATE';
        
        // Navigate to the appropriate chat window based on conversation type
        if (isPrivate) {
            window.location.href = `../chat/private-chat.html?id=${conversationId}`;
        } else {
            // For group chats, load in the current view
            loadConversationMessages(conversationId);
        }
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
});

function loadConversations() {
    $.ajax({
        url: 'http://localhost:9000/conversations',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        success: function(response) {
            displayConversations(response.data);
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
                </div>
            </div>
        `;

        conversationList.append(conversationItem);
    });
}

function loadConversationMessages(conversationId) {
    // Clear the chat container
    const chatContainer = $('#chat-container');
    chatContainer.html('<div class="loading">Loading messages...</div>');
    
    // In a real implementation, you would load messages from the server here
    // For now, just display a placeholder
    setTimeout(() => {
        chatContainer.html(`
            <div class="chat-header">
                <h3>Conversation #${conversationId}</h3>
            </div>
            <div class="messages-container">
                <div class="messages-list">
                    <div class="message-timestamp">Today</div>
                    <div class="message received">
                        <div class="message-content">Hello! This is a placeholder message.</div>
                        <div class="message-time">10:30 AM</div>
                    </div>
                    <div class="message sent">
                        <div class="message-content">This is just a demo. Real messaging will be implemented soon!</div>
                        <div class="message-time">10:32 AM</div>
                    </div>
                </div>
            </div>
            <div class="message-input-container">
                <input type="text" id="message-input" placeholder="Type a message...">
                <button id="send-message-btn">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        `);
    }, 1000);
}

function getAvatarInitial(name) {
    if (!name || name === '') {
        return '?';
    }
    const initials = name.split(' ').map(part => part[0]).join('');
    return initials.substring(0, 2).toUpperCase();
}