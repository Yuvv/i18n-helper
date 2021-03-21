package io.github.yuvv.i18nhelper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * AbstractMsgMojo
 *
 * @author Yuvv
 * @date 2021/1/4
 */
public abstract class AbstractMsgMojo extends AbstractMojo {

    /**
     * messages 文件名前缀
     */
    @Parameter(defaultValue = "messages", property = "msgBaseName", readonly = true)
    protected String msgBaseName;

    /**
     * messages 文件输出文件夹
     */
    @Parameter(defaultValue = "${project.build.directory}/resources/i18n", property = "msgDirectory", required = true)
    protected File msgDirectory;
}
