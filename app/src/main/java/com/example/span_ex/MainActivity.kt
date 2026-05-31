package com.example.span_ex

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chat.glidespan.GlideSpanGifLoader
import com.chat.glidespan.GlideSpanImageLoader
import com.chat.spanutil.SpanBuilder
import com.chat.spanutil.SpanLog
import com.chat.spanutil.span.CharAnim
import com.chat.spanutil.span.CharAnims
import com.chat.spanutil.span.EmojiRegistry
import com.chat.spanutil.span.LoaderType
import com.chat.spanutil.span.RepeatConfig
import com.chat.spanutil.span.RoundMaskDrawable
import com.chat.svgaspan.SvgaCache
import com.chat.svgaspan.SvgaSpanLoader
import com.chat.svgspan.DefaultSvgLoader

class MainActivity : AppCompatActivity() {

    private lateinit var content: LinearLayout

    private val imageUrl = "https://placehold.co/96x96/png"
    private val gifUrl =
        "https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExM3lwNWtqMGdocWpvdWp1Ymtrc2hmeTZnbTI2MG5naHZ6ZnZwbWY3bCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/DzFj5QLRs7AZ2/giphy.gif"
    private val svgUrl = "https://dev.w3.org/SVG/tools/svgweb/samples/svg-files/android.svg"
    private val svgaUrl = "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/angel.svga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSpanApis()
        setContentView(createContentView())
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        renderExamples()
    }

    private fun installSpanApis() {
        SpanLog.enabled = true
        SpanLog.d("富文本演示") { "调试日志示例" }
        SpanLog.i("富文本演示") { "普通日志示例" }
        SpanLog.w("富文本演示") { "警告日志示例" }
        SpanLog.e("富文本演示") { "错误日志示例" }

        GlideSpanImageLoader.install()
        GlideSpanGifLoader.install()
        DefaultSvgLoader.install()
        SvgaSpanLoader.install()

        SpanBuilder.setLoader(LoaderType.Image, GlideSpanImageLoader())
        SpanBuilder.setLoader(LoaderType.Gif, GlideSpanGifLoader())
        SpanBuilder.setLoader(LoaderType.Svg, DefaultSvgLoader())
        SpanBuilder.setLoader(LoaderType.Svga, SvgaSpanLoader())

        SpanBuilder.setTextSpanFactory { _, _, _ ->
            RelativeSizeSpan(1.12f)
        }
        SpanBuilder.setImageTransformer { drawable, _, _ ->
            RoundMaskDrawable(drawable, dp(10).toFloat())
        }

        EmojiRegistry.clear()
        EmojiRegistry.register(":本地:", R.drawable.ic_launcher_foreground)
        EmojiRegistry.register("[远程]", imageUrl)
        EmojiRegistry.registerAll(
            mapOf(
                ":批量:" to R.drawable.ic_launcher_foreground,
                "[批量远程]" to imageUrl,
            )
        )
        EmojiRegistry.unregister("[未使用]")
        EmojiRegistry.resolve(":本地:")

        SvgaCache.trimMemoryHalf()
        SvgaCache.trimMemory()
    }

    private fun createContentView(): View {
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(24))
        }
        return ScrollView(this).apply {
            id = R.id.main
            setBackgroundColor(Color.rgb(248, 250, 252))
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            )
        }
    }

    private fun renderExamples() {
        addHeader()
        addTextStyleExample()
        addFindRangeExample()
        addDecorationExample()
        addCustomLengthAndAnimationExample()
        addImageExample()
        addRemoteLoaderExample()
        addReplaceExample()
        addBuildModeExamples()
    }

    private fun addHeader() {
        content.addView(TextView(this).apply {
            text = "富文本模块方法演示"
            setTextColor(Color.rgb(15, 23, 42))
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        })
        content.addView(TextView(this).apply {
            text = "文本、图片、矢量图、动图与动效组合示例"
            setTextColor(Color.rgb(71, 85, 105))
            textSize = 14f
            setPadding(0, dp(4), 0, dp(10))
        })
    }

    private fun addTextStyleExample() = addSpanExample("文本拼接与基础样式") { tv ->
        SpanBuilder.with(this)
            .append("蓝色追加 ").color(Color.rgb(37, 99, 235))
            .append("追加一行").bold().appendLine()
            .append("粗体 ").bold()
            .append("斜体 ").italic()
            .append("粗斜体 ").boldItalic()
            .append("下划线 ").underline()
            .append("删除线 ").strikethrough()
            .append("大字号").sizePx(dp(22))
            .append(" 背景色").backgroundColor(Color.rgb(254, 240, 138))
            .into(tv)
    }

    private fun addFindRangeExample() = addSpanExample("整段文本、查找与范围定位") { tv ->
        SpanBuilder.with(this)
            .setText("查找第一处；全部 全部；正则 甲12 乙34；范围演示；整段文本")
            .find("第一处").color(Color.rgb(220, 38, 38)).bold()
            .findAll("全部").backgroundColor(Color.rgb(187, 247, 208))
            .findRegex(Regex("[\\u4e00-\\u9fa5]\\d+")).color(Color.rgb(124, 58, 237)).underline()
            .range(0, 4).backgroundColor(Color.rgb(219, 234, 254))
            .all().customTextSpan()
            .into(tv)
    }

    private fun addDecorationExample() = addSpanExample("渐变、描边、发光与阴影") { tv ->
        SpanBuilder.with(this)
            .append("双色渐变  ")
            .gradientColor(Color.rgb(14, 165, 233), Color.rgb(236, 72, 153))
            .append("多色渐变  ")
            .gradientColor(
                intArrayOf(Color.rgb(22, 163, 74), Color.rgb(234, 179, 8), Color.rgb(239, 68, 68)),
                floatArrayOf(0f, 0.45f, 1f),
            )
            .append("纵向渐变  ")
            .gradientColor(Color.rgb(79, 70, 229), Color.rgb(20, 184, 166), vertical = true)
            .append("描边  ").stroke(Color.WHITE, dp(2).toFloat())
            .append("发光  ").glow(Color.rgb(14, 165, 233), dp(5).toFloat())
            .append("阴影").shadow(Color.argb(180, 15, 23, 42), dp(3).toFloat(), dp(2).toFloat(), dp(2).toFloat())
            .into(tv)
    }

    private fun addCustomLengthAndAnimationExample() = addSpanExample(
        "截断、自定义效果、字符动画与边距"
    ) { tv ->
        val wave = CharAnim { paint, progress, index, _ ->
            CharAnims.Rise.apply(paint, progress, index, 1)
            CharAnims.Bounce.apply(paint, progress, index, 1)
            CharAnims.Slide.apply(paint, progress, index, 1)
            paint.color = Color.rgb(217, 119, 6)
        }

        SpanBuilder.with(this)
            .append("末尾截断：一二三四五六七八九十十一十二").maxLength(10)
            .appendLine()
            .append("中间截断：一二三四五六七八九十十一十二").maxLengthMiddle(10)
            .appendLine()
            .append("全局自定义文字效果 ").customTextSpan()
            .append("局部自定义文字效果 ")
            .customTextSpan { _, _, _ -> ForegroundColorSpan(Color.rgb(190, 18, 60)) }
            .append("文字边距")
            .backgroundColor(Color.rgb(224, 242, 254))
            .marginPx(left = dp(10), top = dp(3), right = dp(10))
            .appendLine()
            .append("上浮、弹跳、倾斜循环")
            .charAnimation(wave, 55L, 420L, RepeatConfig.infiniteReverse(pauseMs = 450L))
            .into(tv)
    }

    private fun addImageExample() = addSpanExample("本地图片、边框与图片变换") { tv ->
        val icon = dp(34)
        SpanBuilder.with(this)
            .append("资源图片 ")
            .image(R.drawable.ic_launcher_foreground, icon, icon)
            .imageBorder(Color.rgb(14, 165, 233), dp(2).toFloat(), dp(8).toFloat())
            .marginPx(left = dp(4), right = dp(8))
            .append("可绘制对象 ")
            .image(launcherDrawable(), icon, icon)
            .imageBorderGradient(
                Color.rgb(244, 63, 94),
                Color.rgb(251, 191, 36),
                dp(2).toFloat(),
                dp(9).toFloat(),
            )
            .append("位图 ")
            .image(demoBitmap(icon, icon), icon, icon)
            .imageBorderGradient(
                intArrayOf(Color.rgb(34, 197, 94), Color.rgb(59, 130, 246), Color.rgb(168, 85, 247)),
                dp(2).toFloat(),
                dp(12).toFloat(),
                vertical = true,
            )
            .appendLine()
            .append("全局图片变换 ")
            .image(launcherDrawable(), icon, icon).customImageTransform()
            .append(" 局部图片变换 ")
            .image(launcherDrawable(), icon, icon)
            .customImageTransform { drawable, _, _ -> RoundMaskDrawable(drawable, dp(17).toFloat()) }
            .textVerticalMarginPx(top = dp(2))
            .into(tv)
    }

    private fun addRemoteLoaderExample() = addSpanExample(
        "网络图片、动图、矢量图与动效加载器"
    ) { tv ->
        val icon = dp(38)
        SpanBuilder.with(this)
            .append("网络图片 ")
            .image(imageUrl, icon, icon, circle = true)
            .imageBorder(Color.rgb(15, 118, 110), dp(2).toFloat(), dp(19).toFloat())
            .append("网络动图 ")
            .gif(gifUrl, icon, icon, circle = true)
            .append("本地矢量图 ")
            .svg(R.raw.span_demo, icon, icon)
            .append("网络矢量图 ")
            .svg(svgUrl, icon, icon)
            .append("网络动效 ")
            .svga(svgaUrl, dp(70), dp(42))
            .into(tv)
    }

    @Suppress("unused")
    private fun localGifResourceOverloadExample() =
        SpanBuilder.with(this)
            .append("本地动图资源重载示例：需要放入真正的动图资源 ")
            .gif(R.mipmap.ic_launcher, dp(28), dp(28), circle = true)
            .build()

    private fun addReplaceExample() = addSpanExample("占位符替换图片与表情替换") { tv ->
        val icon = dp(30)
        SpanBuilder.with(this)
            .setText("资源=# 可绘制对象=@ 网络=& 表情 :本地: [远程] :批量: [批量远程]")
            .replaceWithImage("#", R.drawable.ic_launcher_foreground, icon, icon)
            .replaceWithImage("@", launcherDrawable(), icon, icon)
            .replaceWithImage("&", imageUrl, icon, icon, circle = true)
            .replaceEmoji(icon, icon)
            .into(tv)
    }

    private fun addBuildModeExamples() {
        addTitle("构建、挂载与直接设置")

        val buildTv = demoTextView()
        content.addView(buildTv)
        buildTv.text = SpanBuilder.with(this)
            .append("只构建文本时，需要手动设置点击处理")
            .color(Color.rgb(37, 99, 235))
            .onClick(underline = true, overrideColor = Color.rgb(37, 99, 235)) {
                toast("点击了只构建文本示例")
            }
            .build()
        buildTv.movementMethod = LinkMovementMethod.getInstance()

        val attachTv = demoTextView()
        content.addView(attachTv)
        val attachBuilder = SpanBuilder.with(this)
            .append("构建并挂载 ")
            .bold()
            .append("远程图片 ")
            .image(imageUrl, dp(28), dp(28), circle = true)
            .append("动画文字")
            .charAnimation(
                CharAnims.Fade,
                perCharDelayMs = 35L,
                charDurationMs = 320L,
                repeat = RepeatConfig.infiniteRestart(pauseMs = 600L),
            )
        attachTv.text = attachBuilder.buildAndAttach(attachTv) { updated ->
            attachTv.text = updated
        }
        attachBuilder.getCharAnimDriver()

        val intoTv = demoTextView()
        content.addView(intoTv)
        SpanBuilder.with(this)
            .append("直接设置会自动处理点击、软件图层和图片回调")
            .shadow(Color.argb(150, 0, 0, 0), dp(2).toFloat(), dp(1).toFloat(), dp(1).toFloat())
            .onClick(overrideColor = Color.rgb(22, 163, 74)) {
                toast("点击了直接设置示例")
            }
            .into(intoTv)
    }

    private fun addSpanExample(title: String, bind: (TextView) -> Unit) {
        addTitle(title)
        demoTextView().also {
            content.addView(it)
            bind(it)
        }
    }

    private fun addTitle(title: String) {
        content.addView(TextView(this).apply {
            text = title
            setTextColor(Color.rgb(15, 23, 42))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(12), 0, dp(5))
        })
    }

    private fun demoTextView(): TextView =
        TextView(this).apply {
            setTextColor(Color.rgb(30, 41, 59))
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
            includeFontPadding = true
            setLineSpacing(dp(2).toFloat(), 1.0f)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = dp(2)
            }
        }

    private fun launcherDrawable(): Drawable =
        (ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground)
            ?: ColorDrawable(Color.rgb(203, 213, 225))).mutate()

    private fun demoBitmap(width: Int, height: Int) =
        createBitmap(width, height).also { bitmap ->
            val canvas = Canvas(bitmap)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb(20, 184, 166)
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), dp(8).toFloat(), dp(8).toFloat(), paint)
            paint.color = Color.WHITE
            canvas.drawCircle(width * 0.62f, height * 0.38f, width * 0.18f, paint)
        }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()
}
