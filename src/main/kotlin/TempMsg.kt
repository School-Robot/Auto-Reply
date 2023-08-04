package tk.mcsog

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.emptyMessageChain

@Serializable
data class TempMsg(
    val group:Long,
    val dn:String,
    var msg:String = "",
    var reply:String = "",
    var state:Int = 0,

)
