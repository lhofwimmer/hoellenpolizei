package listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import managers.ConfigManager
import managers.containsFileType
import managers.getByFileType
import org.javacord.api.entity.message.MessageAttachment
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.event.message.MessageCreateEvent
import utils.EmbedUtil
import utils.FileUtils
import java.util.*

class FileListener : BaseListener() {

    override fun onMessageCreate(event: MessageCreateEvent?) {
        super.onMessageCreate(event)
        if (ConfigManager.instance.markupChannels.contains(event?.message?.channel?.id)) {
            event?.messageAttachments?.let { attachments ->
                attachments.forEach {
                    it?.let { attachment ->
                        printCodeBlock(attachment)
                    }
                }
            }
        }
    }

    fun printCodeBlock(attachment: MessageAttachment) {
        println("Processing <${attachment.fileName}>")
        if (!attachment.fileName.contains(".")) {
            attachment.message.channel.sendMessage(EmbedUtil.getErrorEmbed("Error: Unable to parse file! Reason: No File extension"))
            return
        }

        val fileExtension = FileUtils.getExtension(attachment.fileName)

        if (ConfigManager.instance.allowedLanguages.containsFileType(fileExtension)) {
            println("Processing file with filetype <$fileExtension>")
            val content = attachment.proxyUrl.readText()
            val message = """
                |```${ConfigManager.instance.allowedLanguages.getByFileType(fileExtension).highlightjs}
                |$content
                |```
            """.trimMargin("|")
            println("Start process send message")
            if (content.length < 2000) {
                println("Processing file smaller than 2000 characters")
                println("Sending embed header")
                val embed = EmbedUtil.getFileMetaInfo(attachment.message)
                attachment.message.channel.sendMessage(embed)
                    .thenAccept {
                        println("Sending file content")
                        attachment.message.channel.sendMessage(message)
                        attachment.message.delete()
                        println("Finished processing <${attachment.url.file}>")
                    }
            } else {
                attachment.message.channel.sendMessage(EmbedUtil.getErrorEmbed("Error: Unable to parse file! Reason: File is longer than 2000 characters"))
            }
        } else {
            attachment.message.channel.sendMessage(EmbedUtil.getErrorEmbed("Error: Unsupported file type!"))
        }
    }
}