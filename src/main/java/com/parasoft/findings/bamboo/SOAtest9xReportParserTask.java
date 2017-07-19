package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.test.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.*;

import javax.inject.*;

public class SOAtest9xReportParserTask extends AbstractReportParserTask {
    private static final String SOATEST_XSL = "soatest-xunit.xsl"; //$NON-NLS-1$

    @Inject
    public SOAtest9xReportParserTask(@ComponentImport TestCollationService testCollationService) {
        super(testCollationService, SOATEST_XSL);
    }

}
