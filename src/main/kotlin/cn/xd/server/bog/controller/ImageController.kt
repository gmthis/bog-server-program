package cn.xd.server.bog.controller

import cn.xd.server.bog.service.ImageService
import cn.xd.server.bog.util.errorJson
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.net.http.HttpResponse
import javax.annotation.Resource
import javax.servlet.http.HttpServletResponse

@Controller
@ResponseBody
class ImageController {

    @Resource
    lateinit var service: ImageService

    companion object{
        private val notUploadImage = errorJson(304, "没有上传图片")
    }

    @PostMapping(
        "/post/upload",
        headers = ["Content-Type=multipart/form-data;"]
    )
    fun imageUpload(
        @RequestParam image: List<CommonsMultipartFile>
    ): String{

        if (image.isEmpty()) return notUploadImage

        return service.imageUpload(image)
    }

    @GetMapping(
        "/image/large/{image}"
    )
    fun large(
        @PathVariable("image") image: String,
        response: HttpServletResponse
    ){
        val lastIndexOf = image.lastIndexOf(".")
        val url = image.substring(0, lastIndexOf - 1)
        val ext = image.substring(lastIndexOf)

        service.sendImage(url, ext, response, "./image/large/")
    }

    @GetMapping(
        "/image/thumb/{image}"
    )
    fun thumb(
        @PathVariable("image") image: String,
        response: HttpServletResponse
    ){
        val lastIndexOf = image.lastIndexOf(".")
        val url = image.substring(0, lastIndexOf - 1)
        val ext = image.substring(lastIndexOf)

        service.sendImage(url, ext, response, "./image/thumb/")
    }

}