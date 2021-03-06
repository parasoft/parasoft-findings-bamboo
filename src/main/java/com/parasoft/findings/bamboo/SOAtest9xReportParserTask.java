/*
 * Copyright 2017 Parasoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
