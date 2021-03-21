package io.github.yuvv.i18nhelper;

import me.xdrop.fuzzywuzzy.Applicable;
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * msg 去重
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "deduplicate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MsgDeduplication extends AbstractMsgMojo {

    /**
     * message 相似度阈值 1-99
     */
    @Parameter(defaultValue = "85", property = "msg.msgSimilarityCutoff", readonly = true)
    private int msgSimilarityCutoff;

    @Override
    public void execute() throws MojoExecutionException {
        File msgFile = new File(msgDirectory, msgBaseName + ".properties");
        if (!msgFile.exists()) {
            throw new MojoExecutionException("File " + msgFile.getAbsolutePath() + " not exists");
        }
        Properties msgProperties = new Properties();
        try (InputStreamReader isReader = new InputStreamReader(new FileInputStream(msgFile), StandardCharsets.UTF_8)) {
            msgProperties.load(isReader);
            // 转换为 msg 到具体 code 的 map
            Map<String, String> valueKeyMap = new HashMap<>(msgProperties.size());
            msgProperties.forEach((k, v) -> {
                String kStr = k.toString();
                String vStr = v.toString();
                if (valueKeyMap.containsKey(vStr)) {
                    getLog().warn("Value `" + kStr + "=" + vStr + "` duplicated with `" + valueKeyMap.get(vStr) + "=" + vStr + "`");
                }
                valueKeyMap.put(vStr, kStr);
            });
            Set<String> propNameSet = new HashSet<>(valueKeyMap.keySet());
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
                if (list.size() < 2) {
                    // 只有一个元素的直接忽略
                    return;
                }
                Iterator<ExtractedResult> iterator = list.iterator();
                ExtractedResult first = iterator.next();
                getLog().info("[ " + first.getString() + " = " + valueKeyMap.get(first.getString()) + " ] highly similar to:");
                while (iterator.hasNext()) {
                    ExtractedResult next = iterator.next();
                    getLog().info("    [ " + next.getString() + " = " + valueKeyMap.get(next.getString()) + " ] --> score=" + next.getScore());
                }
            });
        } catch (IOException e) {
            getLog().error(e);
        }
    }
}
