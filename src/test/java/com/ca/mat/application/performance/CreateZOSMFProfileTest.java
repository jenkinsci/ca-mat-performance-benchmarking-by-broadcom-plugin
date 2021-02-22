package com.ca.mat.application.performance;

import com.ca.mat.application.performance.view.CreateZOSMFProfile;
import com.ca.mat.application.performance.view.Root;
import com.ca.mat.application.performance.view.PerformanceBenchmarking;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import hudson.XmlFile;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateZOSMFProfileTest {

    public static final String CONFIGURE_URL = "configure";
    public static final String CONFIG_FORM_NAME = "config";
    @Rule
    public JenkinsRule j = new JenkinsRule();

	public List<CreateZOSMFProfile.AddProfile> configRoundtrip(CreateZOSMFProfile.AddProfile... entries) throws Exception {
    	CreateZOSMFProfile crm = new CreateZOSMFProfile();
        XmlFile configFile = crm.getConfigFile();
        crm.setConfig(new CreateZOSMFProfile.Config(ImmutableList.copyOf(entries)));
        configFile.write(crm);
        long savedManually = configFile.getFile().lastModified();

        configRoundtrip(crm);
        long savedViaWebInterface = configFile.getFile().lastModified();

        assertThat(savedManually).isLessThan(savedViaWebInterface);

        CreateZOSMFProfile saved = new CreateZOSMFProfile();

        return saved.getConfig().getEntries();
    }

    private void configRoundtrip(PerformanceBenchmarking performanceBenchmarking) throws Exception {
        j.submit(j.createWebClient().goTo(getUiSampleConfigureUrl(performanceBenchmarking)).getFormByName(CONFIG_FORM_NAME));
    }

    private String getUiSampleConfigureUrl(PerformanceBenchmarking performanceBenchmarking) {
        return Joiner.on('/').join(new Root().getUrlName(), performanceBenchmarking.getUrlName(), CONFIGURE_URL);
    }
}
