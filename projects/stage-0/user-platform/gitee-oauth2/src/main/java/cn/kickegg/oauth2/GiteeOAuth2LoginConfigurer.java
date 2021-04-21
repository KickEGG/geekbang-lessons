package cn.edu.dgut.css.sai.springsecuritygiteeexperiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.core.io.Resource;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Gitee码云OAuth2认证SecurityConfigurer
 *
 * @author Sai
 * @since 2020-5-10
 */
@SuppressWarnings("JavadocReference")
public class GiteeOAuth2LoginConfigurer<H extends HttpSecurityBuilder<H>> extends
        AbstractHttpConfigurer<GiteeOAuth2LoginConfigurer<H>, H> {

    ///////////////////////////////////////////////////
    /// 步骤一：创建接入码云的应用
    /// 参考：https://gitee.com/api/v5/oauth_doc#/list-item-3
    static final String CLIENT_ID = "3068627a904f5595362a180f242e4f603142fdbc1407599a25231c9571cb35c4";
    static final String CLIENT_SECRET = "7664a61c7b88560c96ccbbd145ef84a552888ab859c5065990e3b5ac667c7286";
    ///////////////////////////////////////////////////
    protected  final    Logger logger= LoggerFactory.getLogger(GiteeOAuth2LoginConfigurer.class);
    /**
     * 自定义安全过滤链
     *
     * @see HttpSecurityBuilder#addFilterBefore(Filter, Class)
     * @see HttpSecurityBuilder#addFilterAfter(Filter, Class)
     * @see SecurityConfigurerAdapter#postProcess(Object)
     * @see FilterComparator
     */
    @Override
    public void configure(H http) {
        ////////////////////////////////////////////////////////////////
        /// 步骤五：把自定义的两个Filter加进安全过滤链
        /// 注意：不要加在SecurityContextPersistenceFilter前面就行。
        http.addFilterAfter((GiteeOAuth2RedirectFilter)this.postProcess(new GiteeOAuth2RedirectFilter()), SecurityContextPersistenceFilter.class);
        http.addFilterAfter((GiteeOAuth2LoginAuthenticationFilter)this.postProcess(new GiteeOAuth2LoginAuthenticationFilter()),SecurityContextPersistenceFilter.class);
        ////////////////////////////////////////////////////////////////
    }

    /**
     * 重定向过滤器
     * <p></p>
     * 应用程序通过浏览器或Webview将用户引导到码云三方认证页面上（ GET请求 ）
     */
    static class GiteeOAuth2RedirectFilter extends OncePerRequestFilter {
        private static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/gitee";

        private static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/gitee";

        /**
         * 自定义重定向过滤器的过滤逻辑。
         * <p></p>
         * 测试：http://localhost:8080/oauth2/gitee
         * <p></p>
         * @see UriComponentsBuilder
         * @see UriComponentsBuilder#buildAndExpand(Object...)
         * @see HttpServletResponse#sendRedirect(String)
         */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            // 如果不是/oauth2/gitee请求，则继续下一个过滤器。
            if (!request.getRequestURI().endsWith(DEFAULT_AUTHORIZATION_REQUEST_BASE_URI)) {
                filterChain.doFilter(request, response);
                return;// 执行完安全过滤器链后不执行后面的代码。
            }


            // 应用通过 浏览器 或 Webview 将用户引导到码云三方认证页面上（ GET请求 ）
            // 重定向地址：https://gitee.com/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code
            /// 步骤二：编写重定向过滤器的业务逻辑。
            /// 当用户访问/oauth2/gitee时，本重定向过滤器拦截请求，并将用户重定向到码云三方认证页面上。
            URI uri=UriComponentsBuilder
                    .fromUriString("https://gitee.com/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code")
                    .build(CLIENT_ID,REDIRECT_URI,"code");
            String redirectUrl = uri.toString();
            logger.debug("redurectUrl:"+redirectUrl);
            new DefaultRedirectStrategy().sendRedirect(request,response,redirectUrl);
            //////////////////////////////////////////////////////////////
        }
    }

    /**
     * 码云OAuth2认证过滤器
     * <p></p>
     * 回调的url例子：http://localhost:8080/login/oauth2/code/gitee?code=d31aab384c54ba5e62cf51abc41252788a56a98643ba6ba4547c3e1ce420f896
     * <p>
     * 获取授权用户的资料：https://gitee.com/api/v5/swagger#/getV5User
     *
     * @see UsernamePasswordAuthenticationFilter
     * @see AbstractAuthenticationProcessingFilter#doFilter(ServletRequest, ServletResponse, FilterChain)
     * @see DaoAuthenticationProvider
     * @see JacksonJsonParser
     * @see UsernamePasswordAuthenticationToken
     * @see AbstractAuthenticationToken#getName()
     * @see DefaultRedirectStrategy 使用它构造重定向的http响应
     * @see ChangeSessionIdAuthenticationStrategy
     * @see SavedRequestAwareAuthenticationSuccessHandler
     */
    static class GiteeOAuth2LoginAuthenticationFilter extends OncePerRequestFilter {
        private static final String DEFAULT_CALLBACK_BASE_URI = "/login/oauth2/code/gitee";
        private static final String DEFAULT_LOGIN_SUCCESS_REDIRECT_URL = "/user";
        private static final ProviderManager providerManager = new ProviderManager(Collections.singletonList(new GiteeOAuth2AuthenticationProvider()));

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            // 检查请求是否‘/login/oauth2/code/gitee’和 是否有code参数。
            String code = request.getParameter("code");
            if (!request.getRequestURI().endsWith(DEFAULT_CALLBACK_BASE_URI) || StringUtils.isEmpty(code)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Spring Security基本校验流程
            GiteeOAuth2LoginAuthenticationToken giteeOAuth2LoginAuthenticationToken = new GiteeOAuth2LoginAuthenticationToken(code, request);
            Authentication successAuthentication;
            try {
                successAuthentication = providerManager.authenticate(giteeOAuth2LoginAuthenticationToken);
            } catch (AuthenticationException e) {
                // 如果校验失败providerManager会抛异常，在catch里作异常处理。
                // do somethings.
                return;
            }

            // 校验成功，执行后面的流程：
            SecurityContextHolder.getContext().setAuthentication(successAuthentication);
            // 改变 session id (只是改id,没有删除session)
            if(!request.getSession().getId().isEmpty()){
                request.changeSessionId();
            }
            // 移除之前认证时的错误信息
            request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            // 成功登录后，设置重定向到页面。
            redirectStrategy(request,response);
        }

        /**
         * 决定 Gitee 登录成功的重定向策略
         * <p></p>
         * 每条安全过滤链都默认添加了配置器{@link RequestCacheConfigurer} ，它会在安全过滤中添加了一个过滤器{@link RequestCacheAwareFilter}，
         * 它会缓存进入安全过滤链的HttpServletRequest对象，request实际是保存在当前session的SPRING_SECURITY_SAVED_REQUEST属性中的,详细查看{@link HttpSessionRequestCache}。
         * 当过滤链捕获到认证异常或访问拒绝异常时，ExceptionTranslationFilter就会缓存这次的HttpServletRequest对象，查看{@code ExceptionTranslationFilter#sendStartAuthentication}
         * {@link DefaultRedirectStrategy}工具类可以很方便帮助我们构造重定向的http响应。
         * <p></p>
         * Spring Security框架的默认的认证成功后的重定向逻辑由{@link SavedRequestAwareAuthenticationSuccessHandler}处理，
         * 本方法是根据它的逻辑写的，为了尽量与Spring Security兼容。
         * <p></p>
         * @see ExceptionTranslationFilter
         * @see RequestCache
         * @see HttpSessionRequestCache
         * @see HttpSessionRequestCache#getRequest(HttpServletRequest, HttpServletResponse)
         * @see SavedRequest
         * @see DefaultRedirectStrategy#sendRedirect(HttpServletRequest, HttpServletResponse, String)
         * @see SavedRequestAwareAuthenticationSuccessHandler
         */
        private void redirectStrategy(HttpServletRequest request, HttpServletResponse response) throws IOException {
            String redirectUrl = DEFAULT_LOGIN_SUCCESS_REDIRECT_URL;
            SavedRequest savedRequest = (SavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
            if (Objects.nonNull(savedRequest))
                redirectUrl = savedRequest.getRedirectUrl();
            new DefaultRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

    /**
     * 自定义的AuthenticationProvider
     * <p></p>
     * 通过code获取access_token,再由access_token拉取码云授权用户的信息
     */
    static class GiteeOAuth2AuthenticationProvider implements AuthenticationProvider {

        private static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/gitee";
        private static final String ACCESS_TOKEN_API_URI = "https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}&redirect_uri={redirect_uri}&client_secret={client_secret}";
        private static final String USER_INFO_URI = "https://gitee.com/api/v5/user?access_token={access_token}";
        private static final RestTemplate rest = new RestTemplate();

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            // 用户认证前，构造一个GiteeOAuth2LoginAuthenticationToken
            GiteeOAuth2LoginAuthenticationToken authenticationToken = (GiteeOAuth2LoginAuthenticationToken) authentication;
            // 通过码云API获取access_token
            String accessToken = getAccessToken(authenticationToken.getCode());
            // 通过码云API获取Gitee授权用户的资料
            Map<String, Object> userInfo = getUserInfo(accessToken);
            // 认证成功后，重新生成Authentication
            return createSuccessAuthentication(userInfo, authenticationToken.getRequest());
        }

        /**
         * 获取码云API的访问令牌access_token
         * <p></p>
         * @see UriComponentsBuilder
         * @see UriComponentsBuilder#buildAndExpand(Object...)
         * @see RequestEntity
         * @see RestTemplate#exchange(RequestEntity, Class)
         * @see JacksonJsonParser
         *
         */
        private String getAccessToken(String code) {
            ////////////////////////////////////////////////////
            /// 步骤三：使用码云access_token API向码云认证服务器发送post请求获取access_token。
            // 正确返回的access_token的json字符串：
            // {"access_token":"7282a1140867f6e3527f805af1950ea8","token_type":"bearer","expires_in":86400,"refresh_token":"0664cd3b66e36943b341285764a257ccfc7265a319dfcdd93c5f1bfbd4e023f1","scope":"user_info","created_at":1589124246}
            URI uri = UriComponentsBuilder.fromUriString(ACCESS_TOKEN_API_URI)
                    .build(code,CLIENT_ID,REDIRECT_URI,CLIENT_SECRET);
            RequestEntity<Void> requestEntity = RequestEntity
                    .post(uri)
                    .headers(httpHeaders -> httpHeaders.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36"))
                    .build();
            ResponseEntity<String> jsonContent = rest.exchange(requestEntity, String.class);
            Map<String,Object>  mapContent=new JacksonJsonParser().parseMap(jsonContent.getBody().toString());
            System.out.println("accessToken:"+(String)mapContent.get("access_token"));
            return (String) mapContent.get("access_token");
            ////////////////////////////////////////////////////
        }

        /**
         * 获取码云授权用户的信息
         * <p></p>
         * @see RequestEntity
         * @see RestTemplate#exchange(RequestEntity, Class)
         * @see JacksonJsonParser
         */
        private Map<String, Object> getUserInfo(String accessToken) {
            ////////////////////////////////////////////////////
            /// 步骤四：使用码云API获取授权用户的资料。
            /// 参考：https://gitee.com/api/v5/swagger#/getV5User
            URI uri = UriComponentsBuilder.fromUriString("https://gitee.com/api/v5/user?access_token={access_token}")
                    .build(accessToken);
            RequestEntity<Void> requestEntity = RequestEntity
                    .get(uri)
                    .headers(httpHeaders -> httpHeaders.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36"))
                    .build();

            ResponseEntity<String> jsonUserInfo = rest.exchange(requestEntity, String.class);

            System.out.println("jsonUserInfo:"+jsonUserInfo.getBody());
            Map<String,Object> userInfo = new JacksonJsonParser().parseMap(jsonUserInfo.getBody().toString());
            return userInfo;
            ////////////////////////////////////////////////////

        }

        /**
         * 认证成功后，重新构造Authentication。
         */
        private Authentication createSuccessAuthentication(Map<String, Object> userInfo, HttpServletRequest request) {
            // 构造 UserDetails
            User user = new User(userInfo.get("login").toString(), "", AuthorityUtils.createAuthorityList("USER"));
            GiteeOAuth2LoginAuthenticationToken authenticationToken = new GiteeOAuth2LoginAuthenticationToken(user, AuthorityUtils.createAuthorityList("USER"));
            // 设置认证用户的额外信息，比如 IP 地址、经纬度等。下面代码将赋值一个WebAuthenticationDetails对象，它的构造函数是request,会封装HttpServletRequest的信息。
            AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
            authenticationToken.setDetails(authenticationDetailsSource.buildDetails(request));
            return authenticationToken;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return GiteeOAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }

    /**
     * 自定义的Authentication
     * <p></p>
     * 校验前只包含code与reuqest;
     * <p>
     * 校验成功后包含principal、authorities.
     */
    static class GiteeOAuth2LoginAuthenticationToken extends AbstractAuthenticationToken {

        private String code;
        private Object principal;
        private HttpServletRequest request;

        public GiteeOAuth2LoginAuthenticationToken(String code, HttpServletRequest request) {
            super(Collections.emptyList());
            this.code = code;
            this.request = request;
        }

        public GiteeOAuth2LoginAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
            super(authorities);
            this.principal = principal;
            setAuthenticated(true);
        }

        public String getCode() {
            return code;
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public Object getCredentials() {
            return "";
        }

        @Override
        public Object getPrincipal() {
            return this.principal;
        }
    }
}
