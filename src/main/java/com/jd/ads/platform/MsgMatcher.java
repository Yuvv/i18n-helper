package com.jd.ads.platform;

import com.jd.ads.platform.misc.tuple.Tuple;
import com.jd.ads.platform.misc.tuple.Tuple3;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Goal which touches a timestamp file.
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "match", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MsgMatcher extends AbstractMojo {

    public static final String MSG_FILE_EXT = ".properties";

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
     * 需要匹配的语言，默认全部
     */
    @Parameter(property = "matchLocales")
    private Set<Locale> matchLocales;

    /**
     * 默认语言，不设置的话会取系统默认语言
     */
    @Parameter(defaultValue = "-", property = "defaultLocale", readonly = true)
    private Locale defaultLocale;

    public void setMatchLocales(String[] matchLocales) {
        if (matchLocales == null || matchLocales.length == 0) {
            this.matchLocales = null;
            return;
        }
        this.matchLocales = new HashSet<>(matchLocales.length);
        for (String localeStr : matchLocales) {
            this.matchLocales.add(Locale.forLanguageTag(localeStr));
        }
    }

    public void setDefaultLocale(String defaultLocale) {
        String defaultValue = "-";
        if (defaultLocale == null || defaultLocale.isEmpty() || defaultValue.equals(defaultLocale)) {
            this.defaultLocale = Locale.getDefault();
        } else {
            this.defaultLocale = Locale.forLanguageTag(defaultLocale);
        }
    }

    private Tuple3<String, String, Locale> getFileNameExtLocale(String filename) {
        String name, ext;
        Locale locale;
        int lastPointIdx = filename.lastIndexOf('.');
        int firstUnderlineIdx = msgBaseName.length();
        if (lastPointIdx < 0) {
            name = filename;
            ext = null;
            locale = Locale.forLanguageTag(filename.substring(firstUnderlineIdx + 1));
        } else {
            name = filename.substring(0, lastPointIdx);
            ext = filename.substring(lastPointIdx);
            locale = Locale.forLanguageTag(filename.substring(firstUnderlineIdx + 1, lastPointIdx));
        }

        return Tuple.tuple(name, ext, locale);
    }

    private void logDividingLine() {
        getLog().warn("-----------------------------------------------------------");
    }

    @Override
    public void execute() throws MojoExecutionException {
        File baseMsgFile = new File(msgDir, msgBaseName + MSG_FILE_EXT);
        if (!baseMsgFile.exists()) {
            throw new MojoExecutionException("File " + baseMsgFile.getAbsolutePath() + " not exists");
        }
        try {
            // 读取默认配置
            Properties baseProp = new Properties();
            baseProp.load(new InputStreamReader(new FileInputStream(baseMsgFile), StandardCharsets.UTF_8));
            Set<String> basePropNames = baseProp.stringPropertyNames();
            if (basePropNames.isEmpty()) {
                getLog().warn("Base message file is empty, processing is interrupted");
                return;
            }

            Files.walk(msgDir.toPath())
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        File file = path.toFile();
                        return file.getName().endsWith(MSG_FILE_EXT) && file.getName().startsWith(msgBaseName) && !file.equals(baseMsgFile);
                    }).filter(path -> {
                        File f = path.toFile();
                        String filename = f.getName();

                        if (matchLocales == null || matchLocales.isEmpty()) {
                            return true;
                        }

                        Tuple3<String, String, Locale> fileInfoTuple = getFileNameExtLocale(filename);
                        if (fileInfoTuple.v2 == null) {
                            // 后缀名为空的文件忽略（理论上不会出现这个的，前面已经过滤过后缀名了）
                            return false;
                        }
                        // 设定值不为空的话就只处理设定的
                        return matchLocales.contains(fileInfoTuple.v3);
                    }).forEach(path -> {
                        File curFile = path.toFile();
                        String filename = curFile.getName();
                        try {
                            Tuple3<String, String, Locale> fileInfoTuple = getFileNameExtLocale(filename);
                            Properties curProp = new Properties();
                            curProp.load(new InputStreamReader(new FileInputStream(curFile), StandardCharsets.UTF_8));

                            Set<String> curPropNames = curProp.stringPropertyNames();
                            Set<String> notEqualsDefaultLocalePropNames = new HashSet<>();
                            boolean notTranslateInfoPrinted = false;
                            for (String basePropName : basePropNames) {
                                if (curPropNames.contains(basePropName)) {
                                    curPropNames.remove(basePropName);
                                    if (defaultLocale.equals(fileInfoTuple.v3)) {
                                        if (!baseProp.getProperty(basePropName).equals(curProp.getProperty(basePropName))) {
                                            notEqualsDefaultLocalePropNames.add(basePropName);
                                        }
                                    }
                                } else {
                                    if (!notTranslateInfoPrinted) {
                                        logDividingLine();
                                        getLog().warn("These messages may has not translated in `" + filename + "`");
                                        notTranslateInfoPrinted = true;
                                    }
                                    getLog().warn("    " + basePropName + " = " + baseProp.getProperty(basePropName));
                                }
                            }
                            if (!curPropNames.isEmpty()) {
                                logDividingLine();
                                getLog().warn("These messages may have been removed but still stay in `" + filename + "`");
                                curPropNames.forEach(cpName ->
                                    getLog().warn("    " + cpName + " = " + curProp.getProperty(cpName))
                                );
                            }
                            if (!notEqualsDefaultLocalePropNames.isEmpty()) {
                                logDividingLine();
                                getLog().warn("These messages not equals with default locale message in `" + filename + "`");
                                notEqualsDefaultLocalePropNames.forEach(cpName ->
                                    getLog().warn("    " + cpName + " = " + curProp.getProperty(cpName))
                                );
                            }
                        } catch (IOException e) {
                            getLog().error(e);
                        }
            });

        } catch (IOException e) {
            getLog().error(e);
        }

    }
}
