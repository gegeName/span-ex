# span-ex

`span-ex` 是一个 Android TextView 富文本 Span 工具库，支持链式拼接文字、图片、GIF、SVG、SVGA、点击事件、渐变、描边、发光、阴影、字符动画、占位符替换和表情替换。

## 安装

在根工程 `settings.gradle.kts` 或 `build.gradle` 中加入 JitPack 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

按需引入模块，版本号统一使用 `0.0.5`。只需要核心能力时只引入 `spanutil`；需要对应功能时再额外引入扩展模块：

```kotlin
dependencies {
    // 核心富文本构建器
    implementation("com.github.gegeName.span-ex:spanutil:0.0.5")

    // Glide 图片 / GIF / WebP 加载扩展
    implementation("com.github.gegeName.span-ex:glidespan:0.0.5")

    // SVG 加载扩展
    implementation("com.github.gegeName.span-ex:svgspan:0.0.5")

    // SVGA 加载扩展
    implementation("com.github.gegeName.span-ex:svgaspan:0.0.5")
}
```

不建议使用 `com.github.gegeName:span-ex:0.0.4` 作为常规接入方式。JitPack 多模块仓库的根坐标可能作为聚合依赖，把所有子模块一起引入；如果想保持功能可选，请使用上面的模块坐标。

最低支持 `minSdk 21`。

## 初始化

在 `Application.onCreate()` 中按需注册图片加载器：

```kotlin
import com.chat.glidespan.GlideSpanGifLoader
import com.chat.glidespan.GlideSpanImageLoader
import com.chat.svgaspan.SvgaSpanLoader
import com.chat.svgspan.DefaultSvgLoader

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        GlideSpanImageLoader.install()
        GlideSpanGifLoader.install()
        DefaultSvgLoader.install()
        SvgaSpanLoader.install()
    }
}
```

也可以手动注册自定义加载器：

```kotlin
SpanBuilder.setLoader(LoaderType.Image, myImageLoader)
```

## 基础用法

推荐使用 `into(textView)`，它会自动处理点击事件、异步图片刷新、动画生命周期和部分需要的软件图层配置。

```kotlin
SpanBuilder.with(this)
    .append("Hello ").color(Color.BLUE).bold()
    .append("span-ex").gradientColor(Color.RED, Color.MAGENTA)
    .append(" ")
    .image(R.drawable.ic_launcher_foreground, dp(24), dp(24))
    .into(textView)
```

如果只需要得到 `CharSequence`：

```kotlin
val text = SpanBuilder.with(context)
    .append("只构建文本")
    .underline()
    .build()

textView.text = text
```

如果使用了 `onClick()`，直接 `build()` 后需要自己设置：

```kotlin
textView.movementMethod = LinkMovementMethod.getInstance()
```

## 网络图片、GIF、SVG、SVGA

```kotlin
SpanBuilder.with(this)
    .append("头像 ")
    .image("https://example.com/avatar.png", dp(32), dp(32), circle = true)
    .append(" GIF ")
    .gif("https://example.com/demo.gif", dp(32), dp(32))
    .append(" SVG ")
    .svg(R.raw.icon_svg, dp(32), dp(32))
    .append(" SVGA ")
    .svga("https://example.com/gift.svga", dp(80), dp(48))
    .into(textView)
```

`image(url)`、`gif()`、`svg()`、`svga()` 都需要对应 loader 已注册，并建议使用 `into(textView)` 或 `buildAndAttach(textView)` 触发异步加载。

## 查找并设置样式

可以先设置完整文案，再通过 `find`、`findAll`、`findRegex` 或 `range` 定位片段：

```kotlin
SpanBuilder.with(this)
    .setText("价格 99 元，会员价 79 元")
    .find("99").strikethrough().color(Color.GRAY)
    .find("79").color(Color.RED).bold()
    .findRegex(Regex("\\d+")).backgroundColor(0x22FF0000)
    .into(textView)
```

## 占位符和表情替换

```kotlin
EmojiRegistry.register(":smile:", R.drawable.ic_smile)
EmojiRegistry.register("[vip]", "https://example.com/vip.png")

SpanBuilder.with(this)
    .setText("用户 :smile: [vip]")
    .replaceEmoji(dp(24), dp(24))
    .into(textView)
```

也可以替换自定义占位符：

```kotlin
SpanBuilder.with(this)
    .setText("等级 # 用户")
    .replaceWithImage("#", R.drawable.ic_level, dp(20), dp(20))
    .into(textView)
```

## 常用 API

| API | 说明 |
| --- | --- |
| `append(text)` / `appendLine(text)` | 追加文本 |
| `setText(text)` | 设置整段文本 |
| `find()` / `findAll()` / `findRegex()` / `range()` / `all()` | 定位需要设置样式的片段 |
| `color()` / `backgroundColor()` / `sizePx()` | 基础文字样式 |
| `bold()` / `italic()` / `boldItalic()` | 字体样式 |
| `underline()` / `strikethrough()` | 下划线和删除线 |
| `gradientColor()` / `stroke()` / `glow()` / `shadow()` | 渐变、描边、发光、阴影 |
| `image()` | 本地图片、Drawable、Bitmap、网络图片 |
| `gif()` | 本地或网络 GIF / WebP |
| `svg()` | 本地 raw SVG、文件路径或网络 SVG |
| `svga()` | assets 文件名或网络 SVGA |
| `imageBorder()` / `imageBorderGradient()` | 图片边框 |
| `marginPx()` / `textVerticalMarginPx()` | 片段边距和文字垂直微调 |
| `replaceWithImage()` | 将占位符替换为图片 |
| `replaceEmoji()` | 根据 `EmojiRegistry` 替换表情 token |
| `onClick()` | 添加点击事件 |
| `charAnimation()` | 字符级动画 |
| `customTextSpan()` / `customImageTransform()` | 自定义文字 Span 或图片变换 |
| `build()` | 返回 `CharSequence` |
| `buildAndAttach(textView)` | 构建并挂载运行时能力，但不主动设置 `textView.text` |
| `into(textView)` | 构建并直接设置到 TextView |

## 混淆

各模块已带有 `consumer-rules.pro`，正常情况下业务工程不需要额外配置。

## 许可证

本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源。Apache 2.0 自带"按现状提供、不作担保、不承担责任"的条款，并额外包含专利授权与责任限制条款。

## 免责声明 / Disclaimer

本项目（以下简称"本软件"）是一个通用的富文本 Span 工具库，仅供学习、研究和合法用途使用。

1. 本软件按"现状"提供，作者不对其适用性、可靠性、安全性作任何明示或暗示的担保。
2. 使用者应自行遵守所在国家/地区的法律法规。对于使用者利用本软件从事的任何违法、侵权或其他不当行为，作者不承担由此产生的任何责任。
3. 本软件不针对任何违法用途设计，作者不认可、不支持将其用于任何违反法律法规的用途。
4. 在适用法律允许的最大范围内，作者不对因使用或无法使用本软件而导致的任何直接或间接损失承担责任。
5. 使用本软件即表示使用者已知悉并接受以上条款。
