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

输出文件中会自动为其生成一个 `key`，并添加相关的代码文件和 `message` 所在的行数的注释，以便快速定位 `message` 的位置。

写文件采用追加的方式，若有重复的 `message` 则会给出提示，但该 `message` 仍会被写入文件。后续可以自行查找或通过下面的`deduplication`
进行去重操作。

**可配置项**：
- `scanDir`: 待提取源代码扫描目录。
- `msgDir`: `messages`资源文件所在目录。
- `msgPatterns`: message 字符串正则。必填。
- `includeFilePatterns`: 需要扫描的文件绝对路径正则。默认为`.*\.java`。(需要格外注意路径正则中的间隔符的编写，以防因为系统差异而无法匹配。下同)
- `excludeFilePatterns`: 需要排除的文件绝对路径正则。非必填，默认为空，不额外排除文件。
- `commentAbsolutePath`: 写文件时所用的文件路径是否使用绝对路径，默认为`false`，使用相对于project/module的相对路径。

**示例配置**：
```xml
<configuration>
    <scanDir>${basedir}/src/main/java/com/jd/ads/cpd/base/services/core/</scanDir>
    <msgDir>${basedir}/src/main/resources/i18n/</msgDir>
    <msgPatterns>
        <pattern><![CDATA[  .*?XyzResult.fail\("(?<msg>.*?)"\).*  ]]></pattern>
        <pattern><![CDATA[  .*?@[a-zA-Z]+?\(.*?message ?= ?"(?<msg>.*?)".*?\).*  ]]></pattern>
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
- `msgDir`: 同上，不赘述。
- `msgBaseName`: 同上，不赘述。
- `matchLocales`：需要匹配的语言包类型，若不填写则会处理 msg 路径下所有语言包
- `defaultLocale`：默认语言包语言类型，若不设置则会取系统默认语言。

**示例配置**：
```xml
<configuration>
    <scanDir>${basedir}/src/main/java/com/jd/ads/cpd/base/services/core/</scanDir>
    <msgDir>${basedir}/src/main/resources/i18n/</msgDir>
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

`MsgDeduplication` 用于对比资源文件中 message 的差异，并想相似的 message 归并，以便用户手动处理。

> PS: 目前对中文的支持其实不是很好，后续再优化 😂😂😂

**可配置项**：
- `msgDir`: 同上，不赘述。
- `msgBaseName`: 同上，不赘述。
- `msgSimilarityCutoff`：message 之间相似度的阈值，默认为85。

**示例配置**：
```xml
<configuration>
    <scanDir>${basedir}/src/main/java/com/jd/ads/cpd/base/services/core/</scanDir>
    <msgDir>${basedir}/src/main/resources/i18n/</msgDir>
    <msgSimilarityCutoff>75</msgSimilarityCutoff>
</configuration>
```
