package com.jd.ads.platform;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * msg 去重
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "deduplicate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MsgDeduplication extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        // todo:
        getLog().info("to be continue...");
    }
}
