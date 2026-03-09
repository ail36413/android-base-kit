# lib_util API 逐方法说明

> 仅面向 `kotlin + 传统 xml` 场景，工具类互不依赖，可按需使用。

## Core

### `UtilKit`
- `isInitialized()`：是否已完成库初始化。
- `config()`：获取当前全局配置快照。
- `init(context, config)`：初始化 MMKV / Timber / 全局上下文。

### `UtilConfig`
- `enableTimber`：是否启用 Timber。
- `debugLog`：是否启用 Debug 日志模式。
- `tagPrefix`：默认日志 Tag 前缀。
- `mmkvRootDir`：MMKV 自定义根目录（可空）。

## Storage

### `KvUtil`
- `putString/getString`：读写字符串。
- `putInt/getInt`：读写 Int。
- `putLong/getLong`：读写 Long。
- `putBoolean/getBoolean`：读写 Boolean。
- `putFloat/getFloat`：读写 Float。
- `putDouble/getDouble`：读写 Double。
- `putBytes/getBytes`：读写 ByteArray。
- `putStringSet/getStringSet`：读写字符串集合。
- `remove/removeKeys`：删除一个或多个 key。
- `clearAll`：清空命名空间。
- `containsKey`：判断 key 是否存在。
- `allKeys`：获取全部 key。
- `count`：获取条目数量。

### `FileUtil`
- `file`：定位应用私有目录文件对象。
- `mkdirs`：创建演示子目录。
- `writeText`：覆盖写文本。
- `appendText`：追加写文本。
- `readText`：读取文本。
- `exists`：文件是否存在。
- `delete`：删除文件。
- `size`：文件大小（字节）。
- `lastModified`：最后修改时间戳（毫秒）。
- `listFiles`：列出目录文件名。
- `clearDir`：清空目录内容。

### `FilePathUtil`
- `join`：拼接路径并规范分隔符。
- `fileName`：获取文件名（含扩展名）。
- `extension`：获取扩展名（不含点）。
- `baseName`：获取不带扩展名的文件名。
- `parent`：获取父路径。
- `normalizeSeparator`：统一斜杠分隔符。
- `isAbsolute`：是否绝对路径。

### `UriFileUtil`
- `fileName(uri|uriString)`：从 `content://` 或 `file://` 获取文件名。
- `mimeType(uri|uriString)`：获取 MIME 类型。
- `size(uri|uriString)`：获取大小（字节）。

### `CacheUtil`
- `put`：写入内存缓存（支持 ttl）。
- `get`：按泛型读取缓存。
- `remove`：移除缓存。
- `contains`：是否存在且未过期。
- `size`：当前缓存数量。
- `clear`：清空缓存（Demo 对应 `btn_cache_clear_demo`）。

## Log / Click / Thread

### `LogUtil`
- `d/i/w/e(message)`：默认 Tag 日志。
- `d/i/w/e(tag, message)`：自定义 Tag 日志。

### `CrashUtil`
- `isInstalled`：是否已安装全局崩溃处理器。
- `install(config, onCrash)`：安装全局崩溃捕获。
- `uninstall`：卸载并恢复之前处理器。
- `recordHandled`：手动记录一个已捕获异常（不杀进程）。
- `crashDir`：崩溃日志目录。
- `listCrashFiles`：按时间倒序列出崩溃日志文件。
- `readCrash`：读取指定崩溃日志内容。
- `clear`：清空崩溃日志目录。

### `ClickUtil`
- `isFastClick`：按 key + 时间窗判断是否快速重复点击。
- `setDebouncedClickListener`：设置防抖点击监听。
- `clear`：清理单个防抖记录。
- `clearAll`：清理全部防抖记录。

### `ThreadUtil`
- `isMainThread`：是否主线程。
- `runOnMain`：主线程执行。
- `runOnMainDelay`：主线程延迟执行。
- `cancelMainTask`：取消主线程延迟任务。
- `runOnIo`：IO 线程执行。
- `shutdown`：关闭线程池（可选清空主线程队列）。

### `RetryUtil`
- `Config`：重试策略（次数/初始间隔/退避系数/最大间隔/`shouldRetry` 条件）。
- `retry`：执行重试，最终失败抛异常；遇到 `InterruptedException` 会立即中断并透传。
- `retryOrNull`：执行重试，最终失败返回 null。

## Device / Intent / Network / Permission

### `AppInfoUtil`
- `packageName`：包名。
- `versionName`：版本名。
- `versionCode`：版本号。
- `isDebuggable`：是否 Debuggable。
- `isMainProcess`：是否主进程。

### `ClipboardUtil`
- `copyText`：复制到剪贴板。
- `getText`：读取首条剪贴板文本。
- `hasText`：是否有主剪贴板内容。

### `IntentUtil`
- `openBrowser`：打开浏览器链接。
- `shareText`：打开系统分享面板。
- `openAppSettings`：打开应用设置页。
- `canHandle`：系统是否可处理 Intent。
- `safeStart`：安全启动 Intent（自动加 `NEW_TASK`）。

### `NetworkUtil`
- `isConnected`：是否有可用网络。
- `isWifi`：是否 Wi-Fi。
- `isCellular`：是否蜂窝网络。
- `isVpn`：是否 VPN。
- `isMetered`：是否计费网络。
- `networkType`：当前网络类型枚举。

### `NetStateListenerUtil`
- 前置条件：需要 `android.permission.ACCESS_NETWORK_STATE`（库清单已声明）。
- `register(listener)`：注册网络变化监听，返回 token；失败返回空串。
- `unregister(token)`：按 token 取消单个监听，空 token 忽略。
- `unregisterAll`：取消当前进程内全部已注册监听。

### `PermissionUtil`
- `isGranted`：单权限是否已授权。
- `areGranted`：多权限是否全部授权。
- `deniedPermissions`：筛出未授权权限列表。
- `shouldShowRationale`：是否应展示权限说明。
- `request`：发起运行时权限请求（requestCode 方式）。
- `allGranted`：权限回调结果是否全部通过。
- `openAppSettings`：跳转应用设置页。

## UI

### `ToastUtil`
- `showShort/showLong`：显示短/长 Toast。
- `cancel`：取消当前 Toast。

### `DisplayUtil`
- `dp2px/px2dp`：dp 与 px 换算。
- `sp2px/px2sp`：sp 与 px 换算。
- `dpToPx/pxToDp/spToPx/pxToSp`：语义化别名。

### `ScreenUtil`
- `screenWidthPx/screenHeightPx`：获取屏幕宽高（px）。
- `densityDpi`：获取屏幕密度 DPI。
- `isLandscape/isPortrait`：判断当前方向。
- `systemBarInsets(view)`：读取当前窗口系统栏 Insets（优先实时值）。
- `availableContentWidthPx/availableContentHeightPx`：可用内容区宽高（优先 Insets，失败回退资源维度估算）。
- `statusBarHeight`：状态栏高度（px）。
- `navigationBarHeight`：导航栏高度（px，无导航栏时为 0）。
- `hasNavigationBar`：是否存在导航栏（资源维度推断）。

### `KeyboardUtil`
- `show(view)`：请求显示软键盘。
- `hide(activity)`：基于当前焦点隐藏软键盘。
- `hide(view)`：基于 view token 隐藏软键盘。

### `ResourceUtil`
- `string`：读取字符串资源（支持格式化参数）。
- `quantityString`：读取复数字符串资源。
- `color`：读取颜色资源。
- `drawable`：读取 Drawable 资源。
- `dimenPx`：读取尺寸资源（px）。

### `ViewUtil`
- `visible/invisible/gone`：设置可见性。
- `setVisible`：按条件切换可见状态。
- `setEnabled`：批量 enabled 状态。
- `setSelected`：批量 selected 状态。

## Text / Number / Format

### `StringUtil`
- `isBlank`：空白检查。
- `orEmptyTrim`：trim 后空安全返回。
- `nullIfBlank`：空白转 null。
- `equalsIgnoreCase`：忽略大小写比较。
- `ellipsize`：截断并追加后缀。

### `NumberUtil`
- `parseInt/parseLong/parseDouble`：安全解析（失败返回默认值）。
- `clamp`：按区间约束 Int。
- `round`：按小数位四舍五入。
- `formatDecimal`：格式化小数并可选去掉尾零。

### `FormatUtil`
- `formatDuration`：毫秒转 `mm:ss` / `HH:mm:ss`。
- `formatFileSize`：字节转可读大小文本。
- `maskPhone`：手机号脱敏。

### `ValidateUtil`
- `isEmail`：邮箱校验。
- `isUrl`：URL 校验。
- `isMobileCN`：中国大陆手机号校验。
- `isIpV4`：IPv4 校验。
- `isStrongPassword`：强密码校验。

### `RegexUtil`
- `isMatch`：正则匹配。
- `findFirst`：提取首个匹配项。
- `findAll`：提取全部匹配项。
- `replace`：正则替换。
- `split`：正则分割。

### `CollectionUtil`
- `isNullOrEmpty`：集合是否为空。
- `joinToStringSafe`：空安全拼接。
- `distinctStable`：去重并保序。
- `chunkedSafe`：安全分块。
- `mutableListOfNotNull`：构建可变非空列表。

### `MapUtil`
- `getOrDefault`：带默认值读取。
- `merge`：合并 Map（后者覆盖前者）。
- `filterNotNullValues`：过滤空值项。
- `toMutable`：转可变副本。

### `TemplateUtil`
- `render`：按 `%{key}` 占位语法渲染模板，缺失 key 替换为空串。
- `keys`：提取模板中全部占位 key（去重）。

### `UrlParamUtil`
- `build`：将参数 map 追加到 URL/query（自动处理 `#fragment` 与末尾 `?`/`&`）。
- `parse`：将 URL 或 query 字符串解析为键值对。
- `append`：便捷追加单个 query 参数。

### `VersionUtil`
- `compare`：按数字段逐位比较版本号。
- `isAtLeast`：判断当前版本是否满足最小目标版本。

### `MaskUtil`
- `maskPhone`：手机号脱敏（前 3 后 4）。
- `maskEmail`：邮箱脱敏（用户名仅保留首字符）。
- `maskIdCard`：证件号按前后保留位数脱敏。

### `CaseUtil`
- `camelToSnake`：驼峰转下划线。
- `snakeToCamel`：下划线转小驼峰。
- `capitalizeFirst`：首字母大写。
- `decapitalizeFirst`：首字母小写。

### `DecimalUtil`
- `add/subtract/multiply`：基于 BigDecimal 的精确运算并按 scale 输出。
- `divide`：精确除法，除数为 0 返回 `"0"`。
- `compare`：比较两个数值大小。

### `BooleanUtil`
- `parse`：文本转布尔（支持兜底）。
- `toggle`：布尔取反。
- `toInt`：布尔转 1/0。

## Encode / Encrypt / JSON / ID

### `EncodeUtil`
- `base64Encode/base64Decode/base64DecodeToBytes`：Base64 编解码。
- `urlEncode/urlDecode/urlDecodeOrNull`：URL 编解码。
- `base64UrlEncode/base64UrlDecode/base64UrlDecodeToBytes`：URL-SAFE Base64。
- `md5/sha256`：摘要哈希。

### `EncryptUtil`
- `aesEncrypt`：AES-CBC 加密（UTF-8 -> Base64(iv+cipher)）。
- `aesDecrypt`：AES-CBC 解密（Base64(iv+cipher) -> UTF-8）。
- `hmacSha256`：HMAC-SHA256 十六进制摘要。

### `JsonUtil`
- `toJson`：Map 转 JSON。
- `pretty/compact`：美化/压缩 JSON。
- `parseObject/parseArray`：解析对象/数组。
- `optString/optInt/optLong/optBoolean/optDouble`：安全读取标量字段。
- `isObject/isArray`：JSON 类型判断。
- `optObject/optArray`：安全读取嵌套对象/数组。

### `HexUtil`
- `bytesToHex`：字节转小写 hex。
- `hexToBytes`：hex 转字节。
- `stringToHexUtf8`：UTF-8 文本转 hex。
- `hexToStringUtf8`：hex 转 UTF-8 文本。

### `ChecksumUtil`
- `crc32`：CRC32。
- `adler32`：Adler32。
- `xorChecksum`：异或校验值。

### `IdUtil`
- `uuid`：带/不带连接符 UUID。
- `shortId`：固定长度短 ID。
- `timeBasedId`：基于时间戳 + 可选计数尾巴的 ID。

## Time / Math / Random

### `DateTimeUtil`
- `nowMillis`：当前时间戳。
- `format`：时间戳格式化。
- `parse`：宽松解析文本为时间戳。
- `parseStrict`：严格解析文本为时间戳（非法日期返回 null）。
- `isToday`：是否今天。

### `DateRangeUtil`
- `isInRange`：闭区间判断。
- `startOfDay/endOfDay`：当天起止时间。
- `overlap`：两个时间区间是否相交。

### `BenchmarkUtil`
- `measure`：单次执行耗时。
- `repeatMeasure`：多次执行总耗时。

### `MathUtil`
- `gcd`：最大公约数。
- `lcm`：最小公倍数。
- `isEven`：偶数判断。
- `percentage`：百分比计算（按小数位四舍五入）。

### `RandomUtil`
- `nextInt`：随机 Int 区间值。
- `nextLong`：随机 Long 区间值。
- `nextBoolean`：随机布尔值。
- `randomString`：按字符集生成随机字符串。
