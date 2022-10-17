package cn.xd.server.bog.controller

import cn.xd.server.bog.service.ForumService
import cn.xd.server.bog.util.IllegalParameterJson
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

@Controller
@ResponseBody
class ForumController {

    @Resource
    private lateinit var service: ForumService

    @PostMapping("/api/forumlist")
    fun getAllForum(): String{
        return service.getAllForum()
    }

    @PostMapping(
        "/api/forum",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun getForumContentFrom(
        @RequestParam parameters: Map<String, Any>
    ): String{
        return getForumContent(parameters)
    }

    @PostMapping(
        "/api/forum",
        headers = ["Content-Type=application/json"]
    )
    fun getForumContentJson(
        @RequestBody parameters: Map<String, Any>
    ): String{
        return getForumContent(parameters)
    }

    private fun getForumContent(
        parameters: Map<String, Any>
    ): String{

        val id = parameters["id"]
        val page = parameters["page"]
        val pageDef = parameters["page_def"]

        if (id !is String && id !is Int || page !is String && page !is Int) return IllegalParameterJson
        val idI = if (id is String) id.toIntOrNull() ?: return IllegalParameterJson else id as Int
        val pageI = if (page is String) page.toIntOrNull() ?: return IllegalParameterJson else page as Int
        if (pageDef !is String? && pageDef !is Int?) return IllegalParameterJson
        val pageDegI = if (pageDef is String) pageDef.toIntOrNull() ?: return IllegalParameterJson else (pageDef as? Int)?.let {
            if (it < 10) 10 else if (it > 20) 20 else it
        } ?: 20

        return service.getForumContent(idI, pageI, 20)
    }

}