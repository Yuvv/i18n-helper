# i18n-helper

`MessageSource` 辅助提取、模糊匹配、多语言校验插件。

引入：
```xml
<plugin>
    <groupId>io.github.yuvv</groupId>
    <artifactId>i18n-helper</artifactId>
    <version>1.1-SNAPSHOT</version>
</plugin>
```


## MsgExtractor

从现有项目中提取 message 字符串，并自动将带参数类型转换为`{num}`的格式化形式，将结果输出到默认的`messages`资源文件。
如果`messages`资源文件不存在，则会自动创建；若已存在，则会将扫描结果合并。

如果提取出来的 `message` 已经在资源文件中存在，则会使用文件中设置的 key；
如果不存在，则会自动为其生成一个 `key`，并添加相关的代码文件和 `message` 所在的行数的注释，以便快速定位 `message` 的位置。

写文件采用覆盖的方式，原有的 `message` 内容会和新的 `message` 内容合并。由于默认按照 `message` 值的自然顺序进行排序，
所以不用担心每次运行都会打乱结构的问题。

**可配置项**：
- `scanDirectories`: 待提取源代码扫描目录。
- `msgDirectory`: `messages`资源文件所在目录。
- `msgPatterns`: message 字符串正则。必填，必须包含 `msg` 分组。
- `msgKeyPatterns`: messageKey 字符串正则。非必填，填的话必须包含 `msgKey` 分组。
- `includeFilePatterns`: 需要扫描的文件绝对路径正则。默认为`.*\.java`。(需要格外注意路径正则中的间隔符的编写，以防因为系统差异而无法匹配。下同)
- `excludeFilePatterns`: 需要排除的文件绝对路径正则。非必填，默认为空，不额外排除文件。
- `commentAbsolutePath`: 写文件时所用的文件路径是否使用绝对路径，默认为`false`，使用相对于project/module的相对路径。

**示例配置**：
```xml
<configuration>
    <scanDirectories>
        <directory>${basedir}/src/main/java/path/to/your/project/consumer/</directory>
        <directory>${basedir}/src/main/java/path/to/your/project/provider/</directory>
    </scanDirectories>
    <msgDirectory>${basedir}/src/main/resources/i18n/</msgDirectory>
    <msgKeyPatterns>
        <pattern><![CDATA[  translate\("(?<msgKey>.*?)"\)  ]]></pattern>
    </msgKeyPatterns>
    <msgPatterns>
        <pattern><![CDATA[  XyzResult.fail\("(?<msg>.*?)"\)  ]]></pattern>
        <pattern><![CDATA[  @[a-zA-Z]+?\(.*?message ?= ?"(?<msg>.*?)".*?\)  ]]></pattern>
    </msgPatterns>
    <includeFilePatterns>
        <pattern>.*\.java</pattern>
    </includeFilePatterns>
    <excludeFilePatterns>
        <pattern>.*([/\\])domain\1.*\.java</pattern>
        <pattern>.*(?:VO|DTO)\.java</pattern>
    </excludeFilePatterns>
</configuration>
```


## MsgMatcher

多语言情况下容易出现只在其中一个或几个语言资源文件里面有更新而其它的忘记更新的情况。如：

1. 在默认语言包里面添加忘了在其它语言添加。
2. 在默认语言删除忘了在其他语言删除。
3. 默认语言包与显示声明的默认语言包相同key的value不同。

`MsgMatcher` 用于对比这些资源文件的差异，并给出相应的提示，以便人工做出处理。

**可配置项**：
- `msgDirectory`: 同上，不赘述。
- `msgBaseName`: 同上，不赘述。
- `matchLocales`：需要匹配的语言包类型，若不填写则会处理 msg 路径下所有语言包
- `defaultLocale`：默认语言包语言类型，若不设置则会取系统默认语言。

**示例配置**：
```xml
<configuration>
    <scanDirectories>
        <directory>${basedir}/src/main/java/path/to/your/project/consumer/</directory>
        <directory>${basedir}/src/main/java/path/to/your/project/provider/</directory>
    </scanDirectories>
    <msgDirectory>${basedir}/src/main/resources/i18n/</msgDirectory>
    <defaultLocale>zh-CN</defaultLocale>
    <matchLocales>
        <locale>en-US</locale>
        <locale>jp</locale>
    </matchLocales>
</configuration>
```

## MsgDeduplication

用于提示的 message 很容易出现差异很小、内容相似的情况，
这种时候将其内容修改为一致或者将其中部分差异提取为参数可以减小维护工作量，缩小资源文件大小。

文本相似度使用 [fuzzywuzzy](https://github.com/xdrop/fuzzywuzzy) 实现。

`MsgDeduplication` 用于对比资源文件中 message 的差异，并想相似的 message 归并，以便用户手动处理。

> PS: 目前对中文的支持其实不是很好，后续再优化 😂😂😂

**可配置项**：
- `msgDirectory`: 同上，不赘述。
- `msgBaseName`: 同上，不赘述。
- `msgSimilarityCutoff`：message 之间相似度的阈值，默认为85。

**示例配置**：
```xml
<configuration>
    <scanDirectories>
        <directory>${basedir}/src/main/java/path/to/your/project/consumer/</directory>
        <directory>${basedir}/src/main/java/path/to/your/project/provider/</directory>
    </scanDirectories>
    <msgDirectory>${basedir}/src/main/resources/i18n/</msgDirectory>
    <msgSimilarityCutoff>75</msgSimilarityCutoff>
</configuration>
```
