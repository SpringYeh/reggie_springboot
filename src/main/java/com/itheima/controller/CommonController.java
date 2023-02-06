package com.itheima.controller;

import com.itheima.common.CustomException;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());

        //原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String filename = UUID.randomUUID().toString() + suffix;

        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists()){
            //目录不存在，需要创建
            dir.mkdirs();
//            mkdirs()可以建立多级文件夹
//            mkdir()只会建立一级的文件夹
        }

        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }

        return R.success(filename);
    }

    /**
     * 文件下载
     * @param name
     * @param res
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse res){
        try {
            //输入流，通过输入流读取文件内容
            //输出流，通过输出流将文件写回浏览器
            FileInputStream is= new FileInputStream(basePath+name);
            ServletOutputStream os = res.getOutputStream();

            int len=0;
            byte[] bytes = new byte[1024];
            while((len= is.read(bytes)) != -1){
                os.write(bytes,0,len);
                os.flush();
            }
            //关闭资源
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
