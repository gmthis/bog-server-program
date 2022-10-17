package cn.xd.server.bog.util.typehandler

import cn.xd.server.bog.entity.string.Image
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List::class)
class ImageJsonHandler: BaseTypeHandler<List<Image>?>(){
    override fun setNonNullParameter(ps: PreparedStatement?, i: Int, parameter: List<Image>?, jdbcType: JdbcType?) {
        ps?.setString(i, if(parameter == null) null else Json.encodeToString(parameter))
    }

    override fun getNullableResult(rs: ResultSet?, columnName: String?): List<Image>? {
        if (rs == null) return null
        val imagesString = rs.getString(columnName) ?: return null
        if (imagesString.isEmpty()) return null
        return Json.decodeFromString(imagesString)
    }

    override fun getNullableResult(rs: ResultSet?, columnIndex: Int): List<Image>? {
        if (rs == null) return null
        val imagesString = rs.getString(columnIndex)?: return null
        if (imagesString.isEmpty()) return null
        return Json.decodeFromString(imagesString)
    }

    override fun getNullableResult(cs: CallableStatement?, columnIndex: Int): List<Image>? {
        if (cs == null) return null
        val imagesString = cs.getString(columnIndex)?: return null
        if (imagesString.isEmpty()) return null
        return Json.decodeFromString(imagesString)
    }

}