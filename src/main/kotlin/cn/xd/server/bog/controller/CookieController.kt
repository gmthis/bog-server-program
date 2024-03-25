package cn.xd.server.bog.controller

import cn.xd.server.bog.service.CookieService
import cn.xd.server.bog.util.IllegalParameterJson
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.annotation.Resource

@Controller
@ResponseBody
class CookieController {

    @Resource
    private lateinit var service: CookieService

    @PostMapping(
        "/post/cookieGit",
        headers = ["Content-Type=application/x-www-form-urlencoded"],produces = ["application/json; charset=utf-8"]
    )
    fun getCookieFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return getCookie(parameters)
    }

    @PostMapping(
        "/post/cookieGit",
        headers = ["Content-Type=application/json"],produces = ["application/json; charset=utf-8"]
    )
    fun getCookieJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return getCookie(parameters)
    }

    private fun getCookie(
        parameters: Map<String, Any>
    ): String{
        return service.newCookie()
    }

    @PostMapping(
        "/api/cookieAdd",
        headers = ["Content-Type=application/x-www-form-urlencoded"],produces = ["application/json; charset=utf-8"]
    )
    fun importCookieFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return importCookie(parameters)
    }

    @PostMapping(
        "/api/cookieAdd",
        headers = ["Content-Type=application/json"],produces = ["application/json; charset=utf-8"]
    )
    fun importCookieJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return importCookie(parameters)
    }

    private fun importCookie(
        parameters: Map<String, Any>
    ): String {
        val cookieAdd = parameters["cookieadd"]
        val _master = parameters["master"]

        if (cookieAdd !is String || _master !is String?) return IllegalParameterJson
        val master = _master ?: "0"
        return service.importCookie(master, cookieAdd)
    }

    @PostMapping(
        "/api/cookiedel",
        headers = ["Content-Type=application/x-www-form-urlencoded"],produces = ["application/json; charset=utf-8"]
    )
    fun removeCookieFrom(
        @RequestParam parameters: Map<String, Any>
    ): String {
        return removeCookie(parameters)
    }

    @PostMapping(
        "/api/cookiedel",
        headers = ["Content-Type=application/json"],produces = ["application/json; charset=utf-8"]
    )
    fun removeCookieJson(
        @RequestBody parameters: Map<String, Any>
    ): String {
        return removeCookie(parameters)
    }

    private fun removeCookie(
        parameters: Map<String, Any>
    ): String {
        val masterToken = parameters["code"]
        val masterCookie = parameters["cookie"]
        val cookie = parameters["del"]

        if (masterToken !is String || masterCookie !is String || cookie !is String || masterCookie.length != 8 ||
                masterToken.length != 32 || cookie.length != 8)
            return IllegalParameterJson

        return service.removeCookie(masterToken, masterCookie, cookie)
    }

    @PostMapping(
        "/api/remarks",
        headers = ["Content-Type=application/x-www-form-urlencoded"],produces = ["application/json; charset=utf-8"]
    )
    fun remarkCookieFrom(
        @RequestParam parameters: Map<String, Any>
    ): String{
        return remarkCookie(parameters)
    }

    @PostMapping(
        "/api/remarks",
        headers = ["Content-Type=application/json"],produces = ["application/json; charset=utf-8"]
    )
    fun remarkCookieJson(
        @RequestBody parameters: Map<String, Any>
    ): String{
        return remarkCookie(parameters)
    }

    private fun remarkCookie(
        parameters: Map<String, Any>
    ): String{
        val masterCookie = parameters["cookie"]
        val masterToken = parameters["code"]
        val cookie = parameters["target"]
        val remarks = parameters["remarks"]

        if (masterCookie !is String || masterToken !is String || cookie !is String || remarks !is String ||
                masterCookie.length != 8 || masterToken.length != 32 || cookie.length != 8)
            return IllegalParameterJson

        return service.remarkCookie(
            masterCookie,
            masterToken,
            cookie,
            remarks
        )
    }

    @PostMapping(
        "/api/sign",
        headers = ["Content-Type=application/x-www-form-urlencoded"],produces = ["application/json; charset=utf-8"]
    )
    fun signInFrom(
        @RequestParam parameters: Map<String, Any>
    ): String{
        return signIn(parameters)
    }

    @PostMapping(
        "/api/sign",
        headers = ["Content-Type=application/json"],produces = ["application/json; charset=utf-8"]
    )
    fun signInJson(
        @RequestBody parameters: Map<String, Any>
    ): String{
        return signIn(parameters)
    }

    private fun signIn(
        parameters: Map<String, Any>
    ): String{

        val cookie = parameters["cookie"]
        val token = parameters["code"]

        if (cookie !is String || token !is String || cookie.length != 8 || token.length != 32)
            return IllegalParameterJson

        return service.singIn(cookie, token)
    }

    @PostMapping(
        "/api/userinfo",
        headers = ["Content-Type=application/x-www-form-urlencoded"],produces = ["application/json; charset=utf-8"]
    )
    fun findCookieInfoFrom(
        @RequestParam parameters: Map<String, Any>
    ): String{
        return findCookieInfo(parameters)
    }

    @PostMapping(
        "/api/userinfo",
        headers = ["Content-Type=application/json"],produces = ["application/json; charset=utf-8"]
    )
    fun findCookieInfoJson(
        @RequestBody parameters: Map<String, Any>
    ): String{
       return findCookieInfo(parameters)
    }

    private fun findCookieInfo(
        parameters: Map<String, Any>
    ): String{
        val cookie = parameters["cookie"]
        val token = parameters["code"]

        if (cookie !is String || token !is String || cookie.length != 8 || token.length != 32)
            return IllegalParameterJson

        return service.findCookieInfo(cookie, token)
    }

}