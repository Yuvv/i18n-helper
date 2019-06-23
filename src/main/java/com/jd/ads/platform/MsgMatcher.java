package com.jd.ads.platform;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which touches a timestamp file.
 *
 * @author Yuvv
 * @date 2019/06/23
 */
@Mojo(name = "match", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class MsgMatcher extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        // todo:
        getLog().info("to be continue...");
    }
}
