$(document).ready(function() {
    // Check if user is logged in by verifying token in localStorage
    const token = localStorage.getItem('access_token');
    const refreshToken = localStorage.getItem('refresh_token'); // Also check refresh token
    if (!token || !refreshToken) {
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
            window.location.href = `private-chat.html?id=${conversationId}`;
        } else {
            // For group chats, use different page or handle differently
            window.location.href = `chat-window.html?id=${conversationId}`;
        }
    });

    // New conversation button click handler
    $('#new-conversation-btn').click(function() {
        // Navigate to new conversation page or show modal
        alert('New conversation feature coming soon!');
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
        const avatarClass = isGroup ? 'avatar group-avatar' : 'avatar';

        // Check if imageUrl exists for displaying avatar image
        const hasAvatarUrl = conversation.imageUrl && conversation.imageUrl.trim() !== '';

        // Create avatar content based on whether an image URL is available
        let avatarContent = '';
        if (hasAvatarUrl) {
            avatarContent = `<img src="${conversation.imageUrl}" alt="${displayName}" class="avatar-img">`;
        } else {
            avatarContent = getAvatarInitial(displayName);
        }

        const conversationItem = `
            <div class="conversation-item" data-id="${conversation.id}" data-type="${conversation.type}">
                <div class="${avatarClass}">
                    ${avatarContent}
                </div>
                <div class="conversation-info">
                    <div class="conversation-name">${displayName}</div>
                </div>
            </div>
        `;

        conversationList.append(conversationItem);
    });
}

function getAvatarInitial(name) {
    if (!name || name === '') {
        return '?';
    }
    const initials = name.split(' ').map(part => part[0]).join('');
    return initials.substring(0, 2).toUpperCase();
}