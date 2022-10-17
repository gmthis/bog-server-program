package cn.xd.server.bog.dao

import cn.xd.server.bog.entity.cookie.CookieDB
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface CookieDAO {

    /**
     * 在数据库中插入一个新的cookie
     * @return 受影响的行数
     */
    @Insert("insert into cookie(cookie, token) values (#{cookie}, #{token})")
    fun newCookie(cookie: CookieDB): Int

    /**
     * 检查数据库中是否有指定的cookie
     * @return 返回1,如果返回的不是1,那该修bug了
     */
    @Select("select count(cookie) from cookie where cookie = #{cookieMain} and token = #{cookieToken}")
    fun checkExist(cookieMain: String, cookieToken: String): Int

    /**
     * 导入一个cookie
     * @return 受影响的行数,返回1
     */
    @Update("update cookie set register = true where token = #{token}")
    fun importCookie(token: String): Int

    /**
     * 删除一个影武者
     * @return 受影响的行数,返回1
     */
    @Update("update cookie set register = false, main_cookie = null where token = #{token}")
    fun removerCookie(token: String): Int

    /**
     * 将一个cookie设置成主cookie
     * @return 受影响的行数,返回1
     */
    @Update("update cookie set is_main_cookie = true where token = #{token}")
    fun setCookieIsMain(token: String): Int

    /**
     * 设置一个影武者cookie的主cookie
     * @return 受影响的行数,返回1
     */
    @Update("update cookie set main_cookie = #{master} where token = #{token}")
    fun setCookieMaster(token: String, master: String): Int

    /**
     * 检查一个cookie是否是主cookie
     * @return 是否为主cookie
     */
    @Select("select is_main_cookie from cookie where token = #{token}")
    fun checkCookieIsMain(token: String): Boolean

    /**
     * 查找指定cookie
     * @return cookie
     */
    @Select("select * from cookie where cookie = #{cookie} and token = #{token}")
    fun safeFindCookie(cookie: String, token: String): CookieDB?

    /**
     * 查找指定cookie
     * @return cookie
     */
    @Select("select * from cookie where cookie = #{cookie}")
    fun findCookie(cookie: String): CookieDB?

    /**
     * 查找一个cookie的所有影武者
     * @return cookie的所有影武者
     */
    @Select("select * from cookie where main_cookie = #{cookie}")
    fun findCookieOwnedByCookie(cookie: String): List<CookieDB>

    /**
     * 检查一个cookie是否被导入
     * @return 检查cookie是否被导入
     */
    @Select("select register from cookie where token = #{token}")
    fun checkImport(token: String): Boolean

    /**
     * 为一个cookie添加备注
     * @return 受影响的行数,返回1
     */
    @Update("update cookie set remark = #{remark} where token = #{token}")
    fun remarkCookie(token: String, remark: String): Int

    /**
     * 更新一个cookie的状态,这不会更新全部,只会更新一些时常变化的状态
     * @return 受影响的行数,返回1
     */
    @Update("update cookie set vip = #{vip}, sign = #{sign}, signtime = #{signtime}, point = #{point}, exp = #{exp} where cookie = #{cookie} and token = #{token}")
    fun updateCookie(cookie: CookieDB): Int

    /**
     * 检查一个cookie是否被ban
     * @return 是否被ban
     */
    @Select("select is_ban from cookie where cookie = #{cookie} and token = #{token}")
    fun checkBan(cookie: String, token: String): Boolean
}