# Auto-Reply

## 介绍

基于[Mirai](https://github.com/mamoe/mirai)的自动回复插件

## 指令

- 添加词条`/dictadd 触发语 回复语`
- 交互式添加`/dictadd`
- 删除词条`/dictdel 触发语`
- 创建词库`/dictcreate 词库名`
- 使用词库`/dictuse 词库名`
- 取消词库`/dictcancel`
- 删除词库`/dictsdel 词库名`
- 切换权限`/dictman`
- 添加权限`/dictmanadd QQ`
- 删除权限`/dictmandel QQ`
- 查看帮助`/dicthelp`

示例指令中使用空格作为分隔符，可替换为其他字符，需要保证分隔符出现的次数与示例中相同，例如`/dictadd#触发语#回复语`

## 功能

### 自动回复

添加词条后直接发送触发语即可

### 词库

每个群默认使用单独的词库，与其他群不互通，如需互通可创建并使用词库

### 权限

群内默认不开启权限控制，即所有人均可添加词条，如需限制可切换权限，并添加对应权限

### 关键字

事件关键字

- bot被戳`NudgeSelf`
- 非bot被戳`Nudge`
- 成员主动加入群`MemberJoin`
- 成员被邀请加入群`MemberJoinInvite`
- 成员主动退出群`MemberLeave`
- 成员被踢出群`MemberLeaveKick`
- 成员被禁言`MemberMute`
- 成员被解除禁言`MemberUnmute`

回复关键字

- 成员QQ号`${member}`
- 禁言时长`${durationSeconds}`
- 操作者QQ号`${operator}`
- 邀请者QQ号`${invitor}`
- 戳一戳动作`${action}`
- 戳一戳后缀`${suffix}`
- 戳一戳来源`${from}`
- 戳一戳目标`${target}`
- 群号`${group}`
- 发送者QQ号`${sender}`

### 回复触发功能

回复触发格式为`${mirai:action:param}`

- At某人`${mirai:at:QQ}`
- At全体`${mirai:at:all}`
- 戳一戳`${mirai:nudge:QQ}`
- 禁言某人`${mirai:mute:QQ:second}`
- 解除禁言`${mirai:mute:QQ}`
- 解除禁言`${mirai:unmute:QQ}`
- 图片消息`${mirai:image:filename}`
- 网络图片`${mirai:image:EncodedURL}`
- 语音消息`${mirai:audio:filename}`
- 文本消息`${mirai:plain:text}`
- 网络文本`${mirai:plain:EncodedURL}`
- 网络JSON`${mirai:plain:EncodedURL:key}`

图片和语音文件需在对应文件夹下