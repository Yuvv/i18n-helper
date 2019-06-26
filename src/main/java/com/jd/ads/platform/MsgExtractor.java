package com.jd.ads.platform;

import com.jd.ads.platform.misc.encounter.LongEncounter;
import com.jd.ads.platform.misc.tuple.Tuple;
import com.jd.ads.platform.misc.tuple.Tuple2;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提取
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "extract", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MsgExtractor extends AbstractMojo {

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
     * messages 文件名前缀
     */
    @Parameter(defaultValue = "messages", property = "msgBaseName", readonly = true)
    private String msgBaseName;

    /**
     * messages 文件输出文件夹
     */
    @Parameter(defaultValue = "${project.build.directory}/resources/i18n", property = "msgDir", required = true)
    private File msgDir;

    /**
     * 包含 messages 的代码扫描目录
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "scanDir", required = true)
    private File scanDir;

    /**
     * 代码中的 messages 匹配正则
     */
    @Parameter(property = "msg.msgPatterns", required = true)
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

    private Map<String, List<Tuple2<Path, Long>>> getMsgResult() {
        Map<String, List<Tuple2<Path, Long>>> msgResultMap = new HashMap<>();
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
                                msgResultMap.computeIfAbsent(sb.toString(), k -> new ArrayList<>())
                                        .add(Tuple.tuple(filePath, encounter.getCount()));
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

        return msgResultMap;
    }

    @Override
    public void execute() throws MojoExecutionException {
        File dir = msgDir;
        if (!dir.exists()) {
            // 输出目录不存在的话先递归创建输出目录
            boolean success = dir.mkdirs();
            if (!success) {
                throw new MojoExecutionException("Create directory `" + msgDir.getAbsolutePath() + "` failed.");
            }
        }
        File msgFile = new File(dir, msgBaseName + ".properties");
        Properties prop = new Properties();
        // todo: ...

        Map<String, List<Tuple2<Path, Long>>> msgResultMap = getMsgResult();

        try (FileWriter w = new FileWriter(new File(dir, msgBaseName + ".properties"))) {
            int subStrIndex = commentAbsolutePath ? 0 : projectBaseDir.getAbsolutePath().length() + 1;
            for (Map.Entry<String, List<Tuple2<Path, Long>>> entry : msgResultMap.entrySet()) {
                for (Tuple2<Path, Long> tuple : entry.getValue()) {
                    w.write("# ");
                    w.write(tuple.v1.toString().substring(subStrIndex));
                    w.write(':');
                    w.write(tuple.v2.toString());
                    w.write('\n');
                }
                w.write(entry.getKey());
                w.write(" = ");
                w.write(entry.getKey());
                w.write("\n\n");
            }
            w.flush();
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}
