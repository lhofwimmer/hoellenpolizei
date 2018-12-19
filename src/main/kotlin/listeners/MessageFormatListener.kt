package listeners

import managers.ConfigManager
import org.javacord.api.event.message.MessageCreateEvent

class MessageFormatListener : BaseListener() {
    override fun onMessageCreate(event: MessageCreateEvent?) {
        if(super.checkIfAuthorIsBot(event!!)) return
        if (ConfigManager.instance.markupChannels.contains(event?.message?.channel?.id)) {
            if (!event?.messageAttachments?.isNullOrEmpty()!!) return
            if (event.messageContent.length < 2000) {
                var lang = event.messageContent.takeWhile { it != ' ' }
                var content: String
                if (ConfigManager.instance.allowedLanguages.containsKey(lang)) {
                    lang = ConfigManager.instance.allowedLanguages[lang]!!
                    content = event.messageContent.substring(lang.length+1)
                }else {
                    lang = "ts"
                    content = event.messageContent
                }
                val message = """
                    |```$lang
                    |$content
                    |```
                """.trimMargin("|")
                event.channel.sendMessage(message)
                event.message.delete()
            }
        }
    }
}