<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>View Image</title>
</head>
<body>
    <h1>Uploaded Image</h1>
    <% InputStream imageStream = (InputStream) request.getAttribute("imageStream"); %>
    <img src="data:image/jpeg;base64, <%= new sun.misc.BASE64Encoder().encode(imageStream.readAllBytes()) %>" />
</body>
</html>
