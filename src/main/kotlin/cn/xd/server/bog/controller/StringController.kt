package cn.xd.server.bog.controller

import cn.xd.server.bog.entity.string.Image
import cn.xd.server.bog.service.StringService
import cn.xd.server.bog.util.IllegalParameterJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.annotation.Resource

@Controller
@ResponseBody
class StringController {

    @Resource
    lateinit var service: StringService

    @PostMapping(
        "/api/thread",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun getSingleContentFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return getSingleContent(parameters)
    }

    @PostMapping(
        "/api/thread",
        headers = ["Content-Type=application/json"]
    )
    fun getSingleContentJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return getSingleContent(parameters)
    }

    private fun getSingleContent(
        parameters: Map<String, Any>
    ): String {
        val id = parameters["id"]

        if (id !is String && id !is Int) return IllegalParameterJson
        val idI = if (id is String) id.toIntOrNull() ?: return IllegalParameterJson else id as Int

        return service.getSingleContent(idI)
    }

    @PostMapping(
        "/api/threads",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun getContentAndReplyFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return getContentAndReply(parameters)
    }

    @PostMapping(
        "/api/threads",
        headers = ["Content-Type=application/json"]
    )
    fun getContentAndReplyJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return getContentAndReply(parameters)
    }

    private fun getContentAndReply(
        parameters: Map<String, Any>
    ): String {

        val id = parameters["id"]
        val page = parameters["page"]
        val pageDef = parameters["page_def"]
        val _order = parameters["order"]

        if (id !is String && id !is Int) return IllegalParameterJson
        val idI = if (id is String) id.toIntOrNull() ?: return IllegalParameterJson else id as Int
        if (page !is String && id !is Int) return IllegalParameterJson
        val pageI = if (page is String) page.toIntOrNull() ?: return IllegalParameterJson else page as Int
        if (pageDef !is String && pageDef !is Int) return IllegalParameterJson
        val pageDefI =
            (if (pageDef is String) pageDef.toIntOrNull() ?: return IllegalParameterJson else pageDef as Int).let {
                if (it > 20) 20 else if (it < 10) 10 else it
            }
        if (_order !is String && _order !is Int) return IllegalParameterJson
        val order = (if (_order is String) _order.toIntOrNull() ?: return IllegalParameterJson else _order as Int).let {
            if (it != 0 && it != 1) 0 else it
        }

        return service.getContentAndReply(idI, pageI, pageDefI, order)

    }

    @PostMapping(
        "/search",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun fuzzySearchFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return fuzzySearch(parameters)
    }

    @PostMapping(
        "/search",
        headers = ["Content-Type=application/json"]
    )
    fun fuzzySearchJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return fuzzySearch(parameters)
    }

    private fun fuzzySearch(
        parameters: Map<String, Any>
    ): String {
        val keyword = parameters["keyword"]
        val page = parameters["page"]
        val pageDef = parameters["page_def"]

        if (keyword !is String) return IllegalParameterJson
        if (page !is String && page !is Int) return IllegalParameterJson
        if (pageDef !is String? && pageDef !is Int?) return IllegalParameterJson

        val pageI = if (page is String) page.toIntOrNull() ?: return IllegalParameterJson else page as Int
        val pageDefI =
            if (pageDef is String) pageDef.toIntOrNull() ?: return IllegalParameterJson else (pageDef as? Int)?.let {
                if (it > 20) 20 else if (it < 1) 1 else it
            } ?: 20

        return service.fuzzySearch(keyword, pageI, pageDefI)
    }

    @PostMapping(
        "/post/del",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun delStringFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return delString(parameters)
    }

    @PostMapping(
        "/post/del",
        headers = ["Content-Type=application/json"]
    )
    fun delStringJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return delString(parameters)
    }

    private fun delString(
        parameters: Map<String, Any>
    ): String {
        val _id = parameters["id"]
        val _cookie = parameters["cookie"]

        if (_id !is Int && _id !is String) return IllegalParameterJson
        val id = if (_id is String) _id.toIntOrNull() ?: return IllegalParameterJson else _id as Int
        if (_cookie !is String) return IllegalParameterJson

        return service.delString(id, _cookie)
    }

    @PostMapping(
        "/post/post",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun sendStringFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return sendString(parameters)
    }

    @PostMapping(
        "/post/post",
        headers = ["Content-Type=application/json"]
    )
    fun sendStringJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return sendString(parameters)
    }

    private fun sendString(
        parameters: Map<String, Any>
    ): String {

        val _res = parameters["res"]
        val _forum = parameters["forum"]
        val _title = parameters["title"]
        val _name = parameters["name"]
        val comment = parameters["comment"]
        val cookie = parameters["cookie"]
        val _webapp = parameters["webapp"]
        val _img = parameters["img"]

        if (_res !is String? && _res !is Int?
            || _forum !is String? && _forum !is Int?
            || _title !is String?
            || _name !is String?
            || comment !is String
            || cookie !is String
            || _webapp !is String && _webapp !is Int
            || _img !is String? && _img !is List<*>?)
            return IllegalParameterJson

        val res = if (_res is String) _res.toIntOrNull() ?: return IllegalParameterJson else _res as Int
        val forum = if (_forum is String) _forum.toIntOrNull() ?: 0 else _forum as Int
        val title = _title ?: ""
        val name = _name ?: ""
        val webapp = if (_webapp is String) _webapp.toIntOrNull() ?: return IllegalParameterJson else _webapp as Int
        val img: List<Image>? =
            if (_img != null)
                if (_img is List<*>)
                    try {
                        val list = ArrayList<Image>(_img.size)
                        for (any in _img) {
                            if (any !is Map<*, *>) continue
                            list.add(Image(any["url"]!! as String, any["ext"]!! as String))
                        }
                        list
                    } catch (e: Exception){
                        return IllegalParameterJson
                    }

                else
                    try {
                        Json.decodeFromString<List<Image>>(_img as String)
                    } catch (e: Exception){
                        return  IllegalParameterJson
                    }
            else
                null


        return service.sendString(res, forum, title, name, comment, cookie, webapp, img)
    }

}