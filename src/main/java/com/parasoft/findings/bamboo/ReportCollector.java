package com.parasoft.findings.bamboo;

import com.atlassian.bamboo.build.test.*;
import com.atlassian.bamboo.build.test.junit.*;
import com.google.common.collect.*;

import java.io.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.apache.commons.io.*;
import org.apache.log4j.*;
import org.fusesource.hawtbuf.ByteArrayInputStream;


public class ReportCollector implements TestReportCollector {
    private static final Logger log = Logger.getLogger(ReportCollector.class);
    private static final XsltErrorListener xsltErrorListener = new XsltErrorListener();
    private static final TransformerFactory tFactory = TransformerFactory.newInstance();

    private final String xslFile;

    public ReportCollector(String xslFile) {
        this.xslFile = xslFile;
    }

    public TestCollectionResult collect(File file) throws Exception {
        InputStream stream = getInputStream(file);
        try {
            JunitTestResultsParser parser = new JunitTestResultsParser();
            parser.parse(stream);
            return new TestCollectionResultBuilder()
                    .addSuccessfulTestResults(parser.getSuccessfulTests())
                    .addFailedTestResults(parser.getFailedTests())
                    .addSkippedTestResults(parser.getSkippedTests()).build();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private InputStream getInputStream(File file)
            throws FileNotFoundException, TransformerException {
        StreamSource xml = new StreamSource(new FileInputStream(file));
        StreamSource xsl = new StreamSource(getClass().getResourceAsStream(xslFile));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult target = new StreamResult(baos);
        Transformer processor = tFactory.newTransformer(xsl);
        processor.setErrorListener(xsltErrorListener);
        processor.transform(xml, target);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public Set<String> getSupportedFileExtensions() {
        return Sets.newHashSet("xml"); //$NON-NLS-1$
    }

    private static class XsltErrorListener implements ErrorListener {

        public void warning(TransformerException exception)
                throws TransformerException {
            log.warn(exception);
        }

        public void error(TransformerException exception)
                throws TransformerException {
            log.error(exception);
        }

        public void fatalError(TransformerException exception)
                throws TransformerException {
            log.fatal(exception);
        }
    }
}
