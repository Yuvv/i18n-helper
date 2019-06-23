# i18n-helper

`MessageSource` 辅助提取、模糊匹配、多语言校验插件。

引入：
```xml
<plugin>
    <groupId>com.jd.ads.platform</groupId>
    <artifactId>i18n-helper</artifactId>
    <version>1.0-SNAPSHOT</version>
</plugin>
```


## MsgExtractor

从现有项目中提取 message 字符串，并自动将带参数类型转换为`{num}`的格式化形式，将结果输出到默认的`messages`资源文件。

**可配置项**：
- `scanDir`: 待提取源代码扫描目录。
- `outputDir`: `messages`资源文件输出目录。
- `msgPatterns`: message 字符串正则。必填。
- `includePatterns`: 需要扫描的文件绝对路径正则。
- `excludePatterns`: 需要排除的文件绝对路径正则。非必填，默认为空，不额外排除文件，

**示例配置**：
```xml
<configuration>
    <scanDir>${basedir}/src/main/java/com/jd/ads/cpd/base/services/core/</scanDir>
    <outputDir>${basedir}/src/main/resources/i18n/</outputDir>
    <msgPatterns>
        <pattern><![CDATA[  .*?XyzResult.fail\("(?<msg>.*?)"\).*  ]]></pattern>
        <pattern><![CDATA[  .*?@[a-zA-Z]+?\(.*?message ?= ?"(?<msg>.*?)".*?\).*  ]]></pattern>
    </msgPatterns>
    <includePatterns>
        <pattern>${basedir}/src/main/java/.*\\.java</pattern>
    </includePatterns>
    <excludePatterns>
        <pattern>.*/domain/.*\\.java</pattern>
        <pattern>.*(?:VO|DTO)\\.java</pattern>
    </excludePatterns>
</configuration>
```


## MsgMatcher

多语言情况下容易出现只在其中一个或几个语言资源文件里面有更新而其它的忘记更新的情况。

`MsgMatcher` 用于对比这些资源文件的差异，并给出相应的提示。 

*todo: 开发中。。。*

## MsgDeduplication

用于提示的 message 很容易出现差异很小、内容相似的情况，
这种时候将其内容修改为一致或者将其中部分差异提取为参数可以减小维护工作量，缩小资源文件大小。

`MsgDeduplication` 用于对比资源文件中 message 的差异，并想相似的 message 归并，以便用户手动处理。

*todo: 开发中。。。*
