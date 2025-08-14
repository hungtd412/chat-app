// We only run the document.ready function when this script is loaded directly,
// not when it's included as a component in main-screen
if (window.location.pathname.includes('chat-list.html')) {
    $(document).ready(function() {
        // Check if user is logged in by verifying token in localStorage
        const token = localStorage.getItem('access_token');
        const refreshToken = localStorage.getItem('refresh_token');
        if (!token || !refreshToken) {
            window.location.href = '../signin/signin.html';
            return;
        }

        loadConversations();

        // Add logout button for standalone mode only
        $('.sidebar').append(`
            <div class="sidebar-footer">
                <button id="standalone-logout-button">
                    <i class="fas fa-sign-out-alt"></i>
                    Logout
                </button>
            </div>
        `);

        // Logout button handler for standalone mode
        $(document).on('click', '#standalone-logout-button', function() {
            // Call the logout function in parent window if it exists
            if (window.parent && typeof window.parent.handleLogout === 'function') {
                window.parent.handleLogout();
            } else {
                // Fallback to basic token removal and redirect
                localStorage.removeItem('access_token');
                localStorage.removeItem('refresh_token');
                window.location.href = '../signin/signin.html';
            }
        });
    });
}

// Search functionality - this will work both standalone and as component
$(document).on('input', '#search-input', function() {
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

// New conversation button click handler
$(document).on('click', '#new-conversation-btn', function() {
    // Navigate to new conversation page or show modal
    alert('New conversation feature coming soon!');
});

// Functions for managing conversations
function loadConversations() {
    $.ajax({
        url: 'http://localhost:9000/conversations/current-user',
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

// Expose this function so main-screen.js can use it
window.displayConversationList = function(conversations) {
    // Call the local function directly instead of calling displayConversations
    // which would create an infinite loop with main-screen.js
    renderConversations(conversations);
};

// Rename the function to avoid conflicts
function displayConversations(conversations) {
    renderConversations(conversations);
}

// Actual implementation function to render conversations
function renderConversations(conversations) {
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
        const hasAvatarUrl = (isGroup ? conversation.imageUrl : conversation.avtUrl) &&
            (isGroup ? conversation.imageUrl.trim() !== '' : conversation.avtUrl.trim() !== '');

        // Create avatar content based on whether an image URL is available
        let avatarContent = '';
        if (hasAvatarUrl) {
            const imageUrl = isGroup ? conversation.imageUrl : conversation.avtUrl;
            avatarContent = `<img src="${imageUrl}" alt="${displayName}" class="avatar-img">`;
        } else {
            avatarContent = getAvatarInitial(displayName);
        }

        // Format the timestamp
        let formattedTime = '';
        if (conversation.createdAt) {
            formattedTime = formatTimestamp(conversation.createdAt);
        }

        // Prepare the latest message text
        const latestMessage = conversation.latestMessage || 'No messages yet';

        const conversationItem = `
            <div class="conversation-item" data-id="${conversation.id}" data-type="${conversation.type}">
                <div class="${avatarClass}">
                    ${avatarContent}
                </div>
                <div class="conversation-info">
                    <div class="conversation-name">${displayName}</div>
                    <div class="conversation-meta">
                        <div class="latest-message">${latestMessage}</div>
                        <div class="message-time">${formattedTime}</div>
                    </div>
                </div>
            </div>
        `;

        conversationList.append(conversationItem);
    });

    // Check if there are any stored unread counts to restore
    restoreUnreadCounts();
}

/**
 * Formats a timestamp into a readable format
 * @param {string} timestamp - The timestamp to format
 * @returns {string} The formatted timestamp
 */
function formatTimestamp(timestamp) {
    if (!timestamp) return '';

    const date = new Date(timestamp);
    const now = new Date();

    // Check if valid date
    if (isNaN(date.getTime())) return '';

    // Today
    if (date.toDateString() === now.toDateString()) {
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    // Yesterday
    const yesterday = new Date(now);
    yesterday.setDate(now.getDate() - 1);
    if (date.toDateString() === yesterday.toDateString()) {
        return 'Yesterday';
    }

    // This week (within 7 days)
    const oneWeekAgo = new Date(now);
    oneWeekAgo.setDate(now.getDate() - 7);
    if (date > oneWeekAgo) {
        const options = { weekday: 'short' };
        return date.toLocaleDateString([], options);
    }

    // This year
    if (date.getFullYear() === now.getFullYear()) {
        const options = { month: 'short', day: 'numeric' };
        return date.toLocaleDateString([], options);
    }

    // Older
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return date.toLocaleDateString([], options);
}

/**
 * Moves a conversation to the top of the conversation list
 * @param {number} conversationId - The ID of the conversation to move
 */
function moveConversationToTop(conversationId) {
    const conversationItem = $(`.conversation-item[data-id="${conversationId}"]`);
    if (conversationItem.length > 0) {
        const conversationList = $('#conversation-list');
        // Only move if it's not already at the top
        if (conversationItem.index() > 0) {
            // Clone the conversation item to preserve event handlers
            const clonedItem = conversationItem.clone(true);
            // Remove the original
            conversationItem.remove();
            // Prepend the clone to the list (add to top)
            conversationList.prepend(clonedItem);
            console.log(`Moved conversation ${conversationId} to the top`);
        }
    }
}
// Expose this function to the window so main-screen.js can use it
window.moveConversationToTop = moveConversationToTop;

/**
 * Updates the unread count badge for a conversation
 * @param {number} conversationId - The ID of the conversation to update
 */
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
// Expose this function to the window so main-screen.js can use it
window.updateUnreadCount = updateUnreadCount;

/**
 * Clears the unread badge from a conversation
 * @param {jQuery} conversationItem - The conversation item element
 */
function clearUnreadBadge(conversationItem) {
    const unreadBadge = conversationItem.find('.unread-badge');
    if (unreadBadge.length > 0) {
        const conversationId = conversationItem.data('id');
        unreadBadge.remove();
        updatePageTitle();

        // Remove this conversation from stored unread counts in session storage
        const storedCounts = sessionStorage.getItem('unreadCounts');
        if (storedCounts) {
            try {
                const unreadCounts = JSON.parse(storedCounts);
                if (unreadCounts[conversationId]) {
                    delete unreadCounts[conversationId];
                    sessionStorage.setItem('unreadCounts', JSON.stringify(unreadCounts));
                }
            } catch (e) {
                console.error('Error updating stored unread counts:', e);
            }
        }
    }
}
// Expose this function to the window so main-screen.js can use it
window.clearUnreadBadge = clearUnreadBadge;

/**
 * Updates the page title to display unread message count
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
// Expose this function to the window so main-screen.js can use it
window.updatePageTitle = updatePageTitle;

/**
 * Gets avatar initials from a name
 * @param {string} name - The name to get initials from
 * @returns {string} The initials (up to 2 characters)
 */
function getAvatarInitial(name) {
    if (!name || name === '') {
        return '?';
    }
    const initials = name.split(' ').map(part => part[0]).join('');
    return initials.substring(0, 2).toUpperCase();
}
// No need to expose this since it's only used within this file

/**
 * Stores unread counts in session storage
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
// Expose this function to the window so main-screen.js can use it
window.storeUnreadCounts = storeUnreadCounts;

/**
 * Restores unread counts from session storage
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
                }
            }

            // Update the page title
            updatePageTitle();

        } catch (e) {
            console.error('Error restoring unread counts:', e);
        }
    }
}
// Expose this function to the window so main-screen.js can use it
window.restoreUnreadCounts = restoreUnreadCounts;

// Set up event listener for page unload to store unread counts
$(window).on('beforeunload', function() {
    storeUnreadCounts();
});