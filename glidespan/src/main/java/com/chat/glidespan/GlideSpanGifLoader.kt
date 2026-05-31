package com.chat.glidespan

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chat.spanutil.SpanBuilder
import com.chat.spanutil.span.LoaderType
import com.chat.spanutil.span.Releasable
import com.chat.spanutil.span.RoundMaskDrawable
import com.chat.spanutil.span.SpanImageLoader

/**
 * 基于 Glide 的 GIF / WebP 动图加载器,与 [GlideSpanImageLoader] 分离避免互相干扰。
 * Application 中通过 [install] 一键注入:
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
        val requestManager = Glide.with(context)
        lateinit var target: CustomTarget<GifDrawable>
        target = object : CustomTarget<GifDrawable>() {
            private var deliveredDrawable: ForeverGifDrawable? = null

            override fun onResourceReady(
                resource: GifDrawable,
                transition: Transition<in GifDrawable>?,
            ) {
                val displayDrawable: Drawable =
                    if (circle) RoundMaskDrawable(resource, cornerRadius = -1f)
                    else resource
                val finalDrawable = ForeverGifDrawable(
                    gif = resource,
                    displayDrawable = displayDrawable,
                    requestManager = requestManager,
                    target = target,
                )
                deliveredDrawable = finalDrawable
                onReady(finalDrawable)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                deliveredDrawable?.onGlideCleared()
                deliveredDrawable = null
            }
        }
        requestManager.asGif().load(url).apply(options).into(target)
    }

    companion object {
        /**
         * 把当前实现注入到 [SpanBuilder] 全局 GIF 加载器,等价于:
         * `SpanBuilder.setLoader(LoaderType.Gif, GlideSpanGifLoader())`。
         */
        @JvmStatic
        fun install() {
            SpanBuilder.setLoader(LoaderType.Gif, GlideSpanGifLoader())
        }
    }
}

private class ForeverGifDrawable(
    private val gif: GifDrawable,
    private val displayDrawable: Drawable,
    private val requestManager: RequestManager,
    private val target: CustomTarget<GifDrawable>,
) : Drawable(), Animatable, Drawable.Callback, Releasable {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val lastBounds = Rect()
    private var running = false
    private var released = false

    private val restartCallback = object : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable) {
            mainHandler.post {
                if (!released && running) {
                    gif.start()
                    invalidateSelf()
                }
            }
        }
    }

    init {
        gif.setLoopCount(GifDrawable.LOOP_FOREVER)
        gif.registerAnimationCallback(restartCallback)
        displayDrawable.callback = this
    }

    override fun draw(canvas: Canvas) {
        if (lastBounds != bounds) {
            lastBounds.set(bounds)
            displayDrawable.bounds = lastBounds
        }
        displayDrawable.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
        displayDrawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        displayDrawable.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = displayDrawable.intrinsicWidth

    override fun getIntrinsicHeight(): Int = displayDrawable.intrinsicHeight

    override fun start() {
        if (released) return
        running = true
        gif.start()
    }

    override fun stop() {
        running = false
        gif.stop()
    }

    override fun isRunning(): Boolean = running

    override fun release() {
        if (released) return
        clearInternal()
        requestManager.clear(target)
    }

    fun onGlideCleared() {
        if (released) return
        clearInternal()
    }

    private fun clearInternal() {
        stop()
        released = true
        gif.unregisterAnimationCallback(restartCallback)
        (displayDrawable as? RoundMaskDrawable)?.release()
        displayDrawable.callback = null
    }

    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, time: Long) {
        callback?.scheduleDrawable(this, what, time)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        callback?.unscheduleDrawable(this, what)
    }
}
