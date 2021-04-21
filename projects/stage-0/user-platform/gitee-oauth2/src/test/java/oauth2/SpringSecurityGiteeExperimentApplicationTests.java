package oauth2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringSecurityGiteeExperimentApplicationTests {

    ///// Setting Up MockMvc and Spring Security. start
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    // 在本类中，每次执行测试都先调用@BeforeEach注解的方法
    @BeforeEach
    public void init() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }
    ///// Setting Up MockMvc and Spring Security. end


    /**
     * 测试限权接口
     * <p></p>
     * 模拟一个登录用户，访问受保护的接口。
     * <p></p>
     *
     * @see WithMockUser
     * @see MockMvc#perform(RequestBuilder)
     * @see MockMvcRequestBuilders#get(String, Object...)
     */
    @Test
    @WithMockUser(username = "use", password = "user", roles = "USER", authorities = "USER")
    public void test() throws Exception {
        ////////////////////////////////////////////
        String content = mvc.perform(get("/test")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())//返回的状态是200
                .andDo(print())         //打印出请求和相应的内容
                .andReturn().getResponse().getContentAsString();
        assertThat(content.equals("访问/test接口成功,你拥有USER权限"));
        ////////////////////////////////////////////
    }


}
