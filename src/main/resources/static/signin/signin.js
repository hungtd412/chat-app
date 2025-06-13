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
                localStorage.setItem('jwt_token', response.token);
                alert('Login successful!');
                window.location.href = '/chat.html';
            },
            error: function (xhr, status, error) {
                console.error('Login error:', xhr.responseJSON || error);
                $('#error-message').text(xhr.responseJSON?.message).show();
            }
        });
    });
});
