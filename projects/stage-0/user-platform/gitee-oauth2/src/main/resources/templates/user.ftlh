<#-- @ftlvariable name="_csrf" type="org.springframework.security.web.csrf.CsrfToken" -->
<h1>这是前台</h1>
<#--noinspection HtmlUnknownTarget-->
<div>
    用户:${userName}登录成功
<div>
<form action="/user/logout" method="post">
    <input class="submit" type="submit" value="退出登录">
    <#-- 加入CSRF令牌，Spring Security提供。 -->
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
</form>
