package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.test.*;
import com.atlassian.plugin.spring.scanner.annotation.imports.*;

import javax.inject.*;

public class Analyzers10xReportParserTask extends AbstractReportParserTask {
    private static final String XUNIT_XSL = "xunit.xsl"; //$NON-NLS-1$

    @Inject
    public Analyzers10xReportParserTask(@ComponentImport TestCollationService testCollationService) {
        super(testCollationService, XUNIT_XSL);
    }
}
