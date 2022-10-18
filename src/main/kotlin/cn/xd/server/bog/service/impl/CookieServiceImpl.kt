package cn.xd.server.bog.service.impl

import cn.xd.server.bog.dao.CookieDAO
import cn.xd.server.bog.entity.cookie.CookieDB
import cn.xd.server.bog.exitApplication
import cn.xd.server.bog.service.CookieService
import cn.xd.server.bog.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.annotation.Resource

@Service
class CookieServiceImpl : CookieService {

    @Resource
    private lateinit var dao: CookieDAO
    @Resource
    private lateinit var json: Json

    companion object {
        private val log = LoggerFactory.getLogger(CookieServiceImpl::class.java)

        private val cookieNotExist = errorJson(2101, "请求中至少有一个cookie不存在或被ban")
        private val cookieNotImport = errorJson(2102, "请求中至少有一个cookie未导入")
        private val cookieIsNotMasterCookie = errorJson(2103, "cookie不是一个主cookie")
        private val cookieIsNotSonOfMasterCookie = errorJson(2104, "目标cookie不是主cookie的影武者")
        private val cookieIsMasterCookie = errorJson(2105, "尝试对主cookie进行非法操作")
        private val tokenError = errorJson(2106, "token与数据库中不匹配")
        private val signTimeLessThanTime = errorJson(2107, "还没到下次可签到时间")

    }

    /**
     * 生成一个新的cookie
     * @return 回复客户端的JSON
     */
    @Transactional
    override fun newCookie(): String{
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
    override fun importCookie(master: String, cookieAdd: String): String {
        val (cookie, token) = checkCookieAndTokenLegality(cookieAdd) ?: return IllegalParameterJson
        if (master == "0"){
//            如果这个cookie没有问题,则将这个cookie设置为主cookie,并设置为已导入,
            val flag = checkAnd(cookie, token) {
                dao.setCookieIsMain(token)
                dao.importCookie(token)
            } ?: return cookieNotExist
            if (flag != 1) return InternalErrorJson.also {
                log.error("设置cookie导入状态时出错")
                log.error(Exception("堆栈信息").stackTraceToString())
            }
            return successJson(3104, JsonArray(listOf(
                json.encodeToJsonElement(dao.findCookie(cookie)!!.getRemarkStyle())
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
            } ?: return cookieNotExist

            if (!dao.checkImport(masterToken)) return cookieNotImport
            if (!dao.checkCookieIsMain(masterToken)) return cookieIsNotMasterCookie
            val importFlag = dao.importCookie(token)
            val setFlag = dao.setCookieMaster(token, masterCookie)
//            检查两次操作是否成功
            if (setFlag != 1 && importFlag != 1) return InternalErrorJson.also {
                log.error("设置cookie导入状态时出错")
                log.error(Exception("堆栈信息").stackTraceToString())
            }

            return successJson(3104, JsonArray(listOf(
                json.encodeToJsonElement(dao.findCookie(cookie)!!.getRemarkStyle())
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
    override fun removeCookie(masterToken: String, masterCookie: String, cookie: String): String {
        if (masterCookie == cookie) return IllegalParameterJson

        val cookieDB = dao.findCookie(cookie) ?: return cookieNotExist

        checkAnd(masterCookie, masterToken){

        } ?: return cookieNotExist

//        1.检查cookie的主cookie是否是masterCookie
        if (cookieDB.isMainCookie) return cookieIsMasterCookie
        if (!cookieDB.register) return cookieNotImport
        if (cookieDB.mainCookie != masterCookie) return cookieIsNotSonOfMasterCookie

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
    override fun remarkCookie(masterCookie: String, masterToken: String, cookie: String, remarks: String): String {
        if (masterCookie == cookie){
            val cookieDB = dao.safeFindCookie(cookie, masterToken) ?: return cookieNotExist
            if (!cookieDB.register) return cookieNotImport
            val remarkCookieFlag = dao.remarkCookie(masterToken, remarks)
            if (remarkCookieFlag != 1) return InternalErrorJson.also {
                log.error("添加备注出错,目标cookie: $masterCookie#$masterToken")
                log.error(Exception("堆栈信息").stackTraceToString())
            }
            return successJson(3007).also {
                log.info("为${cookie}添加备注:${remarks}")
            }
        }else{
            val cookieDB = dao.findCookie(cookie) ?: return cookieNotExist
            checkAnd(masterCookie, masterToken){} ?: return cookieNotExist

            if (cookieDB.mainCookie != masterCookie) return cookieIsNotSonOfMasterCookie
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

    /**
     * 为cookie增加一次签到计数
     */
    @Transactional
    override fun singIn(cookie: String, token: String): String {
        val cookieDB = dao.safeFindCookie(cookie, token) ?: return cookieNotExist
        if (!cookieDB.register) return cookieNotImport
        if (cookieDB.isBan) return cookieNotExist

//        检查是否符合签到要求
        var singFlag = false
        if (cookieDB.signtime == 0L) singFlag = true
        val calendar: Calendar = Calendar.getInstance()
        if (!singFlag){
            if (cookieDB.signtime <= calendar.timeInMillis) singFlag = true
        }

        if (!singFlag) return signTimeLessThanTime

        val copy = cookieDB.copy(
            sign = cookieDB.sign + 1,
            point = cookieDB.point + 20,
            exp = cookieDB.exp + 100,
            signtime = calendar.also {
                it.add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
        )

        val updateCookieFlag = dao.updateCookie(copy)

        if (updateCookieFlag != 1) return InternalErrorJson.also {
            log.error("更新签到状态时出现错误, 目标: $cookie#$token")
        }

        return successJson(7010, json.encodeToJsonElement(copy.getSignInfo())).also {
            log.info("$cookie 签到, 当前次数: ${copy.sign}, 下次可签到时间: ${copy.signtime}")
        }
    }

    /**
     * 查找一个cookie的信息
     * @return cookie信息
     */
    @Transactional
    override fun findCookieInfo(cookie: String, token: String): String {
        val findCookie = dao.safeFindCookie(cookie, token) ?: return cookieNotExist
        if (!findCookie.register) return cookieNotImport

        val cookieInfo = findCookie.getCookieInfo()

        if (findCookie.isMainCookie){
            for (cookieDB in dao.findCookieOwnedByCookie(cookie)) {
                cookieInfo.list.add(cookieDB.getRemarkStyle())
            }
        }

        return successJson(6001, json.encodeToJsonElement(cookieInfo))
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
            val checkBanFlag = dao.checkBan(cookie, token)
            if (checkBanFlag) return null
            block()
        } else {
            null
        }
    }

}