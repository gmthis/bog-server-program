package cn.xd.server.bog.controller

import cn.xd.server.bog.service.CookieService
import cn.xd.server.bog.util.IllegalParameterJson
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import javax.annotation.Resource

@Controller
@ResponseBody
class CookieController {

    @Resource
    lateinit var service: CookieService

    @PostMapping(
        "/post/cookieGit",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun getCookieFrom(cdk: GetCookieRequest): String {
        return service.newCookie()
    }

    @PostMapping(
        "/post/cookieGit",
        headers = ["Content-Type=application/json"]
    )
    fun getCookieJson(
        @RequestBody cdk: GetCookieRequest
    ): String {
        return service.newCookie()
    }

    @PostMapping(
        "/api/cookieAdd",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun importCookieFrom(
        importCookieRequest: ImportCookieRequest
    ): String {
        return importCookie(importCookieRequest)
    }

    @PostMapping(
        "/api/cookieAdd",
        headers = ["Content-Type=application/json"]
    )
    fun importCookieJson(
        @RequestBody importCookieRequest: ImportCookieRequest
    ): String {
        return importCookie(importCookieRequest)
    }

    private fun importCookie(
        importCookieRequest: ImportCookieRequest
    ): String {
        if (importCookieRequest.cookieadd == null) return IllegalParameterJson
        val cookieAdd = importCookieRequest.cookieadd
        val master = importCookieRequest.master ?: "0"
        return service.importCookie(master, cookieAdd)
    }

    @PostMapping(
        "/api/cookiedel",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun removeCookieFrom(
        removeCookieRequest: RemoveCookieRequest
    ): String {
        return removeCookie(removeCookieRequest)
    }

    @PostMapping(
        "/api/cookiedel",
        headers = ["Content-Type=application/json"]
    )
    fun removeCookieJson(
        @RequestBody removeCookieRequest: RemoveCookieRequest
    ): String {
        return removeCookie(removeCookieRequest)
    }

    private fun removeCookie(
        removeCookieRequest: RemoveCookieRequest
    ): String {
        if (
            removeCookieRequest.code == null ||
            removeCookieRequest.cookie == null ||
            removeCookieRequest.del == null ||
            removeCookieRequest.cookie.length != 8 ||
            removeCookieRequest.code.length != 32 ||
            removeCookieRequest.del.length != 8
        ) return IllegalParameterJson
        return service.removeCookie(removeCookieRequest.code, removeCookieRequest.cookie, removeCookieRequest.del)
    }

    @PostMapping(
        "/api/remarks",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun remarkCookieFrom(
        remarkCookieRequest: RemarkCookieRequest
    ): String{
        return remarkCookie(remarkCookieRequest)
    }

    @PostMapping(
        "/api/remarks",
        headers = ["Content-Type=application/json"]
    )
    fun remarkCookieJson(
        @RequestBody remarkCookieRequest: RemarkCookieRequest
    ): String{
        return remarkCookie(remarkCookieRequest)
    }

    private fun remarkCookie(
        remarkCookieRequest: RemarkCookieRequest
    ): String{
        if (
            remarkCookieRequest.cookie == null ||
            remarkCookieRequest.code == null ||
            remarkCookieRequest.target == null ||
            remarkCookieRequest.remarks == null ||
            remarkCookieRequest.cookie.length != 8 ||
            remarkCookieRequest.code.length != 32 ||
            remarkCookieRequest.target.length != 8
        ) return IllegalParameterJson
        return service.remarkCookie(
            remarkCookieRequest.cookie,
            remarkCookieRequest.code,
            remarkCookieRequest.target,
            remarkCookieRequest.remarks
        )
    }

    @PostMapping(
        "/api/sign",
        headers = ["Content-Type=application/x-www-form-urlencoded"]
    )
    fun signInFrom(
        signInRequest: SingInRequest
    ): String{
        return signIn(signInRequest)
    }

    @PostMapping(
        "/api/sign",
        headers = ["Content-Type=application/json"]
    )
    fun signInJson(
        @RequestBody signInRequest: SingInRequest
    ): String{
        return signIn(signInRequest)
    }

    private fun signIn(
        signInRequest: SingInRequest
    ): String{
        if (
            signInRequest.code == null ||
            signInRequest.cookie == null ||
            signInRequest.cookie.length != 8 ||
            signInRequest.code.length != 32
        ) return IllegalParameterJson
        return service.singIn(signInRequest.cookie, signInRequest.code)
    }
}

data class GetCookieRequest(
    val cdk: String?
)

data class ImportCookieRequest(
    val master: String?,
    val cookieadd: String?
)

data class RemoveCookieRequest(
    val cookie: String?,
    val code: String?,
    val del: String?
)

data class RemarkCookieRequest(
    val cookie: String?,
    val code: String?,
    val target: String?,
    val remarks: String?
)

data class SingInRequest(
    val cookie: String?,
    val code: String?
)