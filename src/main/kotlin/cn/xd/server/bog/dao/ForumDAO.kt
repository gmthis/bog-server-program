package cn.xd.server.bog.dao

import cn.xd.server.bog.entity.forum.ForumDB
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface ForumDAO {

    @Select("select * from forum order by `rank`")
    fun getAllForum(): List<ForumDB>

}