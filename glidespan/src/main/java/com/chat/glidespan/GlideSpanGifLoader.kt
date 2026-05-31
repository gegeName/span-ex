package com.chat.glidespan

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chat.spanutil.SpanBuilder
import com.chat.spanutil.span.LoaderType
import com.chat.spanutil.span.RoundMaskDrawable
import com.chat.spanutil.span.SpanImageLoader

/**
 * 基于 Glide 的 GIF / WebP 动图加载器，与 [GlideSpanImageLoader] 分离避免相互干扰。
 * Application 中通过 [install] 一键注入：
 * GlideSpanGifLoader.install()
 */
class GlideSpanGifLoader : SpanImageLoader {

    override fun load(
        context: Context,
        url: Any,
        width: Int,
        height: Int,
        circle: Boolean,
        onReady: (Drawable) -> Unit,
    ) {
        val options = RequestOptions()
            .override(width, height)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
        Glide.with(context).asGif().load(url).apply(options)
            .into(object : CustomTarget<GifDrawable>() {
                override fun onResourceReady(
                    resource: GifDrawable,
                    transition: Transition<in GifDrawable>?,
                ) {
                    configureGif(resource)
                    val finalDrawable: Drawable =
                        if (circle) RoundMaskDrawable(resource, cornerRadius = -1f)
                        else resource
                    onReady(finalDrawable)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun configureGif(gif: GifDrawable) {
        gif.setLoopCount(GifDrawable.LOOP_FOREVER)
        gif.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable) {
                if (drawable is GifDrawable && !drawable.isRunning) drawable.start()
            }
        })
        if (gif.isRunning) gif.stop()
        gif.start()
    }

    companion object {
        /**
         * 把当前实现注入到 [SpanBuilder] 全局 GIF 加载器，等价于：
         * `SpanBuilder.setLoader(LoaderType.Gif, GlideSpanGifLoader())`。
         */
        @JvmStatic
        fun install() {
            SpanBuilder.setLoader(LoaderType.Gif, GlideSpanGifLoader())
        }
    }
}
