package io.github.yuvv.i18nhelper.misc.prop;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MsgProperties
 *
 * @author Yuvv
 * @date 2021/1/4
 */
public class MsgProperties extends LinkedHashMap<String, String> {

    /**
     * msg key -> file path -> line no
     */
    private transient volatile ConcurrentHashMap<String, Map<String, Set<Integer>>> comments;

    /**
     * 初始化
     *
     * @param initialCapacity 大小，没啥大用
     */
    public MsgProperties(int initialCapacity) {
        super(initialCapacity);
        this.comments = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * 初始化
     */
    public MsgProperties() {
        this(8);
    }

    /**
     * 从文件加载，进识别 UTF-8 编码
     *
     * @param reader 待读取对象
     * @throws IOException 可能发生的异常
     */
    public synchronized void load(Reader reader) throws IOException {
        Objects.requireNonNull(reader, "reader parameter is null");
        load0(new BufferedReader(reader));
    }

    /**
     * 从文件加载，进识别 UTF-8 编码
     *
     * @param inStream 待读取对象
     * @throws IOException 可能发生的异常
     */
    public synchronized void load(InputStream inStream) throws IOException {
        Objects.requireNonNull(inStream, "inStream parameter is null");
        load0(new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8)));
    }

    private void load0(BufferedReader bufferedReader) throws IOException {
        String line = null;

        Map<String, Set<Integer>> fileLineNoMap = new LinkedHashMap<>(8);
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                // empty line
                continue;
            }
            String key = null, value = null;
            if (line.charAt(0) == '!' || line.charAt(0) == '#') {
                // comment mode
                int semIndex = line.indexOf(':');
                if (semIndex < 0) {
                    // bad comment, ignore it
                    continue;
                }
                try {
                    String filename = line.substring(1, semIndex).trim();
                    Set<Integer> fileNoSet = Stream.of(line.substring(semIndex + 1).split(","))
                            .filter(Objects::nonNull)
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .collect(Collectors.toSet());
                    if (!filename.isEmpty() && !fileNoSet.isEmpty()) {
                        fileLineNoMap.computeIfAbsent(filename, k -> new HashSet<>()).addAll(fileNoSet);
                    }
                } catch (Exception ignored) {
                }
            } else {
                // key-value mode
                int eqIndex = line.indexOf('=');
                if (eqIndex < 0) {
                    // bad key-value, ignore it
                    fileLineNoMap.clear();
                    continue;
                }
                key = line.substring(0, eqIndex).trim();
                StringBuilder valueStrBuilder = new StringBuilder();
                String vStr = line.substring(eqIndex + 1).trim();
                boolean continuesMode = false;
                while (vStr != null) {
                    for (int i = 0; i < vStr.length(); i++) {
                        if (vStr.charAt(i) != '\\' || continuesMode) {
                            valueStrBuilder.append(vStr.charAt(i));
                            continuesMode = false;
                        } else {
                            continuesMode = true;
                        }
                    }
                    if (continuesMode) {
                        vStr = bufferedReader.readLine();
                        if (vStr != null) {
                            vStr = vStr.trim();
                        }
                    } else {
                        vStr = null;
                    }
                }
                value = valueStrBuilder.toString();
            }
            if (key != null && !key.isEmpty()) {
                // add k-v to messages
                super.put(key, value);
                // add k-f-l to comments
                comments.put(key, fileLineNoMap);

                // reset fileLineNoMap -> must be a new instance
                fileLineNoMap = new LinkedHashMap<>(8);
            }
        }
    }

    /**
     * 存储到文件，仅使用 UTF-8 编码
     *
     * @param writer 待写入对象
     * @throws IOException 可能的异常
     */
    public void store(Writer writer)
            throws IOException {
        store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer),
                false);
    }

    /**
     * 存储到文件，仅使用 UTF-8 编码
     *
     * @param out 待写入对象
     * @throws IOException 可能的异常
     */
    public void store(OutputStream out)
            throws IOException {
        store0(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)),
                true);
    }

    private void store0(BufferedWriter bw, boolean escUnicode)
            throws IOException {
        synchronized (this) {
            bw.write("# --------------------- GENERATED BY I18N-HELPER ---------------------");
            bw.newLine();
            for (Map.Entry<String, String> e : this.entrySet()) {
                // comments
                Map<String, Set<Integer>> commentContent = this.comments.get(e.getKey());
                if (commentContent != null && !commentContent.isEmpty()) {
                    // 间隔一行
                    bw.newLine();
                    for (Map.Entry<String, Set<Integer>> ce : commentContent.entrySet()) {
                        bw.write("# ");
                        bw.write(ce.getKey());
                        bw.write(":");
                        bw.write(ce.getValue().stream().filter(Objects::nonNull).sorted().map(Objects::toString).collect(Collectors.joining(",")));
                        bw.newLine();
                    }
                }
                // k-v
                bw.write(e.getKey());
                bw.write(" = ");
                bw.write(e.getValue().replaceAll("\\\\", "\\\\\\\\").trim());
                bw.newLine();
            }
        }
        bw.flush();
    }

    public synchronized String put(String key, String value, Map<String, Set<Integer>> comments) {
        this.mergeComment(key, comments);
        return this.put(key, value);
    }

    public boolean mergeComment(String key, Map<String, Set<Integer>> comments) {
        if (comments == null) {
            return false;
        }
        Map<String, Set<Integer>> curCommentMap = this.comments.computeIfAbsent(key, k -> new LinkedHashMap<>());
        for (Map.Entry<String, Set<Integer>> ce : comments.entrySet()) {
            curCommentMap.computeIfAbsent(ce.getKey(), k -> new HashSet<>()).addAll(ce.getValue());
        }
        return true;
    }

    public boolean mergeComment(String key, String filepath, Collection<Integer> linenumber) {
        if (filepath == null || linenumber == null || linenumber.isEmpty()) {
            return false;
        }
        Map<String, Set<Integer>> curCommentMap = this.comments.computeIfAbsent(key, k -> new LinkedHashMap<>());
        curCommentMap.computeIfAbsent(filepath, k -> new HashSet<>()).addAll(linenumber);
        return true;
    }

    public boolean putComment(String key, Map<String, Set<Integer>> comments) {
        if (comments == null) {
            return false;
        }
        Map<String, Set<Integer>> curCommentMap = this.comments.computeIfAbsent(key, k -> new LinkedHashMap<>());
        curCommentMap.putAll(comments);
        return true;
    }
}
