package tk.mcsog

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.isUploaded
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset

object AutoReply : KotlinPlugin(
        JvmPluginDescription(
                id = "tk.mcsog.auto-reply",
                name = "Auto Reply",
                version = "0.2.0",
        ) {
            author("MCSOG")
        }
) {
    override fun onEnable() {
        PluginConf.reload()
        PluginData.reload()
        File(dataFolder.absolutePath+File.separatorChar+"image").mkdirs()
        File(dataFolder.absolutePath+File.separatorChar+"audio").mkdirs()
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            val m: String = this.message.serializeToMiraiCode()
            PluginData.tempMsg[this.group.id.toString()+"-"+this.sender.id.toString()]?.let {
                if (it.state == 0){
                    it.msg = m
                    it.state = 1
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("请发送回复语"))
                    return@subscribeAlways
                }else if (it.state == 1){
                    it.reply = RecMsg(this.message).serializeToMiraiCode()
                    PluginData.dictData[it.dn]?.let { it1 ->
                        it1.dictList[it.msg] = it.reply
                    }
                    PluginData.tempMsg.remove(this.group.id.toString()+"-"+this.sender.id.toString())
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("添加成功"))
                    return@subscribeAlways
                }
            }

            // help
            if (m == "/dicthelp") {
                this.group.sendMessage(QuoteReply(this.message)+PlainText("/dictadd 触发语 回复语-添加词条\n/dictadd-交互式添加\n/dictdel 触发语-删除词条\n/dictcreate 词库名-创建词库\n/dictuse 词库名-使用词库\n/dictcancel-取消词库\n/dictsdel 词库名-删除词库\n/dictman-开启或关闭权限限制\n/dictmanadd QQ-添加权限\n/dictmandel QQ-删除权限"))
                return@subscribeAlways
            }

            // add
            if (m == "/dictadd"){
                var new = true
                var dn = "test"
                PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                    new = false
                    dn = if (it.admin){
                        if (this.sender.id in it.permission){
                            it.dictName
                        }else{
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }else{
                        it.dictName
                    }
                }
                if (new){
                    val ng = GroupInfo(this.group.id, this.group.id.toString(), this.bot.id)
                    PluginData.dictData[this.group.id.toString()]?:let {
                        val nd = DictInfo(this.group.id.toString())
                        PluginData.dictData[this.group.id.toString()] = nd
                    }
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()] = ng
                    dn = this.group.id.toString()
                }
                PluginData.tempMsg[this.group.id.toString()+"-"+this.sender.id.toString()] = TempMsg(this.group.id, dn)
                this.group.sendMessage(QuoteReply(this.message)+PlainText("请发送触发语"))
                return@subscribeAlways
            }

            // add
            if (m.startsWith("/dictadd")&&m.length>11){
                var new = true
                var dn = "test"
                PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                    new = false
                    dn = if (it.admin){
                        if (this.sender.id in it.permission){
                            it.dictName
                        }else{
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }else{
                        it.dictName
                    }
                }
                if (new){
                    val ng = GroupInfo(this.group.id, this.group.id.toString(), this.bot.id)
                    PluginData.dictData[this.group.id.toString()]?:let {
                        val nd = DictInfo(this.group.id.toString())
                        PluginData.dictData[this.group.id.toString()] = nd
                    }
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()] = ng
                    dn = this.group.id.toString()
                }
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 3){
                    if (m_split[2] == ""){
                        this.group.sendMessage(QuoteReply(this.message)+PlainText("回复语不能为空"))
                        return@subscribeAlways
                    }
                    val mc: MessageChain = m_split[2].deserializeMiraiCode()
                    PluginData.dictData[dn]!!.dictList[m_split[1]] = RecMsg(mc).serializeToMiraiCode()
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("添加成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // del
            if (m.startsWith("/dictdel")&&m.length>9){
                var new = true
                var dn = "test"
                PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                    new = false
                    dn = if (it.admin){
                        if (this.sender.id in it.permission){
                            it.dictName
                        }else{
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }else{
                        it.dictName
                    }
                }
                if (new){
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("无词条"))
                    return@subscribeAlways
                }
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.dictData[dn]!!.dictList.remove(m_split[1])
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("删除成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // create
            if (m.startsWith("/dictcreate")&&m.length>12){
                val c: Char = m[11]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.dictData[m_split[1]]?.let {
                        this.group.sendMessage(QuoteReply(this.message)+PlainText("词库已存在"))
                        return@subscribeAlways
                    }
                    if (this.sender.id == PluginConf.manager){
                        val nd = DictInfo(m_split[1])
                        PluginData.dictData[m_split[1]] = nd
                        this.group.sendMessage(QuoteReply(this.message)+PlainText("创建成功"))
                        return@subscribeAlways
                    }else{
                        this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                        return@subscribeAlways
                    }
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // use
            if (m.startsWith("/dictuse")&&m.length>9){
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.dictData[m_split[1]]?.let {
                        if (this.sender.id == PluginConf.manager){
                            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                                it.dictName = m_split[1]
                                this.group.sendMessage(QuoteReply(this.message)+PlainText("切换成功"))
                                return@subscribeAlways
                            }
                            val ng = GroupInfo(this.group.id, m_split[1], this.bot.id)
                            PluginData.dictData[this.group.id.toString()]?:let {
                                val nd = DictInfo(this.group.id.toString())
                                PluginData.dictData[this.group.id.toString()] = nd
                            }
                            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()] = ng
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("切换成功"))
                            return@subscribeAlways
                        }else{
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("词库不存在"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // cancel
            if (m == "/dictcancel"){
                PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                    if (this.sender.id == PluginConf.manager){
                        it.dictName = it.groupNum.toString()
                        this.group.sendMessage(QuoteReply(this.message)+PlainText("取消成功"))
                        return@subscribeAlways
                    }else{
                        this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                        return@subscribeAlways
                    }
                }
                this.group.sendMessage(QuoteReply(this.message)+PlainText("无词库"))
                return@subscribeAlways
            }

            // del
            if (m.startsWith("/dictsdel")&&m.length>9){
                val c: Char = m[9]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.dictData[m_split[1]]?.let {
                        if (this.sender.id == PluginConf.manager){
                            PluginData.dictData.remove(it.dictName)
                            PluginData.groupData.forEach { it1 ->
                                if (it1.value.dictName == it.dictName){
                                    it1.value.dictName = it1.value.groupNum.toString()
                                }
                            }
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("删除成功"))
                            return@subscribeAlways
                        }else{
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("词库不存在"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // man
            if (m == "/dictman"){
                if (this.sender.id == PluginConf.manager){
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                        it.admin = !it.admin
                        this.group.sendMessage(QuoteReply(this.message)+PlainText("切换成功"))
                        return@subscribeAlways
                    }
                    val ng = GroupInfo(this.group.id, this.group.id.toString(), this.bot.id, true)
                    PluginData.dictData[this.group.id.toString()]?:let {
                        val nd = DictInfo(this.group.id.toString())
                        PluginData.dictData[this.group.id.toString()] = nd
                    }
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()] = ng
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("切换成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                    return@subscribeAlways
                }
            }

            // manadd
            if (m.startsWith("/dictmanadd")&&m.length>12){
                val c: Char = m[11]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                        if (it.admin){
                            if (this.sender.id in it.permission){
                                val qq: Long = m_split[1].toLong()
                                if (qq in it.permission){
                                    this.group.sendMessage(QuoteReply(this.message)+PlainText("已添加"))
                                    return@subscribeAlways
                                }else{
                                    it.permission.add(qq)
                                    this.group.sendMessage(QuoteReply(this.message)+PlainText("添加成功"))
                                    return@subscribeAlways
                                }
                            }else{
                                this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                                return@subscribeAlways
                            }
                        }else{
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("未开启权限限制"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("未开启自动回复"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // mandel
            if (m.startsWith("/dictmandel")&&m.length>12){
                val c: Char = m[11]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                        if (it.admin){
                            if (this.sender.id in it.permission){
                                val qq: Long = m_split[1].toLong()
                                if (qq in it.permission){
                                    it.permission.remove(qq)
                                    this.group.sendMessage(QuoteReply(this.message)+PlainText("删除成功"))
                                    return@subscribeAlways
                                }else{
                                    this.group.sendMessage(QuoteReply(this.message)+PlainText("无权限"))
                                    return@subscribeAlways
                                }
                            }else{
                                this.group.sendMessage(QuoteReply(this.message)+PlainText("权限不足"))
                                return@subscribeAlways
                            }
                        }else{
                            this.group.sendMessage(QuoteReply(this.message)+PlainText("未开启权限限制"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("未开启自动回复"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(QuoteReply(this.message)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // auto-reply
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList[m]?.replace("\${group}",this.group.id.toString())?.replace("\${sender}",this.sender.id.toString())?.deserializeMiraiCode()?.let { it3 ->
                        this.group.sendMessage(QuoteReply(this.message)+ SendMsg(this.bot, this.group, it3))
                    }
                }
            }
        }

        globalEventChannel().subscribeAlways<NudgeEvent> {
            if (this.subject is Group){
                PluginData.groupData[this.subject.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                    if (this.target.id == this.bot.id){
                        PluginData.dictData[it1.dictName]?.let { it2 ->
                            it2.dictList["NudgeSelf"]?.replace("\${action}", this.action)?.replace("\${suffix}", this.suffix)?.replace("\${from}", this.from.id.toString())?.replace("\${target}", this.target.id.toString())?.deserializeMiraiCode()?.let { it3 ->
                                this.subject.sendMessage(SendMsg(this.bot, this.subject, it3))
                            }
                        }
                    }else{
                        PluginData.dictData[it1.dictName]?.let { it2 ->
                            it2.dictList["Nudge"]?.replace("\${action}", this.action)?.replace("\${suffix}", this.suffix)?.replace("\${from}", this.from.id.toString())?.replace("\${target}", this.target.id.toString())?.deserializeMiraiCode()?.let { it3 ->
                                this.subject.sendMessage(SendMsg(this.bot, this.subject, it3))
                            }
                        }
                    }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberJoinEvent.Active> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberJoin"]?.replace("\${member}", this.member.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 ->
                            this.group.sendMessage(SendMsg(this.bot, this.group, it3))
                        }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberJoinEvent.Invite> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberJoinInvite"]?.replace("\${member}", this.member.id.toString())?.replace("\${invitor}", this.invitor.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 ->
                            this.group.sendMessage(SendMsg(this.bot, this.group, it3))
                        }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberLeaveEvent.Quit> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberLeave"]?.replace("\${member}", this.member.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 ->
                            this.group.sendMessage(SendMsg(this.bot, this.group, it3))
                        }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberLeaveEvent.Kick> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberLeaveKick"]?.replace("\${member}", this.member.id.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 ->
                            this.group.sendMessage(SendMsg(this.bot, this.group, it3))
                        }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberMuteEvent> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberMute"]?.replace("\${member}", this.member.id.toString())?.replace("\${durationSeconds}", this.durationSeconds.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 ->
                            this.group.sendMessage(SendMsg(this.bot, this.group, it3))
                        }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberUnmuteEvent> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberUnmute"]?.replace("\${member}", this.member.id.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 ->
                            this.group.sendMessage(SendMsg(this.bot, this.group, it3))
                        }
                }
            }
        }
    }

    fun RecMsg(msg: MessageChain): MessageChain{
        var mc: MessageChain = emptyMessageChain()
        for (mc_single in msg){
            if (mc_single is Image){
                launch {
                    File(dataFolder.absolutePath + File.separatorChar + "image" + File.separatorChar + mc_single.imageId).writeBytes(URL(mc_single.queryUrl()).readBytes())
                }
                mc+=mc_single
            }else if (mc_single is OnlineAudio){
                launch {
                    File(dataFolder.absolutePath + File.separatorChar + "audio" + File.separatorChar + mc_single.filename).writeBytes(URL(mc_single.urlForDownload).readBytes())
                }
                mc+=PlainText("\${mirai:audio:"+mc_single.filename+"}")
            }else{
                mc+=mc_single
            }
        }
        return mc
    }

    suspend fun SendMsg(bot: Bot, target: Contact, msg: MessageChain): MessageChain{
        val group:Group = target as Group
        var mc: MessageChain = emptyMessageChain()
        for (mc_single in msg){
            if (mc_single is Image){
                if (!mc_single.isUploaded(bot)){
                    File(dataFolder.absolutePath + File.separatorChar + "image" + File.separatorChar + mc_single.imageId).let { it ->
                        if (it.exists()){
                            it.toExternalResource().use { res ->
                                val i: Image = group.uploadImage(res)
                                mc+=i
                            }
                        }else{
                            mc+=mc_single
                        }
                    }
                }else{
                    mc+=mc_single
                }
            }else if(mc_single is PlainText){
                if ("\${mirai:" in mc_single.content){
                    var m:String = mc_single.content
                    while ("\${mirai:" in m){
                        mc+=PlainText(m.substring(0,m.indexOf("\${mirai:")))
                        m=m.substring(m.indexOf("\${mirai:"))
                        if ("}" in m){
                            val mirai:String = m.substring(2,m.indexOf("}"))
                            val mirai_split = mirai.split(":")
                            if(mirai_split.size>=3) {
                                when (mirai_split[1]) {
                                    "audio" -> {
                                        File(dataFolder.absolutePath + File.separatorChar + "audio" + File.separatorChar + mirai_split[2]).let { it ->
                                            if (it.exists()) {
                                                it.toExternalResource().use { res ->
                                                    val i: Audio = group.uploadAudio(res)
                                                    mc+=i
                                                }
                                            } else {
                                                mc+=mc_single
                                            }
                                        }
                                    }

                                    "image" -> {
                                        if (mirai_split[2].startsWith("http")) {
                                            URL(URLDecoder.decode(mirai_split[2])).readBytes().toExternalResource().use {
                                                val i: Image = group.uploadImage(it)
                                                mc+=i
                                            }
                                        } else {
                                            File(dataFolder.absolutePath + File.separatorChar + "image" + File.separatorChar + mirai_split[2]).let {
                                                if (it.exists()) {
                                                    it.toExternalResource().use { res ->
                                                        val i: Image = group.uploadImage(res)
                                                        mc+=i
                                                    }
                                                } else {
                                                    mc+=mc_single
                                                }
                                            }
                                        }
                                    }

                                    "mute" -> {
                                        if (mirai_split.size == 3) {
                                            group.members[mirai_split[2].toLong()]?.unmute()
                                        } else if (mirai_split.size == 4) {
                                            group.members[mirai_split[2].toLong()]?.mute(mirai_split[3].toInt())
                                        }
                                    }

                                    "unmute" -> {
                                        group.members[mirai_split[2].toLong()]?.unmute()
                                    }

                                    "nudge" -> {
                                        group.members[mirai_split[2].toLong()]?.nudge()?.sendTo(group)
                                    }

                                    "at" -> {
                                        if (mirai_split[2] == "all") {
                                            mc+=AtAll
                                        } else {
                                            mc+=At(mirai_split[2].toLong())
                                        }
                                    }
                                }
                            }
                            m=m.substring(m.indexOf("}")+1)
                        }
                    }
                    mc+=PlainText(m)
                }else{
                    mc+=mc_single
                }
            }else{
                mc+=mc_single
            }
        }
        return mc
    }

    override fun onDisable() {
        PluginConf.save()
        PluginData.save()
        logger.info { "Plugin disabled" }
    }
}