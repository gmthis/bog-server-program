package cn.xd.server.bog.service

import org.springframework.web.multipart.commons.CommonsMultipartFile
import javax.servlet.http.HttpServletResponse

interface ImageService {
    fun imageUpload(images: List<CommonsMultipartFile>): String
    fun sendImage(url: String, ext: String, response: HttpServletResponse, path: String)
}
