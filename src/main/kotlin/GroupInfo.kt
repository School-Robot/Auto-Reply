package tk.mcsog

import kotlinx.serialization.Serializable

@Serializable
data class GroupInfo(
    val groupNum: Long,
    var dictName: String,
    val qq: Long,
    var admin: Boolean = false,
    var permission: ArrayList<Long> = arrayListOf<Long>(1822245039L)
)