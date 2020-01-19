package com.cositea.demo.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags="文件下载接口")
@RestController
@RequestMapping(value="downLoad")  
public class DownloadController {
    private static final Logger log = LoggerFactory.getLogger(GWTestController.class);
    
    @ApiIgnore
    @RequestMapping("version")
    public String  downLoadApp(String filename,HttpServletResponse res) {
        
        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + filename);
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        File file=new File("./file/"  + filename);//可以自己更改文件路径
        if(!file.exists()){    
            log.info("文件"+filename+" 不存在!");
            return "文件"+filename+" 不存在";
        }    
        res.setContentLengthLong(file.length());
        try {
          os = res.getOutputStream();
          bis = new BufferedInputStream(new FileInputStream(file));
          int i = bis.read(buff);
          while (i != -1) {
            os.write(buff, 0, buff.length);
            os.flush();
            i = bis.read(buff);
          }
        } catch (IOException e) {
          log.info("下载中断！！！！！！！");
        } finally {
          if (bis != null) {
            try {
              bis.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
        log.info("下载文件success");
      return "success";
  }

}
