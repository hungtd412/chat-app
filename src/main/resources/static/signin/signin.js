$(document).ready(function () {
    $('#login-button').click(function () {
        const username = $('#username').val();
        const password = $('#password').val();

        if (!username || !password) {
            $('#error-message').text('Please fill in all fields').show();
            return;
        }

        $.ajax({
            url: 'http://localhost:9000/auth/log-in',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ username, password }),
            success: function (response) {
                console.log('Login successful:', response);
                // Store both tokens from the response
                localStorage.setItem('access_token', response.data.accessToken);
                localStorage.setItem('refresh_token', response.data.refreshToken);
                // Redirect to main-screen.html instead of chat-list.html
                window.location.href = '../mainscreen/main-screen.html';
            },
            error: function (xhr, status, error) {
                console.error('Login error:', xhr.responseJSON || error);
                $('#error-message').text(xhr.responseJSON?.message || 'Login failed').show();
            }
        });
    });
});
