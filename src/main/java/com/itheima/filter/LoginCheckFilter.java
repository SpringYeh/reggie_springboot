package com.itheima.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

    //  1、获取本次请求的URI
        String requestURI = request.getRequestURI();
//        log.info("拦截到请求：{}",requestURI);
    //  2、判断本次请求是否需要处理
        //定义不需要处理的请求路径
        String[] uris = {
                "/user/sendMsg",//移动端发送短信
                "/user/login",//移动端登录
                "/common/upload",
                "/common/download",
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

    //  3、如果不需要处理，则直接放行
        if(check(uris,requestURI)){
//            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

    //  4-1、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            Long loginUid = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(loginUid);
//            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }
    //  4-2、判断（移动端）登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            Long loginUid = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(loginUid);
            filterChain.doFilter(request,response);
            return;
        }

    //  5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//        log.info("用户未登录");
        return;
    }

    /**
     * 检查路径是否匹配
     * @param uris 不需要处理的请求路径数组
     * @param requestURI 本次请求的URI
     * @return true表示不需要匹配
     */
    public boolean check(String[] uris, String requestURI){
        for (int i = 0; i < uris.length; i++) {
            boolean match = PATH_MATCHER.match(uris[i], requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
