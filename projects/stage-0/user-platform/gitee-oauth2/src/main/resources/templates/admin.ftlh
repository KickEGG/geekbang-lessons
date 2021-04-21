<#-- @ftlvariable name="_csrf" type="org.springframework.security.web.csrf.CsrfToken" -->
<h1>这是后台</h1>
<#--noinspection HtmlUnknownTarget-->
<form action="/admin/logout" method="post">
    <input class="submit" type="submit" value="退出登录">
    <#-- 加入CSRF令牌，Spring Security提供。 -->
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
</form>
