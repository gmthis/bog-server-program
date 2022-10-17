package cn.xd.server.bog.service.impl

import cn.xd.server.bog.dao.ImageDao
import cn.xd.server.bog.entity.string.Image
import cn.xd.server.bog.service.ImageService
import cn.xd.server.bog.util.InternalErrorJson
import cn.xd.server.bog.util.errorJson
import cn.xd.server.bog.util.successJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import net.coobird.thumbnailator.Thumbnailator
import net.coobird.thumbnailator.Thumbnails
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.DigestUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.annotation.Resource
import javax.servlet.http.HttpServletResponse

@Service
class ImageServiceImpl: ImageService {

    @Resource
    lateinit var dao: ImageDao

    companion object{
        private val log = LoggerFactory.getLogger(ImageServiceImpl::class.java)

        private val imageIsBan = errorJson(2301, "图片被ban")
    }

    @Transactional
    override fun imageUpload(images: List<CommonsMultipartFile>): String {

        val imagesSend = ArrayList<Image>(images.size)

        for (image in images) {
            val digest = DigestUtils.md5DigestAsHex(image.bytes)
            val filename = image.originalFilename
            val ext = filename.substring(filename.lastIndexOf("."))

            if (ext != ".jpg" && ext != ".png" && ext != ".gif" && ext != "webp") {
                imagesSend.add(Image("image does not exist.", ""))
                continue
            }

            var isBan = dao.checkBan(digest)
            if (isBan == null){
                val addImageFile = dao.addImage(digest, ext)
                if (addImageFile != 1) return InternalErrorJson.also {
                    log.error("插入图片出错，目标图片md5: $digest")
                    log.error(Exception("堆栈信息").stackTraceToString())
                }else{
                    log.info("插入图片:$digest")
                }

                val scaleImage = compressPicForScale(image.bytes, 512)
                val srcSize = image.bytes.size

                image.transferTo(File("./image/large/$digest$ext")).also {
                    log.info("上传大图完成:$digest,图片大小:${srcSize / 1024}kb")
                }

                File("./image/thumb/$digest$ext").outputStream().use {
                    it.write(scaleImage)
                }.also {
                    log.info("生成缩略图完成:$digest,图片大小:${scaleImage.size / 1024}kb")
                }

                isBan = false
            }

            if (isBan){
                imagesSend.add(Image("image does not exist.", ""))
                continue
            }
            imagesSend.add(Image(digest, ext))
        }

        return successJson(200, Json.encodeToJsonElement(imagesSend))
    }

    override fun sendImage(url: String, ext: String, response: HttpServletResponse, path: String) {
        val checkBanFlag = dao.checkBan(url)
        var _url = url
        var _ext = ext

        if (checkBanFlag == null || checkBanFlag){
            _url = "image"
            _ext = "png"
        }

        val file = File("$path$_url$_ext")

        val contentType = when(_ext){
            ".jpg" -> "image/jpeg"
            ".gif" -> "image/gif"
            ".png" -> "image/png"
            ".webp" -> "image/webp"
            else -> "image/jpeg"
        }

        response.contentType = contentType
        response.outputStream.use {output ->
            file.inputStream().use {input ->
                IOUtils.copy(input, output)
            }
        }
    }

    /**
     * @param desFileSize 目标图片大小 单位kb
     */
    private fun compressPicForScale(imageBytes: ByteArray, desFileSize: Long): ByteArray{
        if (imageBytes.isEmpty() || imageBytes.size < desFileSize * 1024) return imageBytes

        val srcSize = imageBytes.size.toLong()
        val accuracy = getAccuracy(srcSize / 1024)
        var outArray = imageBytes

        while (outArray.size > desFileSize * 1024){
            val inputStream = ByteArrayInputStream(imageBytes)
            val outputStream = ByteArrayOutputStream(outArray.size)
            Thumbnails.of(inputStream)
                .scale(accuracy)
                .outputQuality(accuracy)
                .toOutputStream(outputStream)
            outArray = outputStream.toByteArray()
            inputStream.close()
            outputStream.close()
        }

        return outArray
    }

    private fun getAccuracy(size: Long): Double = when{
        size < 900 -> 0.85
        size < 2047 -> 0.6
        size < 3275 -> 0.44
        else -> 0.4
    }

}