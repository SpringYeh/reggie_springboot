package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖","8008008820DHC",phone,code);

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

//    /**
//     * 发送邮箱验证码
//     * @param user
//     * @return
//     */
//    @PostMapping("/sendMsg")
//    public R<String> sendMsgEmail(@RequestBody User user, HttpSession session){
//        // 获取邮箱账号
//        String phone = user.getPhone();
//
//        String subject = "瑞吉餐购登录验证码";
//
//        if (StringUtils.isNotEmpty(phone)) {
//            String code = ValidateCodeUtils.generateValidateCode(4).toString();//生成4位随机码
//            String context = "【瑞吉】欢迎使用瑞吉餐购服务，验证码: " + code + ",切勿将验证码泄露于他人，本条验证码有效期15分钟。如非本人操作，请忽略并删除此邮件！";
//            log.info("登录验证码code={}", code);
//
//            // 真正地发送邮箱验证码
//            userService.sendMsg(phone, subject, context);
//
//            //  将随机生成的验证码保存到session中
//            session.setAttribute(phone, code);
//
//            // 验证码由保存到session 优化为 缓存到Redis中，并且设置验证码的有效时间为 5分钟
////            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
//
//            return R.success("验证码发送成功，请及时查看!");
//        }
//        return R.error("验证码发送失败，请重新输入!");
//    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从Session中获取保存的验证码 (根据账号作为属性找验证码的值确实不错耶)
        Object codeInSession = session.getAttribute(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession != null && codeInSession.equals(code)){
            //如果能够比对成功，说明登录成功

            //判断当前手机号对应的用户是否为新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if(user == null){
                //如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                // 取手机号加个前缀为用户名
                user.setName("手机冲浪" + phone);
                // 取邮箱的前缀为用户名
//                user.setName("无聊网友" + phone.substring(0,phone.indexOf("@")));
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录用户的id
        request.getSession().removeAttribute("user");
        return R.success("用户登出成功");
    }
}
