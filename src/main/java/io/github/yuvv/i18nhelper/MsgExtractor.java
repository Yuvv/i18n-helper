package io.github.yuvv.i18nhelper;

import io.github.yuvv.i18nhelper.misc.encounter.LongEncounter;
import io.github.yuvv.i18nhelper.misc.generator.Generator;
import io.github.yuvv.i18nhelper.misc.generator.MsgKeyGenerator;
import io.github.yuvv.i18nhelper.misc.prop.MsgProperties;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 提取
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "extract", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MsgExtractor extends AbstractMsgMojo {

    /**
     * message 参数正则
     */
    private static final Pattern MSG_PARAM_PATTERN = Pattern.compile("\" ?\\+ ?[\\w.()]+? ?\\+ ?\"");

    /**
     * 工程路径
     */
    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File projectBaseDir;

    /**
     * 生成的文件是否使用绝对路径
     */
    @Parameter(defaultValue = "false", readonly = true)
    private boolean commentAbsolutePath;

    /**
     * 包含 messages 的代码扫描目录
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "scanDirectories", required = true)
    private File[] scanDirectories;

    /**
     * 代码中的 messages 匹配正则
     */
    @Parameter(property = "msgPatterns", required = true)
    private Pattern[] msgPatterns;

    /**
     * 扫描目录下需要读取解析的文件
     */
    @Parameter(defaultValue = ".*?\\.java", property = "includeFilePatterns", required = true)
    private Pattern[] includeFilePatterns;

    /**
     * 扫描目录下需要排除的文件
     */
    @Parameter(property = "excludeFilePatterns")
    private Pattern[] excludeFilePatterns;

    public void setMsgPatterns(String[] msgPatterns) {
        this.msgPatterns = new Pattern[msgPatterns.length];
        for (int i = 0; i < msgPatterns.length; i++) {
            this.msgPatterns[i] = Pattern.compile(msgPatterns[i]);
        }
    }

    public void setIncludeFilePatterns(String[] includeFilePatterns) {
        if (includeFilePatterns == null) {
            return;
        }
        this.includeFilePatterns = new Pattern[includeFilePatterns.length];
        for (int i = 0; i < includeFilePatterns.length; i++) {
            this.includeFilePatterns[i] = Pattern.compile(includeFilePatterns[i]);
        }
    }

    public void setExcludeFilePatterns(String[] excludeFilePatterns) {
        if (excludeFilePatterns == null) {
            return;
        }
        this.excludeFilePatterns = new Pattern[excludeFilePatterns.length];
        for (int i = 0; i < excludeFilePatterns.length; i++) {
            this.excludeFilePatterns[i] = Pattern.compile(excludeFilePatterns[i]);
        }
    }

    /**
     * 通过逐行扫描，利用正则匹配，从代码中获取 msg
     *
     * @return msg -> path -> lineNo 的映射
     */
    private Map<String, Map<Path, List<Long>>> getMsgResult() {
        // 使用 tree map 保证有序
        Map<String, Map<Path, List<Long>>> msgResultMap = new TreeMap<>();
        for (File scanDir : scanDirectories) {
            try {
                Files.walk(scanDir.toPath())
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            if (excludeFilePatterns != null && excludeFilePatterns.length > 0) {
                                for (Pattern pattern : excludeFilePatterns) {
                                    if (pattern.matcher(path.toAbsolutePath().toString()).matches()) {
                                        getLog().info("File " + path.toString() + " excluded");
                                        return false;
                                    }
                                }
                            }
                            if (includeFilePatterns != null && includeFilePatterns.length > 0) {
                                for (Pattern pattern : includeFilePatterns) {
                                    if (pattern.matcher(path.toAbsolutePath().toString()).matches()) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }).forEach(filePath -> {
                    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath.toFile()))) {
                        LongEncounter encounter = new LongEncounter(1L);
                        bufferedReader.lines().forEachOrdered(line -> {
                            for (Pattern pattern : msgPatterns) {
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.matches()) {
                                    String msg = matcher.group("msg");
                                    String[] msgSegments = MSG_PARAM_PATTERN.split(msg);
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < msgSegments.length; i++) {
                                        sb.append(msgSegments[i]);
                                        sb.append('{');
                                        sb.append(i);
                                        sb.append('}');
                                    }
                                    sb.delete(sb.length() - 3, sb.length());
                                    // 使用 tree map 保证有序
                                    msgResultMap.computeIfAbsent(sb.toString(), k -> new TreeMap<>())
                                            .computeIfAbsent(filePath, path -> new ArrayList<>()).add(encounter.getCount());
                                }
                            }
                            encounter.increase();
                        });
                        getLog().info("File " + filePath.toString() + " processing completed");
                    } catch (IOException e) {
                        getLog().error("Read file[path=" + filePath.toString() + "] failed", e);
                    }
                });
            } catch (IOException e) {
                getLog().error(e);
            }
        }

        return msgResultMap;
    }

    @Override
    public void execute() throws MojoExecutionException {
        // --> debug info
        getLog().debug("Message Patterns:");
        for (Pattern msgPattern : msgPatterns) {
            getLog().debug("\t" + msgPattern.toString());
        }

        // --> main logic begins
        File dir = msgDirectory;
        if (!dir.exists()) {
            // 输出目录不存在的话先递归创建输出目录
            boolean success = dir.mkdirs();
            if (!success) {
                throw new MojoExecutionException("Create directory `" + msgDirectory.getAbsolutePath() + "` failed.");
            }
        }
        // 读取已有的配置（如果存在的话）
        MsgProperties prop = new MsgProperties();
        File msgFile = new File(dir, msgBaseName + ".properties");
        if (msgFile.exists()) {
            try (FileInputStream is = new FileInputStream(msgFile)) {
                prop.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            } catch (IOException e) {
                getLog().error(e);
            }
        }
        // 将已有配置存储到 msg -> msgKey 的 map 里面
        Map<String, String> valueKeyMap = new HashMap<>(prop.size());
        prop.forEach((k, v) -> valueKeyMap.put(v, k));
        // 从代码中抽取 message，msg -> path -> lineNo 的映射
        Map<String, Map<Path, List<Long>>> msgResultMap = getMsgResult();
        Generator keyGenerator = new MsgKeyGenerator();
        int subStrIndex = commentAbsolutePath ? 0 : projectBaseDir.getAbsolutePath().length() + 1;
        for (Map.Entry<String, Map<Path, List<Long>>> entry : msgResultMap.entrySet()) {
            Map<String, Set<Integer>> comments = entry.getValue().entrySet().stream()
                    // 写入文件相对于工程/module目录的相对路径，同时确保路径分隔符一致
                    .collect(Collectors.toMap(e -> e.getKey().toString().substring(subStrIndex).replaceAll("\\\\", "/"),
                            // 写入文件对应的行号（逗号分隔）
                            e -> e.getValue().stream().filter(Objects::nonNull).map(Long::intValue).collect(Collectors.toSet())));

            String msgKey;
            if (valueKeyMap.containsKey(entry.getKey())) {
                // 如果已经存在则使用已经配置好的 msgKey，追加注释即可
                msgKey = valueKeyMap.get(entry.getKey());
                prop.mergeComment(msgKey, comments);
            } else {
                if (entry.getKey().startsWith("{") && entry.getKey().endsWith("}")
                        && prop.containsKey(entry.getKey().substring(1, entry.getKey().length() - 1))) {
                    // 已经做替换的，更新注释即可
                    prop.putComment(entry.getKey().substring(1, entry.getKey().length() - 1), comments);
                } else {
                    // 否则按照 hashCode 生成一个 msgKey
                    msgKey = keyGenerator.generate(entry.getKey());

                    prop.put(msgKey, entry.getKey(), comments);
                }
            }
        }
        // 写回 message 文件
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(msgFile), StandardCharsets.UTF_8)) {
            prop.store(writer);
            writer.flush();
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}
