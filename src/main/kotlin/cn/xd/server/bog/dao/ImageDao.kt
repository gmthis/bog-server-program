package cn.xd.server.bog.dao

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface ImageDao {

    @Select("select is_ban from image where url = #{url}")
    fun checkBan(url: String): Boolean?

    @Insert("insert into image(url, ext) VALUES (#{url}, #{ext})")
    fun addImage(url: String, ext: String): Int

}
