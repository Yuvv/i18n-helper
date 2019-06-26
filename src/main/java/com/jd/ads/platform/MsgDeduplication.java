package com.jd.ads.platform;

import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.Extractor;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * msg 去重
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "deduplicate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MsgDeduplication extends AbstractMojo {

    /**
     * messages 文件名前缀
     */
    @Parameter(defaultValue = "messages", property = "msg.msgBaseName", readonly = true)
    private String msgBaseName;

    /**
     * messages 文件输出文件夹
     */
    @Parameter(defaultValue = "${project.build.directory}/resources/i18n", property = "msg.msgDir", required = true)
    private File msgDir;

    /**
     * message 相似度阈值 1-100
     */
    @Parameter(defaultValue = "85", property = "msg.msgSimilarityCutoff", readonly = true)
    private int msgSimilarityCutoff;

    @Override
    public void execute() throws MojoExecutionException {
        File msgFile = new File(msgDir, msgBaseName + ".properties");
        if (!msgFile.exists()) {
            throw new MojoExecutionException("File " + msgFile.getAbsolutePath() + " not exists");
        }
        Properties msgProperties = new Properties();
        try {
            msgProperties.load(new FileInputStream(msgFile));
            Set<String> propNameSet = msgProperties.stringPropertyNames();
            List<List<ExtractedResult>> similarPropNames = new ArrayList<>();

            Applicable func = new WeightedRatio();
            while (!propNameSet.isEmpty()) {
                Iterator<String> iterator = propNameSet.iterator();
                if (iterator.hasNext()) {
                    String query = iterator.next();
                    iterator.remove();
                    // 过滤
                    List<ExtractedResult> best = new ArrayList<>();
                    // index 在这里没有什么意义
                    int index = 1;
                    while (iterator.hasNext()) {
                        String curStr = iterator.next();
                        int score = func.apply(query, curStr);
                        if (score >= msgSimilarityCutoff) {
                            best.add(new ExtractedResult(curStr, score, index));
                            iterator.remove();
                        }
                        index++;
                    }

                    best.sort(Comparator.reverseOrder());
                    best.add(0, new ExtractedResult(query, 100, 0));
                    similarPropNames.add(best);
                }
            }

            similarPropNames.forEach(list -> {
                list.forEach(each -> {
                    getLog().info(each.getString() + "--> score=" + each.getScore());
                });
                getLog().info("~~");
            });
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}
