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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stax.StAXSource;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.Xslt30Transformer;

import org.apache.log4j.Logger;
import org.fusesource.hawtbuf.ByteArrayInputStream;

import com.atlassian.bamboo.build.test.TestCollectionResult;
import com.atlassian.bamboo.build.test.TestCollectionResultBuilder;
import com.atlassian.bamboo.build.test.TestReportCollector;
import com.atlassian.bamboo.build.test.junit.JunitTestResultsParser;

public class ReportCollector implements TestReportCollector {
    private static final Logger log = Logger.getLogger(ReportCollector.class);
    private static final XsltErrorListener xsltErrorListener = new XsltErrorListener();
    private static final XMLInputFactory xmlInputFactory = createXmlInputFactory();

    private static XMLInputFactory createXmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }

    public ReportCollector() {
    }

    @Override
    public TestCollectionResult collect(File file) throws Exception {
        ReportType reportType = getReportType(file);
        if (reportType == null) {
            log.info("Skipping non-Parasoft report: " + file.getAbsolutePath()); //$NON-NLS-1$
            return new TestCollectionResultBuilder().build();
        }
        try (InputStream stream = getInputStream(file, reportType.getXsl())) {
            JunitTestResultsParser parser = new JunitTestResultsParser();
            parser.parse(stream);
            return new TestCollectionResultBuilder()
                    .addSuccessfulTestResults(parser.getSuccessfulTests())
                    .addFailedTestResults(parser.getFailedTests())
                    .addSkippedTestResults(parser.getSkippedTests()).build();
        }
    }

    private InputStream getInputStream(File file, String xslFile)
            throws SaxonApiException, IOException, XMLStreamException {
        try (InputStream input = new FileInputStream(file);
                InputStream xslInput = getClass().getResourceAsStream(xslFile)) {
            XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(input);
            try {
                StreamSource xsl = new StreamSource(xslInput);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Processor processor = new Processor(false);
                XsltCompiler compiler = processor.newXsltCompiler();
                XsltExecutable executable = compiler.compile(xsl);
                Serializer target = processor.newSerializer(baos);
                Xslt30Transformer transformer = executable.load30();
                transformer.setErrorListener(xsltErrorListener);
                transformer.transform(new StAXSource(xmlReader), target);
                return new ByteArrayInputStream(baos.toByteArray());
            } finally {
                xmlReader.close();
            }
        }
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return new HashSet<>(Collections.singleton("xml")); //$NON-NLS-1$
    }

    private ReportType getReportType(File from) throws XMLStreamException, IOException {
        XMLEventReader reader = null;
        try (InputStream input = new FileInputStream(from)) {
            reader = xmlInputFactory.createXMLEventReader(input);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    String toolName = null;
                    StartElement startElement = event.asStartElement();
                    @SuppressWarnings("unchecked")
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        String name = attribute.getName().getLocalPart();
                        if ("toolName".equals(name)) { //$NON-NLS-1$
                            toolName = attribute.getValue();
                            break;
                        }
                    }
                    if (toolName != null) {
                        return "SOAtest".equals(toolName) ? ReportType.SOATEST : ReportType.ANALYZERS; //$NON-NLS-1$
                    }
                    return null;
                default:
                    break;
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    log.warn(e);
                }
            }
        }
        return null;
    }

    private static class XsltErrorListener implements ErrorListener {

        @Override
        public void warning(TransformerException exception)
                throws TransformerException {
            log.warn(exception);
        }

        @Override
        public void error(TransformerException exception)
                throws TransformerException {
            log.error(exception);
        }

        @Override
        public void fatalError(TransformerException exception)
                throws TransformerException {
            log.fatal(exception);
        }
    }

    private enum ReportType {
        SOATEST("soatest-xunit.xsl"), //$NON-NLS-1$
        ANALYZERS("xunit.xsl"); //$NON-NLS-1$

        private final String xsl;

        ReportType(String xsl) {
            this.xsl = xsl;
        }

        String getXsl() {
            return xsl;
        }
    }
}
