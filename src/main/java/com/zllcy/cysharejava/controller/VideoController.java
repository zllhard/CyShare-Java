package com.zllcy.cysharejava.controller;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import com.zllcy.cysharejava.common.Result;
import com.zllcy.cysharejava.config.NonStaticResourceHttpRequestHandler;
import com.zllcy.cysharejava.entity.Label;
import com.zllcy.cysharejava.entity.Video;
import com.zllcy.cysharejava.service.LabelService;
import com.zllcy.cysharejava.service.VideoService;
import com.zllcy.cysharejava.utils.VideoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author zllcy
 * @date 2022/3/28 14:40
 */
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {
    @Value("${upload.location}")
    private String uploadLocation;

    @Value("${upload.cover}")
    private String coverLocation;

    @Value("${download.location}")
    private String downloadLocation;

    @Autowired
    private NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;

    @Autowired
    private VideoService videoService;

    @Autowired
    private LabelService labelService;

    @GetMapping("/listVideo/{pageNum}")
    public Result listVideoForPage(@PathVariable("pageNum") Integer pageNum) {
        PageInfo<Video> videoPageInfo = videoService.listVideoForPage(pageNum);
        return Result.success(videoPageInfo);
    }

    @GetMapping("/listVideoByUsername/{username}/{pageNum}")
    public Result listVideoForPage(@PathVariable("username") String username, @PathVariable("pageNum") Integer pageNum) {
        PageInfo<Video> videoPageInfo = videoService.listVideoForPageByUsername(username, pageNum);
        return Result.success(videoPageInfo);
    }

    @PostMapping("/insertVideo")
    public Result insertVideo (Video video, @RequestParam(value = "file") MultipartFile multipartFile) throws Exception {
        log.info(String.valueOf(video));
        // ?????????????????????
        String videoFilename = multipartFile.getOriginalFilename();

        // ????????????
        if (StrUtil.isNotEmpty(videoFilename)) {
            int suffixIndex = videoFilename.lastIndexOf('.');
            // ????????????
            String suffix = videoFilename.substring(suffixIndex);
            // ????????????????????????????????????
            long prefix = System.currentTimeMillis();
            // ?????????
            String fullFileName = prefix + suffix;
            // ?????????????????????
            video.setFilename(fullFileName);
            // ????????????
            File parentPath = new File(uploadLocation);
            // ???????????????????????????????????????????????????
            if (!parentPath.exists()) {
                parentPath.mkdirs();
            }
            // ?????????
            File fullPath = new File(parentPath, fullFileName);
            // ??????
            multipartFile.transferTo(fullPath);

            // ????????????
            String coverName = prefix + ".png";
            // ????????????
            video.setCover(coverName);
            // ?????????????????????
            String coverFullName = coverLocation + coverName;
            VideoUtils.fetchPic(fullPath, coverFullName);
        } else {
            return Result.fail("???????????????");
        }

        videoService.insertVideo(video);

        return Result.success(null);
    }

    @PutMapping("/updateVideo")
    public Result updateVideo(@RequestBody Video video) throws Exception {
        int i = videoService.updateVideo(video);
        if (i != 1) {
            return Result.fail("????????????");
        }
        return Result.success(null);
    }

    @DeleteMapping("/deleteVideo/{id}")
    public Result deleteVideo(@PathVariable("id") Integer id) throws Exception {
        int i = videoService.deleteVideo(id);
        if (i != 1) {
            return Result.fail("????????????");
        }
        return Result.success(null);
    }

    @GetMapping("/searchVideo/{keyWords}/{pageNum}")
    public Result searchVideo(@PathVariable("keyWords") String keyWords, @PathVariable("pageNum") Integer pageNum) throws Exception {
        PageInfo<Video> videoPageInfo = videoService.searchVideoForPageByKeyWords(pageNum, keyWords);
        return Result.success(videoPageInfo);
    }


    @RequestMapping("/play/{filename}")
    public void play(HttpServletRequest request, HttpServletResponse response, @PathVariable("filename") String filename) throws Exception{
        // ?????????????????????????????????
        String fullPath =  uploadLocation + filename;
        File file = new File(fullPath);
        if (file.exists()) {
            request.setAttribute(NonStaticResourceHttpRequestHandler.ATTR_FILE, fullPath);
            nonStaticResourceHttpRequestHandler.handleRequest(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        }
    }

    @GetMapping("/listLabel")
    public Result listLabel() {
        List<Label> labelList = labelService.listLabel();
        return Result.success(labelList);
    }

}
