# Android中的安全距离适配

在Android屏幕的空间中，大部分的区域我们都是可以随意绘制，只有一部分区域是显示的固定内容：

* **状态栏**
* 标题栏(ActionBar)
* 页面内容(Content)
* **导航栏**

其中**标题栏**是可选的，除了Material风格的应用应用的并不多，**页面内容**就是`android.R.id.content`是Activity的主要内容。

而我们主要需要讨论的就是 **状态栏和导航栏**，因为这两个区域在不同设备类型，不同的Android版本和不同的厂商下大小和效果是不同的，等等。这些差异无疑增加了我们做页面适配的复杂程度，页更容易出现兼容问题。

在2017年下半年iPhone X的发布，引入了刘海屏设备，导致了蓝绿大厂争相效仿，同时又自成一派，颇有一番百家争鸣之象。
这也导致了一个新的问题 **刘海区域适配** ，那时候Android才8.1，并没有API来支持这屏幕上这多出来的一块区域，不过好在大部分设备在定制时**刘海和状态栏高度是一致的**。

终于在2018年发布的Android 9中Google正式支持了刘海屏，定制了规范约束了设备厂商，减轻了刘海屏适配的差异问题，但是根源问题并没有解决。因为刘海区域的存在，可能会出现页面内容被遮挡，比如：启用页广告跳过按钮被遮挡的问题，导致被应用商店拒掉的风险。

不过好在Android 9中要求刘海设备必须有以下行为：

* 一条边缘最多只能包含一个刘海。
* 一台设备不能有两个以上的刘海。
* 设备的两条较长边缘上不能有刘海。
* **在未设置特殊标志的竖屏模式下，状态栏的高度必须与刘海的高度持平。**
* 默认情况下，在全屏模式或横屏模式下，整个刘海区域必须显示黑边。

**刘海高度默认是和状态栏高度一致**依旧没有变，所以问题又回到了状态栏区域的处理。

## 正文

所以肯定有同学说了：直接获取状态栏高度不就可以了适配刘海屏了。像这样：

```kotlin
val top = context.getStatusBarHeight()
titleBar.setPadding(0, top, 0, 0)
```

这么说也没有错，大部分情况下是没有问题的。但是既然官方已经适配刘海屏了，也为我们提供了新的API为什么不用呢：

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    window.decorView.post {
        val top = window.decorView.rootWindowInsets?.displayCutout?.safeInsetTop ?: 0
        // val bottom = window.decorView.rootWindowInsets?.displayCutout?.safeInsetBottom ?: 0
        titleBar.setPadding(0, top, 0, 0)
    }
}
```

上面的方案实际上可以获取上下左右四个方向的安全距离，但大部分情况我们只需要处理顶部就可以了。实际上这已经可以解决我们的问题了，但是还有更好的解决方案方案：

1. 添加依赖
```gradle
implementation 'androidx.core:core:1.7.0'

// 老版本也可以，但是getInsets() API 还没添加
// implementation 'androidx.core:core:1.3.0'
```
2. 使用ViewCompat工具
```kotlin
ViewCompat.setOnApplyWindowInsetsListener(titleBar) { view: View, insets: WindowInsetsCompat ->
    //val top = insets.systemWindowInsetTop // 高版本已经过时，可以用下面的api替换
    val stableInsets = insets.getInsets(
    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
    titleBar.setPadding(0, stableInsets.top, 0, 0)
    return@setOnApplyWindowInsetsListener insets
}
```

实际上屏幕安全距离，基本上全部围绕这一个API，Google也推荐我们这么做，在很多系统控件都能看到它的影子，比如：`AppBarLayout、DrawerLayout、NavigationBarView`等等都有用到，内部都是来处理系统安全距离的。

### 系统栏适配

上面提到了手机有各种系统栏（状态栏、导航栏），如果一个**全屏+刘海屏+透明系统栏+屏幕旋转**的页面处理这些安全距离就更复杂，比如短视频页，这里先给大家列几条可能出现的问题：

* 没有导航栏或者可以动态隐藏导航栏的设备
* 导航栏不会旋转的设备（就是导航栏一直在屏幕的一个边，不会跟随屏幕旋转）
* 导航栏跟随屏幕旋转的设备（主要是手势导航的设备和一些平板上）
* 刘海在屏幕底部的设备（开发者选项可以开启双刘海模式，设备两个短边都有刘海）
* 底部刘海+导航栏一起显示的设备
* ... ...

这些所有的问题通过 `ViewCompat.setOnApplyWindowInsetsListener()` 来优雅处理，
通过 `WindowInsetsCompat.getInsets(type)` 可以获取系统的各个栏的大小，
我们也可以同时获取多个系统栏的高度，各个距离内部会进行累加，返回一个类似Rect的对象，对应屏幕的左上右下需要插入的距离：
```kotlin
val stableInsets = insets.getInsets(
    WindowInsetsCompat.Type.statusBars() or
    WindowInsetsCompat.Type.navigationBars() or
    WindowInsetsCompat.Type.displayCutout())
```
然后在对不同位置的控件添加对应的边距。除了上面提到的三种类型的安全距离，还有一些其他的类型，有兴趣的可以自己了解。

### 其他适配

`ViewCompat.setOnApplyWindowInsetsListener()`能解决大部分安全距离的问题，但是有一点它是处理不了的，就是 **屏幕圆角**，这些安全距离的计算是不处理屏幕圆角的，所以如果有圆角要处理那我们就要另辟蹊径了。

好在Android 12中官方添加了对圆角的支持：

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val roundedCorner = insets.toWindowInsets()
        ?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
    roundedCorner?.center
}
```

除了圆角支持，还有对**隐私指示器**提供了支持：
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val rect = insets.toWindowInsets()?.privacyIndicatorBounds
    // 页面控件需要避开这个区域，不然可能会被遮挡
}
```
隐私指示器的范围，主要是 **摄像头和麦克风** 使用中状态的指示器边界，如果是录制直播或者相机的页面需要处理这个区域。

除了圆角以外，好像没有找到官方对**打孔屏**的支持，可能后面会加入对打孔屏的支持吧。

## 相关链接

[Demo](https://github.com/hushenghao/SafeSpace)

[官方文档：支持刘海屏](https://developer.android.google.cn/guide/topics/display-cutout)

[官方文档：圆角](https://developer.android.google.cn/guide/topics/ui/look-and-feel/rounded-corners)