<#-- @ftlvariable name="_csrf" type="org.springframework.security.web.csrf.CsrfToken" -->
<!DOCTYPE html>
<html lang="zh">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="robots" content="noindex, nofollow">

    <title>Spring Security 课堂演示：登录界面</title>
    <link href="/css/login.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css?family=Muli" rel="stylesheet"/>
</head>

<body>
<div class="login-content" style="background-image: url(/img/background.svg);">
    <div class="box">
        <div>
            <img style="width: 20%" class="logo" src="/css-logo.png" alt="Alfresco">
        </div>
        <div class="box-container">
            <div>
                <p class="application-name" style="margin-left: 97px">管理信息系统-后台</p>
            </div>
            <div>
                <form id="kc-form-login" class="form" action="/admin/login_backend" method="post">
                    <#-- 加入CSRF令牌，Spring Security提供。 -->
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                    <label for="username"></label><input id="username" class="login-field" placeholder="用户名" type="text"
                                                         name="username" tabindex="1">
                    <div>
                        <label class="visibility" id="v"><img id="vi" src="/img/eye-off.png" alt="eye"></label>
                    </div>
                    <label for="password"></label><input id="password" class="login-field" placeholder="密码"
                                                         type="password" name="password" tabindex="2">
                    <input class="submit" type="submit" value="登录" tabindex="3">
                </form>
            </div>
            <div>
                <p class="copyright">&nbsp;</p>
            </div>
        </div>
    </div>
</div>
<script>
    document.getElementById("v").addEventListener("click", () => {
        const x = document.getElementById("password");
        const v = document.getElementById("vi");
        if (x.type === "password") {
            x.type = "text";
            v.src = "/img/eye.png";
        } else {
            x.type = "password";
            v.src = "/img/eye-off.png";
        }
    });
</script>
</body>
</html>
