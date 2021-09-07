package co.zsmb.chatkitstream

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.zsmb.chatkitstream.databinding.ActivityMainBinding
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.QueryChannelsController

class MainActivity : AppCompatActivity() {

    private val adapter = DialogsListAdapter<Dialog>(CoilImageLoader)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dialogsList.setAdapter(adapter)

        adapter.setOnDialogClickListener { dialog ->
            startActivity(MessagesActivity.newIntent(this, dialog.channel))
        }

        val client = ChatClient.Builder("b67pax5b2wdq", applicationContext)
            .logLevel(ChatLogLevel.ALL)
            .build()
        ChatDomain.Builder(client, applicationContext).build()

        val user = User(
            id = "tutorial-droid",
            extraData = mutableMapOf(
                "name" to "Tutorial Droid",
                "image" to "https://bit.ly/2TIt8NR",
            ),
        )
        client.connectUser(
            user = user,
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZHJvaWQifQ.NhEr0hP9W9nwqV7ZkdShxvi02C5PR7SJE7Cs4y7kyqg"
        ).enqueue {
            // TODO Do error handling here in a real app
            loadChannels(user)
        }
    }

    private fun loadChannels(user: User) {
        // Query Stream Chat API for the list of channels (conversations, dialogues)
        val filter: FilterObject = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", user.id),
        )
        val sort: QuerySort<Channel> = QuerySort.desc("last_updated")
        ChatDomain.instance()
            .queryChannels(filter, sort)
            .enqueue { result ->
                val controller: QueryChannelsController = result.data()
                controller.channels.observe(this) { channels ->
                    adapter.setItems(channels.map { Dialog(it) })
                }
            }
    }
}
