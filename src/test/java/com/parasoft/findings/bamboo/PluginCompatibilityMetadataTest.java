package com.parasoft.findings.bamboo;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PluginCompatibilityMetadataTest {
    @Test
    public void testDescriptorRetainsCompatibilityMetadata() throws Exception {
        Path descriptor = Paths.get("src/main/resources/atlassian-plugin.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document document;
        try (InputStream input = Files.newInputStream(descriptor)) {
            document = factory.newDocumentBuilder().parse(input);
        }

        Element plugin = document.getDocumentElement();
        assertEquals("${atlassian.plugin.key}", plugin.getAttribute("key"));
        assertEquals("${project.name}", plugin.getAttribute("name"));

        NodeList pluginInfoNodes = plugin.getElementsByTagName("plugin-info");
        assertEquals(1, pluginInfoNodes.getLength());
        Element pluginInfo = (Element) pluginInfoNodes.item(0);
        NodeList params = pluginInfo.getElementsByTagName("param");
        assertEquals(1, paramCount(params, "atlassian-data-center-status"));
        assertEquals(1, paramCount(params, "atlassian-data-center-compatible"));
        assertEquals("compatible", paramValue(params, "atlassian-data-center-status"));
        assertEquals("true", paramValue(params, "atlassian-data-center-compatible"));
        assertEquals("images/pluginIcon.png", paramValue(params, "plugin-icon"));
    }

    private String paramValue(NodeList params, String name) {
        for (int i = 0; i < params.getLength(); i++) {
            Element param = (Element) params.item(i);
            if (name.equals(param.getAttribute("name"))) {
                return param.getTextContent();
            }
        }
        return null;
    }

    private int paramCount(NodeList params, String name) {
        int count = 0;
        for (int i = 0; i < params.getLength(); i++) {
            Element param = (Element) params.item(i);
            if (name.equals(param.getAttribute("name"))) {
                count++;
            }
        }
        return count;
    }
}
