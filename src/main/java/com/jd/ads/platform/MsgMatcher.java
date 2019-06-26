package com.jd.ads.platform;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Locale;

/**
 * Goal which touches a timestamp file.
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "match", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MsgMatcher extends AbstractMojo {

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
    private Locale[] locales;

    public void setLocales(String[] locales) {
        if (locales == null || locales.length == 0) {
            this.locales = null;
            return;
        }
        this.locales = new Locale[locales.length];
        for (int i = 0; i< locales.length; i++) {
            this.locales[i] = Locale.forLanguageTag(locales[i]);
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        // todo:
        getLog().info("to be continue...");
    }
}
