package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;



    /**
     * 员工登录
     * @param request 用以存员工ID到session
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
//        System.out.println(employee);     //@RequestBody Employee employee 请求体赋给了employee变量
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //3、如果没有查询到则返回登录失败结果
        if(emp==null){
            return R.error("用户不存在");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }
        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("员工登出成功");
    }

    /**
     * 新增员工
     * @param request 要获取当前登录用户的id
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        // 1、设置初始默认密码123456，并MD5加密
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);

//        // 2、表中还有4个属性需要自行设置
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        // // 获取当前登录用户的id
//        Long createId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(createId);
//        employee.setUpdateUser(createId);
        //已使用MP的公共字段自动填充


        // 3、调用service端存储员工数据到数据库
        employeeService.save(employee);
        return R.success("员工添加成功");
    }

    /**
     * 员工信息分页查询
     * @param page 第几页
     * @param pageSize 一页几个
     * @param name 按名称查询（可选项）
     * @return Page对象，由MP封装好的
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
//        log.info("分页查询条件：page={};pageSize={};name={}",page,pageSize,name);

        // 1、分页构造器
        Page<Employee> pageRes = new Page(page,pageSize);

        // 2、条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //  //添加过滤条件
        queryWrapper.like(name!=null,Employee::getName,name);
        //  //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 3、执行查询
        employeeService.page(pageRes,queryWrapper);

        return R.success(pageRes);
    }

    /**
     * 根据id修改员工信息
     * @param request 要用到session里登录者的id
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
//        Long loginUid = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(loginUid);
//        employee.setUpdateTime(LocalDateTime.now());
        //已使用MP的公共字段自动填充

        employeeService.updateById(employee);
//        ==>  Preparing: UPDATE employee SET status=?, update_user=? WHERE id=?
//        ==> Parameters: 1(Integer), 1(Long), 1620624352603058200(Long)
        return R.success("修改员工信息成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee emp = employeeService.getById(id);
        if(emp!=null){
            return R.success(emp);
        }
        return R.error("没有查询到该用户");
    }
}
