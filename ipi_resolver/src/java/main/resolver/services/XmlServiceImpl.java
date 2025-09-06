package java.main.resolver.services;

import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlServiceImpl implements XmlService {
    @Override
    public void doOperation(String filePath) throws Exception {
        var directory = getDirectory(filePath);
        var files = getFilesByDirectory(directory);

        for (var file : files) {
            readXmlFile(file);
        }
    }

    private void readXmlFile(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();

        var parsedDocument = builder.parse(xmlFile);
        var items = parsedDocument.getElementsByTagName("det");

        for (int i = 0; i < items.getLength(); i++) {
            var det = items.item(i);
            // working on
        }
    }

    private File[] getFilesByDirectory(File directory) {
        var files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (files == null) {
            return new File[0];
        }

        return files;
    }

    private File getDirectory(String filePath) throws Exception {
        var directory = new File(filePath);

        if (!directory.isDirectory()) {
            throw new Exception("O caminho fornecido não é um diretório.");
        }

        return directory;
    }
}
