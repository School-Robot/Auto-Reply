package tk.mcsog

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginData: AutoSavePluginData("Data") {
    @ValueDescription("群数据")
    var groupData: HashMap<String, GroupInfo> by value()

    @ValueDescription("词库数据")
    var dictData: HashMap<String, DictInfo> by value()
}