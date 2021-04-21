package cn.edu.dgut.css.sai.springsecuritygiteeexperiment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;

@SpringBootApplication
public class SpringSecurityGiteeExperimentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityGiteeExperimentApplication.class, args);
    }

    @SuppressWarnings("JavadocReference")
    @Configuration
    static class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

        /**
         * 重写{@link WebSecurityConfigurerAdapter#configure(AuthenticationManagerBuilder)}方法，会实例化一个这条过滤链本地专用的{@link AuthenticationManager}，
         * 这时不会使用全局的{@link AuthenticationConfiguration#getAuthenticationManager()}。
         * <p>
         * 可以定义多个{@link WebSecurityConfigurerAdapter}的子类，每个子类会被Spring Security框架解释为一条安全过滤链{@link SecurityFilterChain},它们由{@link FilterChainProxy}匹配、选择使用。
         * <p>
         * <p>
         * Spring Security的自动配置类{@link AuthenticationConfiguration}声明了一个{@link InitializeUserDetailsBeanManagerConfigurer}Bean，下面说明它有什么作用：
         * {@link InitializeUserDetailsBeanManagerConfigurer} 继承了{@link GlobalAuthenticationConfigurerAdapter} 会用于配置全局AuthenticationManager{@link AuthenticationConfiguration#getAuthenticationManager()}。
         * {@link InitializeUserDetailsBeanManagerConfigurer.InitializeUserDetailsManagerConfigurer#configure(AuthenticationManagerBuilder)}方法会检查是否有{@link UserDetailsService}的Bean在容器中，
         * 如果有的话，会实例化一个{@link DaoAuthenticationProvider},并添加到全局AuthenticationManager中;
         * 它还会检查是否有{@link PasswordEncoder}的Bean在容器，如果有的话，会配置在{@link DaoAuthenticationProvider}中。
         * 总结：{@link InitializeUserDetailsBeanManagerConfigurer}的作用就是帮助全局{@link AuthenticationManager}初始化一个{@link DaoAuthenticationProvider}
         *
         * @see SecurityAutoConfiguration
         * @see EnableGlobalAuthentication
         * @see AuthenticationConfiguration
         * @see WebSecurityConfigurerAdapter#getHttp()
         * @see WebSecurityConfigurerAdapter#authenticationManager()
         * @see AbstractDaoAuthenticationConfigurer
         * @see InMemoryUserDetailsManagerConfigurer
         * @see InMemoryUserDetailsManager
         * @see DaoAuthenticationProvider
         */
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("ADMIN").authorities("ADMIN");
        }

        @Bean
        UserDetailsService CustomUserDetailsService() {
            return new InMemoryUserDetailsManager(User.withUsername("sai").password("{noop}sai").roles("USER").authorities("doIt").build());
        }

        /**
         * 设置忽略静态资源的身份认证
         */
        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/css/**", "/img/**", "/**/*.png");
        }

        /**
         * 配置自定义的登录界面，并设置允许所有人访问(必须)。
         * 注意：post请求是默认有CSRF保护。请在自定义的登录界面的表单中加入CSRF令牌一并提交。或配置.csrf().disable()；
         *
         * @see WebSecurityConfigurerAdapter#getHttp()
         * @see UsernamePasswordAuthenticationFilter
         * @see HttpSecurity#formLogin()
         * @see FormLoginConfigurer
         * @see AbstractAuthenticationFilterConfigurer#init(HttpSecurityBuilder)
         * @see AbstractAuthenticationFilterConfigurer#updateAuthenticationDefaults()
         * @see AbstractAuthenticationFilterConfigurer#updateAccessDefaults(HttpSecurityBuilder)
         * @see AbstractAuthenticationFilterConfigurer#configure(HttpSecurityBuilder)
         * @see HttpSecurity#logout()
         * @see LogoutConfigurer
         * @see LogoutConfigurer#getLogoutRequestMatcher(HttpSecurityBuilder) 如果CSRF关闭，则对退出登录的请求方法不限制；如果CSRF启用(默认)，只支持post请求。
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http
                    // 配置安全过滤链只拦载检查 '/admin/**' 的请求，并配置需要的权限。
                    .antMatcher("/admin/**")
                        .authorizeRequests().anyRequest().hasAnyAuthority("ADMIN").and()
                    // 自定义登录界面
                    .formLogin()
                        .loginPage("/admin/login_backend").permitAll()
                        // 设置默认身份认证成功后跳转的页面,即直接访问登录界面时，认证成功后跳转的页面。
                        .defaultSuccessUrl("/admin").and()
                    // 自定义退出登录请求Url、成功退出登录后的重定向Url
                    .logout()
                        .logoutUrl("/admin/logout").permitAll()
                        .logoutSuccessUrl("/admin/login_backend?logout");

        }
    }

    /**
     * 另一条安全过滤链，匹配所有请求。
     * {@link WebSecurityConfigurerAdapter}默认order是100,不能有两个相同的order值WebSecurityConfigurerAdapter。order值越小，越优先匹配。
     *
     * @see FilterChainProxy#getFilters(HttpServletRequest) for循环遍历。
     * @see Order
     * @see WebSecurityConfigurerAdapter
     */
    @SuppressWarnings("JavadocReference")
    @Configuration
    @Order(101)
    static class UserSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication().withUser("user").password("{noop}user").roles("USER").authorities("USER");
        }

        /**
         * 前台安全过滤链配置。
         * <p></p>
         * 这里我们关注另一个问题，异常问题。
         * <p>
         * Spring Security主要分两种：{@link AuthenticationException}认证异常、{@link AccessDeniedException}访问拒绝异常，它们由{@link ExceptionTranslationFilter}捕获处理。<br/>具体逻辑看{@link ExceptionTranslationFilter#handleSpringSecurityException(HttpServletRequest, HttpServletResponse, FilterChain, RuntimeException)}
         * <p></p>
         * 由上面的{@code handleSpringSecurityException}方法源码可知：<br>
         * 认证异常被捕获后交由{@link AuthenticationEntryPoint}处理；<p>
         * 访问拒绝异常被捕获后由{@link AccessDeniedHandler}处理。<p>
         * 注意的是，如果用户没有认证，即是匿名用户，抛出{@link AccessDeniedException}时会交由{@link AuthenticationEntryPoint}处理。
         * <p></p>
         * <p>
         * {@link WebSecurityConfigurerAdapter#getHttp()}查看这个方法的源码，了解默认调用了{@link HttpSecurity#exceptionHandling()}方法；<br>
         * {@link HttpSecurity#exceptionHandling()}方法会添加{@link ExceptionHandlingConfigurer}配置器；<br>
         * {@link ExceptionHandlingConfigurer} 配置{@link AuthenticationEntryPoint}和{@link AccessDeniedHandler}，把它们赋值给{@link ExceptionTranslationFilter}，最后把{@link ExceptionTranslationFilter}加入安全过滤链。<p></p>
         *
         * @see LoginUrlAuthenticationEntryPoint
         * @see AccessDeniedHandlerImpl
         * @see FilterComparator
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // 不指定path,本安全过滤链会匹配所有请求。
            http
                    .authorizeRequests()
                        .antMatchers("/").permitAll()// 首页放行
                        .anyRequest().hasAnyAuthority("USER").and()
                    .formLogin()
                        .loginPage("/user/login_frontend").permitAll()
                        .defaultSuccessUrl("/user").and()
                    .logout()
                        .logoutUrl("/user/logout").permitAll()
                        .logoutSuccessUrl("/user/login_frontend?logout").and()
                    // 自定义访问拒绝异常处理逻辑
                    .exceptionHandling().accessDeniedHandler(UserSecurityConfig::accessDeniedHandle)
                    ////////////////////////////////////////////////
                    /// 步骤六：把我们自定义的SecurityConfigurer应用到安全过滤链
            .and()
            .apply(new GiteeOAuth2LoginConfigurer<>())
            ;
            ////////////////////////////////////////////////
        }
        /**
         * @see AccessDeniedHandlerImpl#handle(HttpServletRequest, HttpServletResponse, AccessDeniedException)
         */
        private static void accessDeniedHandle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
            request.setAttribute(WebAttributes.ACCESS_DENIED_403,
                    accessDeniedException);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setCharacterEncoding(Charset.defaultCharset().displayName());// 解决中文乱码
            response.addHeader("Content-Type", MediaType.TEXT_HTML_VALUE);
            response.getWriter().write("你的权限不够");
        }
    }

    @Controller
    static class UserLoginController {

        // 前台-登录界面
        @GetMapping("/user/login_frontend")
        String userLogin() {
            return "login_frontend";
        }

        /**
         * 获取登录用户的资料接口
         * <p></p>
         * @see Authentication
         * @see Principal
         * @see Model
         */
        @GetMapping("/user")
        String userIndex(HttpServletRequest request,Model model) {
            ////////////////////////////////////
            /// 步骤七：改造/user接口，返回码云用户资料给前端；改造user.ftlh模板用于显示用户资料。
           GiteeOAuth2LoginConfigurer.GiteeOAuth2LoginAuthenticationToken auth = (GiteeOAuth2LoginConfigurer.GiteeOAuth2LoginAuthenticationToken)request.getUserPrincipal();
           String userName= auth.getName();
           model.addAttribute("userName",userName);
            return "user";
            ////////////////////////////////////
        }

        // http://localhost:8080/test
        @GetMapping("test")
        @ResponseBody
        String test(Authentication auth) {
            System.out.println("auth = " + auth);
            return "访问/test接口成功,你拥有USER权限";
        }
    }

    @Controller
    static class AdminLoginController {
        // 后台-登录界面
        @GetMapping("/admin/login_backend")
        public String adminLogin() {
            return "login_backend";
        }

        // 后台-首页
        @GetMapping("/admin")
        public String adminIndex() {
            return "admin";
        }
    }


}
