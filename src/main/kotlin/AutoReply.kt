package tk.mcsog

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info

object AutoReply : KotlinPlugin(
        JvmPluginDescription(
                id = "tk.mcsog.auto-reply",
                name = "Auto Reply",
                version = "0.1.4",
        ) {
            author("MCSOG")
        }
) {
    override fun onEnable() {
        PluginConf.reload()
        PluginData.reload()
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            val m: String = this.message.serializeToMiraiCode()

            // help
            if (m == "/dicthelp") {
                this.group.sendMessage(At(this.sender.id)+PlainText("/dictadd 触发语 回复语-添加词条\n/dictdel 触发语-删除词条\n/dictcreate 词库名-创建词库\n/dictuse 词库名-使用词库\n/dictdel 词库名-删除词库\n/dictman-开启或关闭权限限制\n/dictmanadd QQ-添加权限\n/dictmandel QQ-删除权限"))
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
                            this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
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
                        this.group.sendMessage(At(this.sender.id)+PlainText("回复语不能为空"))
                        return@subscribeAlways
                    }
                    PluginData.dictData[dn]!!.dictList[m_split[1]] = m_split[2]
                    this.group.sendMessage(At(this.sender.id)+PlainText("添加成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
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
                            this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }else{
                        it.dictName
                    }
                }
                if (new){
                    this.group.sendMessage(At(this.sender.id)+PlainText("无词条"))
                    return@subscribeAlways
                }
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.dictData[dn]!!.dictList.remove(m_split[1])
                    this.group.sendMessage(At(this.sender.id)+PlainText("删除成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // create
            if (m.startsWith("/dictcreate")&&m.length>12){
                val c: Char = m[11]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    PluginData.dictData[m_split[1]]?.let {
                        this.group.sendMessage(At(this.sender.id)+PlainText("词库已存在"))
                        return@subscribeAlways
                    }
                    if (this.sender.id == PluginConf.manager){
                        val nd = DictInfo(m_split[1])
                        PluginData.dictData[m_split[1]] = nd
                        this.group.sendMessage(At(this.sender.id)+PlainText("创建成功"))
                        return@subscribeAlways
                    }else{
                        this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                        return@subscribeAlways
                    }
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
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
                                this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                                return@subscribeAlways
                            }
                            val ng = GroupInfo(this.group.id, m_split[1], this.bot.id)
                            PluginData.dictData[this.group.id.toString()]?:let {
                                val nd = DictInfo(this.group.id.toString())
                                PluginData.dictData[this.group.id.toString()] = nd
                            }
                            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()] = ng
                            this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                            return@subscribeAlways
                        }else{
                            this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(At(this.sender.id)+PlainText("词库不存在"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // del
            if (m.startsWith("/dictdel")&&m.length>9){
                val c: Char = m[8]
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
                            this.group.sendMessage(At(this.sender.id)+PlainText("删除成功"))
                            return@subscribeAlways
                        }else{
                            this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(At(this.sender.id)+PlainText("词库不存在"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // man
            if (m == "/dictman"){
                if (this.sender.id == PluginConf.manager){
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let {
                        it.admin = !it.admin
                        this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                        return@subscribeAlways
                    }
                    val ng = GroupInfo(this.group.id, this.group.id.toString(), this.bot.id, true)
                    PluginData.dictData[this.group.id.toString()]?:let {
                        val nd = DictInfo(this.group.id.toString())
                        PluginData.dictData[this.group.id.toString()] = nd
                    }
                    PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()] = ng
                    this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
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
                                    this.group.sendMessage(At(this.sender.id)+PlainText("已添加"))
                                    return@subscribeAlways
                                }else{
                                    it.permission.add(qq)
                                    this.group.sendMessage(At(this.sender.id)+PlainText("添加成功"))
                                    return@subscribeAlways
                                }
                            }else{
                                this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                                return@subscribeAlways
                            }
                        }else{
                            this.group.sendMessage(At(this.sender.id)+PlainText("未开启权限限制"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(At(this.sender.id)+PlainText("未开启自动回复"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
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
                                    this.group.sendMessage(At(this.sender.id)+PlainText("删除成功"))
                                    return@subscribeAlways
                                }else{
                                    this.group.sendMessage(At(this.sender.id)+PlainText("无权限"))
                                    return@subscribeAlways
                                }
                            }else{
                                this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                                return@subscribeAlways
                            }
                        }else{
                            this.group.sendMessage(At(this.sender.id)+PlainText("未开启权限限制"))
                            return@subscribeAlways
                        }
                    }
                    this.group.sendMessage(At(this.sender.id)+PlainText("未开启自动回复"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }

            // auto-reply
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList[m]?.deserializeMiraiCode()?.let { it3 -> this.group.sendMessage(it3) }
                }
            }
        }

        globalEventChannel().subscribeAlways<NudgeEvent> {
            if (this.subject is Group){
                PluginData.groupData[this.subject.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                    if (this.target.id == this.bot.id){
                        PluginData.dictData[it1.dictName]?.let { it2 ->
                            it2.dictList["NudgeSelf"]?.replace("\${action}", this.action)?.replace("\${suffix}", this.suffix)?.replace("\${from}", this.from.id.toString())?.replace("\${target}", this.target.id.toString())?.deserializeMiraiCode()?.let { it3 -> this.subject.sendMessage(it3) }
                        }
                    }else{
                        PluginData.dictData[it1.dictName]?.let { it2 ->
                            it2.dictList["Nudge"]?.replace("\${action}", this.action)?.replace("\${suffix}", this.suffix)?.replace("\${from}", this.from.id.toString())?.replace("\${target}", this.target.id.toString())?.deserializeMiraiCode()?.let { it3 -> this.subject.sendMessage(it3) }
                        }
                    }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberJoinEvent.Active> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberJoin"]?.replace("\${member}", this.member.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 -> this.group.sendMessage(it3)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberJoinEvent.Invite> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberJoinInvite"]?.replace("\${member}", this.member.id.toString())?.replace("\${invitor}", this.invitor.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 -> this.group.sendMessage(it3)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberLeaveEvent.Quit> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberLeave"]?.replace("\${member}", this.member.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 -> this.group.sendMessage(it3)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberLeaveEvent.Kick> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberLeaveKick"]?.replace("\${member}", this.member.id.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 -> this.group.sendMessage(it3)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberMuteEvent> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberMute"]?.replace("\${member}", this.member.id.toString())?.replace("\${durationSeconds}", this.durationSeconds.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 -> this.group.sendMessage(it3)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberUnmuteEvent> {
            PluginData.groupData[this.group.id.toString()+"-"+this.bot.id.toString()]?.let { it1 ->
                PluginData.dictData[it1.dictName]?.let { it2 ->
                    it2.dictList["MemberUnmute"]?.replace("\${member}", this.member.id.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it3 -> this.group.sendMessage(it3)}
                }
            }
        }
    }

    override fun onDisable() {
        PluginConf.save()
        PluginData.save()
        logger.info { "Plugin disabled" }
    }
}