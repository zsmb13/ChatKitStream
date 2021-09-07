package co.zsmb.chatkitstream

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.zsmb.chatkitstream.databinding.ActivityMessagesBinding
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomain

class MessagesActivity : AppCompatActivity(),
    MessagesListAdapter.OnLoadMoreListener,
    MessageInput.InputListener,
    MessageInput.TypingListener {
    private lateinit var messagesAdapter: MessagesListAdapter<Message>
    private lateinit var cid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up message list
        val currentUserId = ChatClient.instance().getCurrentUser()!!.id
        messagesAdapter = MessagesListAdapter<Message>(currentUserId, CoilImageLoader)
        messagesAdapter.setLoadMoreListener(this)
        binding.messagesList.setAdapter(messagesAdapter)

        // Set up message input
        binding.input.setInputListener(this)
        binding.input.setTypingListener(this)

        // Query Stream Chat API for messages in this channel
        cid = checkNotNull(intent.getStringExtra(CID_KEY)) {
            "Specifying a channel id is required when starting MessagesActivity"
        }
        ChatDomain.instance().watchChannel(cid, 30).enqueue { result ->
            // TODO Do error handling here in a real app
            val channelController = result.data()
            channelController.messages.observe(this) { messages ->
                messagesAdapter.clear(true)
                messagesAdapter.addToEnd(messages.map { Message(it) }, true)
            }
        }
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        ChatDomain.instance().loadOlderMessages(cid, 30).enqueue()
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        if (input.isNullOrEmpty()) return false

        ChatDomain.instance().sendMessage(
            io.getstream.chat.android.client.models.Message(
                cid = cid,
                text = input.toString(),
            )
        ).enqueue()
        return true
    }

    override fun onStartTyping() {
        ChatDomain.instance().keystroke(cid, null).enqueue()
    }

    override fun onStopTyping() {
        ChatDomain.instance().stopTyping(cid).enqueue()
    }

    companion object {
        private const val CID_KEY = "key:cid"

        fun newIntent(context: Context, channel: Channel): Intent =
            Intent(context, MessagesActivity::class.java).putExtra(CID_KEY, channel.cid)
    }
}
