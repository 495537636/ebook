package com.sunshine.ebook.controller;

import com.sunshine.ebook.common.response.ErrorResponse;
import com.sunshine.ebook.entity.Userinfo;
import com.sunshine.ebook.util.GenerateUUID;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import org.apache.hadoop.io.IOUtils;

@Api(tags = "文件上传接口")
@RestController
@RequestMapping(value = "/api/v1/upload")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${ebook.ebookPath}")
    private String ebookPath;

    @ApiOperation(value = "上传文件")
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    //@RequiresRoles("admin")
    //@RequiresPermissions("upload")
    public ResponseEntity upload(
            @ApiParam(value = "文件", required = true) @RequestPart MultipartFile file,
            @ApiParam(value = "用户ID", required = true) @RequestParam("userid") int userid
    ) {
        System.out.println("-------------------------------------");
        //获取当前的Subject
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "请先登录");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            //检查用户是否有上传权限
            subject.checkPermission("upload");
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "没有权限");
            return ResponseEntity.badRequest().body(response);
        }
        String fileName = file.getOriginalFilename();
        String sux = fileName.substring(fileName.lastIndexOf("."));
        Collection<Userinfo> reamInfo = subject.getPrincipals().fromRealm("user");
        String relativelyPath = System.getProperty("user.dir");
        File parentFile = new File(relativelyPath).getParentFile();
        String savePath = parentFile.getPath() + File.separator + ebookPath + File.separator + userid + File.separator;
        File folder = new File(savePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String saveFile = savePath + GenerateUUID.generateFileId() + sux;
        logger.info("文件保存路径：" + saveFile);
        try {
            FileOutputStream out = new FileOutputStream(new File(saveFile));
            IOUtils.copyBytes(file.getInputStream(), out, 1024, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
