package co.zsmb.chatkitstream

import com.stfalcon.chatkit.commons.models.IDialog
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import java.util.Date

class Dialog(
    val channel: io.getstream.chat.android.client.models.Channel
) : IDialog<Message> {
    override fun getId(): String = channel.id
    override fun getDialogPhoto(): String = channel.image
    override fun getDialogName(): String = channel.name
    override fun getUsers(): List<IUser> = channel.members.map { User(it.user) }
    override fun getLastMessage(): Message? = channel.messages.lastOrNull()?.let { Message(it) }
    override fun setLastMessage(message: Message?) { throw UnsupportedOperationException() }
    override fun getUnreadCount(): Int = channel.unreadCount ?: 0
}

class Message(
    private val message: io.getstream.chat.android.client.models.Message
) : IMessage {
    override fun getId(): String = message.id
    override fun getText(): String = message.text
    override fun getUser(): IUser = User(message.user)
    override fun getCreatedAt(): Date? = message.createdAt ?: message.createdLocallyAt
}

class User(
    private val user: io.getstream.chat.android.client.models.User
) : IUser {
    override fun getId(): String = user.id
    override fun getName(): String = user.name
    override fun getAvatar(): String = user.image
}
