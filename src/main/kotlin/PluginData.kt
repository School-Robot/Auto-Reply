package tk.mcsog

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginData: AutoSavePluginData("Data") {
    @ValueDescription("群数据")
    val groupData: MutableMap<String, GroupInfo> by value()

    @ValueDescription("词库数据")
    val dictData: MutableMap<String, DictInfo> by value()

    @ValueDescription("临时词条")
    val tempMsg: MutableMap<String, TempMsg> by value()
}