package co.zsmb.chatkitstream

import android.widget.ImageView
import coil.load
import com.stfalcon.chatkit.commons.ImageLoader

object CoilImageLoader : ImageLoader {
    override fun loadImage(imageView: ImageView, url: String?, payload: Any?) {
        imageView.load(url) {
            allowHardware(false)
        }
    }
}
