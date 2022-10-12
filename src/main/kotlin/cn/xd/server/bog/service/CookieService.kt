package cn.xd.server.bog.service

import cn.xd.server.bog.dao.CookieDAO
import cn.xd.server.bog.entity.cookie.CookieDB
import cn.xd.server.bog.exitApplication
import cn.xd.server.bog.util.*
import com.fasterxml.jackson.databind.DeserializationConfig
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.Resource

@Service
class CookieService {

    @Resource
    lateinit var dao: CookieDAO

    companion object {
        private val log = LoggerFactory.getLogger(CookieService::class.java)
    }

    /**
     * 生成一个新的cookie
     * @return 回复客户端的JSON
     */
    @Transactional
    fun newCookie(): String{
        var cookieMain = randomCookieMain()
        var cookieToken = randomCookieToken()

//        检查,避免出现重复的cookie和token
        while (true){
            val num = dao.checkExist(cookieMain, cookieToken)
            if (num != 0) {
                if (num > 1){
                    log.error("致命的错误,数据库表cookie中出现了不该出现的重复数据,这是不可能的,问题发生的目标: cookie: $cookieMain, token: $cookieToken")
                    log.error(Exception("堆栈信息").stackTraceToString())
                    exitApplication()
                    return InternalErrorJson
                }
                cookieMain = randomCookieMain()
                cookieToken = randomCookieToken()
            }else{
                break
            }
        }

        val cookie = CookieDB(cookie = cookieMain, token = cookieToken)
        return if (dao.newCookie(cookie) == 1)
            successJson(2, JsonPrimitive("${cookie.cookie}#${cookie.token}")).also {
                log.info("生成一个新的cookie: $it")
            }
        else
            InternalErrorJson.also {
                log.error(Exception("堆栈信息").stackTraceToString())
            }
    }

    /**
     * 导入一个cookie
     * @return 回复客户端的JSON
     */
    @Transactional
    fun importCookie(master: String, cookieAdd: String): String {
        val (cookie, token) = checkCookieAndTokenLegality(cookieAdd) ?: return IllegalParameterJson
        if (master == "0"){
//            如果这个cookie没有问题,则将这个cookie设置为主cookie,并设置为已导入,
            val flag = checkAnd(cookie, token) {
                dao.setCookieIsMain(token)
                dao.importCookie(token)
            } ?: return errorJson(2101, "cookie不存在")
            if (flag != 1) return InternalErrorJson.also {
                log.error("设置cookie导入状态时出错")
                log.error(Exception("堆栈信息").stackTraceToString())
            }
            return successJson(3104, JsonArray(listOf(
                Json.encodeToJsonElement(dao.findCookie(cookie)!!.getRemarkStyle())
            ))).also {
                log.info("导入cookie: $cookieAdd, 主cookie为: $master")
            }
        } else {
            val (masterCookie, masterToken) = checkCookieAndTokenLegality(master) ?: return IllegalParameterJson
            if (masterToken == token) return IllegalParameterJson
//            如果这两个cookie都没有问题,那么如下操作: 1.检查master是否被导入 2.检查master是否为主cookie 3.设置cookie的主cookie
//            4.设置cookie状态为已导入
            checkAnd(cookie, token){
                checkAnd(masterCookie, masterToken){}
            } ?: return errorJson(2101, "cookie不存在")

            if (!dao.checkImport(masterToken)) return errorJson(2102, "master cookie未导入")
            if (!dao.checkCookieIsMain(masterToken)) return errorJson(2103, "master cookie非主cookie")
            val importFlag = dao.importCookie(token)
            val setFlag = dao.setCookieMaster(token, masterCookie)
//            检查两次操作是否成功
            if (setFlag != 1 && importFlag != 1) return InternalErrorJson.also {
                log.error("设置cookie导入状态时出错")
                log.error(Exception("堆栈信息").stackTraceToString())
            }

            return successJson(3104, JsonArray(listOf(
                Json.encodeToJsonElement(dao.findCookie(cookie)!!.getRemarkStyle())
            ))).also {
                log.info("导入cookie: $cookieAdd, 主cookie为: $master")
            }
        }
    }

    /**
     * 删除某个影武者的导入
     *
     * 注意函数内不会检查masterToken和masterCookie的合法性,在调用前需要检查
     * @return 回复客户端的JSON
     */
    @Transactional
    fun removeCookie(masterToken: String, masterCookie: String, cookie: String): String {
        if (masterCookie == cookie) return IllegalParameterJson

        val cookieDB = dao.findCookie(cookie) ?: return errorJson(2101, "目标cookie不存在")

        checkAnd(masterCookie, masterToken){

        } ?: return errorJson(2101, "cookie不存在")

//        1.检查cookie的主cookie是否是masterCookie
        if (cookieDB.isMainCookie) return errorJson(2105, "目标cookie是个主cookie")
        if (!cookieDB.register) return errorJson(2102, "目标cookie未导入")
        if (cookieDB.mainCookie != masterCookie) return errorJson(2104, "目标cookie不是主cookie的影武者")

        val removerCookieFlag = dao.removerCookie(cookieDB.token)
        if (removerCookieFlag != 1) return InternalErrorJson.also {
            log.error("删除cookie出错,目标: $cookie, master: $masterCookie#$masterToken")
            log.error(Exception("堆栈信息").stackTraceToString())
        }

        return successJson(3103).also {
            log.info("为${masterCookie}删除一个影武者:${cookie}")
        }
    }

    /**
     * 为一个cookie添加备注
     * @return 回复客户端的JSON
     */
    @Transactional
    fun remarkCookie(masterCookie: String, masterToken: String, cookie: String, remarks: String): String {
        if (masterCookie == cookie){
            val cookieDB = dao.safeFindCookie(cookie, masterToken) ?: return errorJson(2101, "目标cookie不存在")
            if (!cookieDB.register) return errorJson(2102, "目标cookie未导入")
            val remarkCookieFlag = dao.remarkCookie(masterToken, remarks)
            if (remarkCookieFlag != 1) return InternalErrorJson.also {
                log.error("添加备注出错,目标cookie: $masterCookie#$masterToken")
                log.error(Exception("堆栈信息").stackTraceToString())
            }
            return successJson(3007).also {
                log.info("为${cookie}添加备注:${remarks}")
            }
        }else{
            val cookieDB = dao.findCookie(cookie) ?: return errorJson(2101, "目标cookie不存在")
            checkAnd(masterCookie, masterToken){} ?: return errorJson(2101, "主cookie不存在")

            if (cookieDB.mainCookie != masterCookie) return errorJson(2104, "目标cookie不是主cookie的影武者")
            val remarkCookieFlag = dao.remarkCookie(cookieDB.token, remarks)
            if (remarkCookieFlag != 1) return InternalErrorJson.also {
                log.error("添加备注出错,目标cookie: $masterCookie#$masterToken")
                log.error(Exception("堆栈信息").stackTraceToString())
            }
            return successJson(3007).also {
                log.info("为${cookie}添加备注:${remarks}")
            }
        }
    }

    fun singIn(cookie: String, token: String): String {
        TODO("Not yet implemented")
    }

    /**
     * 检查完整cookie的合法性,并切割返回
     * @return 分割后的cookie和token
     */
    private fun checkCookieAndTokenLegality(cookieAndToken: String): Pair<String, String>?{
        val list = cookieAndToken.split("#")
        if (list.size != 2) return null
        if (list[0].length != 8) return null
        if (list[1].length != 32) return null
        return Pair(list[0], list[1])
    }

    /**
     * 检查cookie是否存在,如果存在则执行传入的代码段
     * @return 代码段执行结果
     */
    private fun<T> checkAnd(cookie: String, token: String, block: () -> T): T?{
        val flag = dao.checkExist(cookie, token)
        return if (flag != 0) {
            if (flag > 1) {
                log.error("致命的错误,数据库表cookie中出现了不该出现的重复数据,这是不可能的,问题发生的目标: cookie: $cookie, token: $token")
                log.error(Exception("堆栈信息").stackTraceToString())
                exitApplication()
                return null
            }
            block()
        } else {
            null
        }
    }

}