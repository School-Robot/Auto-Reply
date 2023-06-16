package tk.mcsog

import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info
import java.io.File

object AutoReply : KotlinPlugin(
        JvmPluginDescription(
                id = "tk.mcsog.auto-reply",
                name = "Auto Reply",
                version = "0.1.0",
        ) {
            author("MCSOG")
        }
) {
    private val GroupDir: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "groups").also { it.mkdirs() } }
    private val DictDir: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "dicts").also { it.mkdirs() } }
    private val testGroup: GroupInfo = GroupInfo(12345L, "test", 12345L)
    private val testDict: HashMap<String, DictInfo> = hashMapOf("test" to DictInfo("test", hashMapOf("test" to "test")))
    private var GroupList: ArrayList<GroupInfo> = arrayListOf(testGroup)
    private var DictList: HashMap<String, DictInfo> = testDict
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        GroupDir.listFiles()?.forEach {
            if (it.name.endsWith(".json")){
                GroupList.add(Json.decodeFromString(GroupInfo.serializer(),it.readText()))
            }
        }
        DictDir.listFiles()?.forEach {
            if (it.name.endsWith(".json")) {
                val d: DictInfo = Json.decodeFromString(DictInfo.serializer(), it.readText())
                DictList.put(d.dictName, d)
            }
        }
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            val m: String = this.message.serializeToMiraiCode()
            if (m.equals("/dicthelp")) {
                this.group.sendMessage(At(this.sender.id)+PlainText("/dictadd 触发语 回复语-添加词条\n/dictdel 触发语-删除词条\n/dictcreate 词库名-创建词库\n/dictuse 词库名-使用词库\n/dictdel 词库名-删除词库\n/dictman-开启或关闭权限限制\n/dictmanadd QQ-添加权限\n/dictmandel QQ-删除权限"))
            }
            if (m.startsWith("/dictadd")&&m.length>11){
                var new = true
                var dn = "test"
                GroupList.forEach {
                    if (it.groupNum == this.group.id && it.qq == this.bot.id){
                        new=false
                        if (it.admin){
                            if (this.sender.id in it.permission){
                                dn = it.dictName
                            }else{
                                this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                                return@subscribeAlways
                            }
                        }else{
                            dn = it.dictName
                        }
                    }
                }
                if (new){
                    val ng = GroupInfo(this.group.id, this.group.id.toString(), this.bot.id)
                    val nd = DictInfo(this.group.id.toString())
                    GroupList.add(ng)
                    DictList.put(this.group.id.toString(), nd)
                    dn = this.group.id.toString()
                }
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 3){
                    DictList[dn]!!.dictList.put(m_split[1], m_split[2])
                    this.group.sendMessage(At(this.sender.id)+PlainText("添加成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }
            if (m.startsWith("/dictdel")&&m.length>9){
                var new = true
                var dn = "test"
                GroupList.forEach {
                    if (it.groupNum == this.group.id && it.qq == this.bot.id){
                        new=false
                        if (it.admin){
                            if (this.sender.id in it.permission){
                                dn = it.dictName
                            }else{
                                this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                                return@subscribeAlways
                            }
                        }else{
                            dn = it.dictName
                        }
                    }
                }
                if (new){
                    this.group.sendMessage(At(this.sender.id)+PlainText("无词条"))
                    return@subscribeAlways
                }
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    DictList[dn]!!.dictList.remove(m_split[1])
                    this.group.sendMessage(At(this.sender.id)+PlainText("删除成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }
            if (m.startsWith("/dictcreate")&&m.length>12){
                val c: Char = m[11]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    DictList.forEach {
                        if (it.key.equals(m_split[1])){
                            this.group.sendMessage(At(this.sender.id)+PlainText("词库已存在"))
                            return@subscribeAlways
                        }
                    }
                    if (this.sender.id == 1822245039L){
                        val nd = DictInfo(m_split[1])
                        DictList.put(m_split[1], nd)
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
            if (m.startsWith("/dictuse")&&m.length>9){
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    val d: DictInfo? = DictList[m_split[1]]
                    if (d!=null){
                        if (this.sender.id == 1822245039L){
                            GroupList.forEach {
                                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                                    it.dictName = m_split[1]
                                    this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                                    return@subscribeAlways
                                }
                            }
                            val ng = GroupInfo(this.group.id, m_split[1], this.bot.id)
                            val nd = DictInfo(this.group.id.toString())
                            GroupList.add(ng)
                            DictList.put(this.group.id.toString(), nd)
                            this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                            return@subscribeAlways
                        }else{
                            this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }else{
                        this.group.sendMessage(At(this.sender.id)+PlainText("词库不存在"))
                        return@subscribeAlways
                    }
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }
            if (m.startsWith("/dictdel")&&m.length>9){
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    val d: DictInfo? = DictList[m_split[1]]
                    if (d != null){
                        if (this.sender.id == 1822245039L){
                            GroupList.forEach {
                                if (it.dictName == d.dictName){
                                    it.dictName = it.groupNum.toString()
                                }
                            }
                            val df = File(DictDir.absolutePath+File.separatorChar+MD5Util.encodeToHex(d.dictName)+".json")
                            if (df.exists()){
                                df.delete()
                            }
                            DictList.remove(d.dictName)
                            this.group.sendMessage(At(this.sender.id)+PlainText("删除成功"))
                            return@subscribeAlways
                        }else{
                            this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }else{
                        this.group.sendMessage(At(this.sender.id)+PlainText("词库不存在"))
                        return@subscribeAlways
                    }
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }
            if (m.startsWith("/dictdel")&&m.length>9){
                val c: Char = m[8]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    val d: DictInfo? = DictList[m_split[1]]
                    if (d != null){
                        if (this.sender.id == 1822245039L){
                            GroupList.forEach {
                                if (it.dictName == d.dictName){
                                    it.dictName = it.groupNum.toString()
                                }
                            }
                            File(DictDir.absolutePath+File.separatorChar+MD5Util.encodeToHex(d.dictName)+".json").delete()
                            DictList.remove(d.dictName)
                            this.group.sendMessage(At(this.sender.id)+PlainText("删除成功"))
                            return@subscribeAlways
                        }else{
                            this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                            return@subscribeAlways
                        }
                    }else{
                        this.group.sendMessage(At(this.sender.id)+PlainText("词库不存在"))
                        return@subscribeAlways
                    }
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }
            if (m.equals("/dictman")){
                if (this.sender.id == 1822245039L){
                    GroupList.forEach {
                        if (it.groupNum == this.group.id && it.qq == this.bot.id){
                            it.admin = !it.admin
                            this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                            return@subscribeAlways
                        }
                    }
                    val ng = GroupInfo(this.group.id, this.group.id.toString(), this.bot.id, true)
                    val nd = DictInfo(this.group.id.toString())
                    GroupList.add(ng)
                    DictList.put(this.group.id.toString(), nd)
                    this.group.sendMessage(At(this.sender.id)+PlainText("切换成功"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("权限不足"))
                    return@subscribeAlways
                }
            }
            if (m.startsWith("/dictmanadd")&&m.length>12){
                val c: Char = m[11]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    GroupList.forEach {
                        if (this.group.id == it.groupNum && this.bot.id == it.qq){
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
                    }
                    this.group.sendMessage(At(this.sender.id)+PlainText("未开启自动回复"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }
            if (m.startsWith("/dictmandel")&&m.length>12){
                val c: Char = m[11]
                val m_split = m.split(c)
                if (m_split.size == 2){
                    GroupList.forEach {
                        if (this.group.id == it.groupNum && this.bot.id == it.qq){
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
                    }
                    this.group.sendMessage(At(this.sender.id)+PlainText("未开启自动回复"))
                    return@subscribeAlways
                }else{
                    this.group.sendMessage(At(this.sender.id)+PlainText("格式错误"))
                    return@subscribeAlways
                }
            }
            GroupList.forEach {
                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                    DictList[it.dictName]!!.dictList[m]?.deserializeMiraiCode()
                        ?.let { it1 -> this.group.sendMessage(it1)}
                }
            }
        }

        globalEventChannel().subscribeAlways<NudgeEvent> {
            if (it.subject is Group){
                GroupList.forEach {
                    if (it.groupNum == this.subject.id && it.qq == this.bot.id){
                        if (this.target.id == this.bot.id){
                            DictList[it.dictName]!!.dictList["NudgeSelf"]?.replace("\${from}", this.from.id.toString())?.replace("\${target}", this.target.id.toString())?.replace("\${action}", this.action)?.replace("\${suffix}",this.suffix)?.deserializeMiraiCode()
                                ?.let { it1 -> this.subject.sendMessage(it1)}
                        }else{
                            DictList[it.dictName]!!.dictList["Nudge"]?.replace("\${from}", this.from.id.toString())?.replace("\${target}", this.target.id.toString())?.replace("\${action}", this.action)?.replace("\${suffix}",this.suffix)?.deserializeMiraiCode()
                                ?.let { it1 -> this.subject.sendMessage(it1)}
                        }
                    }
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberJoinEvent.Active> {
            GroupList.forEach {
                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                    DictList[it.dictName]!!.dictList["MemberJoin"]?.replace("\${member}", this.member.id.toString())?.deserializeMiraiCode()
                        ?.let { it1 -> this.group.sendMessage(it1)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberJoinEvent.Invite> {
            GroupList.forEach {
                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                    DictList[it.dictName]!!.dictList["MemberJoinInvite"]?.replace("\${member}", this.member.id.toString())?.replace("\${invitor}", this.invitor.id.toString())?.deserializeMiraiCode()
                        ?.let { it1 -> this.group.sendMessage(it1)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberLeaveEvent.Quit> {
            GroupList.forEach {
                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                    DictList[it.dictName]!!.dictList["MemberLeave"]?.replace("\${member}", this.member.id.toString())?.deserializeMiraiCode()
                        ?.let { it1 -> this.group.sendMessage(it1)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberLeaveEvent.Kick> {
            GroupList.forEach {
                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                    DictList[it.dictName]!!.dictList["MemberLeaveKick"]?.replace("\${member}", this.member.id.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it1 -> this.group.sendMessage(it1)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberMuteEvent> {
            GroupList.forEach {
                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                    DictList[it.dictName]!!.dictList["MemberMute"]?.replace("\${member}", this.member.id.toString())?.replace("\${durationSeconds}", this.durationSeconds.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it1 -> this.group.sendMessage(it1)}
                }
            }
        }

        globalEventChannel().subscribeAlways<MemberUnmuteEvent> {
            GroupList.forEach {
                if (it.groupNum == this.group.id && it.qq == this.bot.id){
                    DictList[it.dictName]!!.dictList["MemberUnmute"]?.replace("\${member}", this.member.id.toString())?.replace("\${operator}", this.operatorOrBot.id.toString())?.deserializeMiraiCode()
                        ?.let { it1 -> this.group.sendMessage(it1)}
                }
            }
        }
    }

    override fun onDisable() {
        logger.info { "Plugin disabled" }
        GroupList.forEach {
            if (it != testGroup){
                File(GroupDir.absolutePath+File.separatorChar+it.groupNum.toString()+"-"+it.qq.toString()+".json").writeText(Json.encodeToString(GroupInfo.serializer(),it))
            }
        }
        DictList.forEach {
            if (!it.key.equals("test")){
                File(DictDir.absolutePath+File.separatorChar+MD5Util.encodeToHex(it.key)+".json").writeText(Json.encodeToString(DictInfo.serializer(),it.value))
            }
        }
    }
}