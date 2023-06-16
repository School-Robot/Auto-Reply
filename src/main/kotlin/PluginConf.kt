package tk.mcsog

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginConf: AutoSavePluginConfig("Config"){
    @ValueDescription("管理员QQ")
    val manager: Long by value(1822245039L)

}