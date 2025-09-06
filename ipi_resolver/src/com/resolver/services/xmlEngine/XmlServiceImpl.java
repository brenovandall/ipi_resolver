package com.resolver.services.xmlEngine;

import com.resolver.services.logger.FileLoggerImpl;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XmlServiceImpl implements XmlService {
    private static final Map<String, Double> tipiTable = new ConcurrentHashMap<>();
    private final FileLoggerImpl logger = new FileLoggerImpl(LOGGER_FILE_PATH);

    private static final String END_LOG_ASTERISKS = "*******************************************************";
    private static final String START_LOG_ASTERISKS = "********************************************* ";
    private static final String NFCE_NAMESPACE = "http://www.portalfiscal.inf.br/nfe";
    private static final String LOGGER_FILE_PATH = "C:\\IpiResolver";
    private static final String COMMA_SEPARATOR = ";";

    @Override
    public void doOperation(String filePath, String tipiTablePath) throws Exception {
        logger.logInformation(START_LOG_ASTERISKS + " Iniciando execução do IPI Resolver © Breno Van Dall " + START_LOG_ASTERISKS);
        warmupTipiTable(tipiTablePath);
        calculateIpi(filePath);
        logger.logInformation(END_LOG_ASTERISKS + " Execução finalizada com sucesso! " + END_LOG_ASTERISKS);
        logger.flush();
    }

    private void calculateIpi(String filePath) throws Exception {
        logger.logInformation("");
        logger.logInformation("*********************************************************************************************************************");
        logger.logInformation("********************************************* Iniciando cálculo de IPI **********************************************");
        logger.logInformation("Todos os XMLs do diretório " + filePath + " serão analisados.");

        var directory = getDirectory(filePath);
        var files = getFilesByDirectory(directory);
        double total = 0, xmlTotal;

        for (var file : files) {
            xmlTotal = 0;

            try {
                xmlTotal = readXmlFileAndGetIpiAliquot(file);
            } catch (Exception e) {
                logger.logInformation("Ocorreu um erro inesperado: " + e.getMessage());
            }
            total += xmlTotal;
        }

        logger.logInformation("************************************** Cálculo de IPI finalizado com sucesso! ***************************************");
        logger.logInformation("*********************************************************************************************************************");
        logger.logInformation("");
        logger.logInformation("VALOR TOTAL CALCULADO (SEM ARREDONDAMENTO): " + total);
    }

    private double readXmlFileAndGetIpiAliquot(File xmlFile) throws ParserConfigurationException, IOException, SAXException {

        logger.logInformation("Iniciando leitura do XML " + xmlFile.getName());

        var factory = getDocumentBuilderFactory();
        var builder = factory.newDocumentBuilder();

        var document = builder.parse(xmlFile);
        var items = document.getElementsByTagNameNS(NFCE_NAMESPACE, "det");

        double result = 0;

        if (items.getLength() == 0) {
            logger.logInformation("NFCe não possui itens.");
        }

        for (int i = 0; i < items.getLength(); i++) {
            logger.logInformation("*** Lendo item " + (i + 1) + " da NFCe.");

            try {
                Node det = items.item(i);
                Node prod = det.getFirstChild();
                Node firstProdSibling = prod.getFirstChild();

                var ncm = getSiblingNodeByName(firstProdSibling, "NCM").getTextContent();
                var vProdNode = getSiblingNodeByName(firstProdSibling, "vProd").getTextContent();
                if (ncm == null || vProdNode == null) {
                    throw new Exception("As tags NCM e/ou vProd não foram econtradas.");
                }

                var vProd = Double.parseDouble(vProdNode);

                var ipiAliquot = tipiTable.get(ncm);
                if (ipiAliquot == null) {
                    throw new Exception("Item ignorado, NCM não possui alíquota para cálculo.");
                }

                var ipiAliquotFactor = ipiAliquot / 100.0;
                var totalItem = vProd * ipiAliquotFactor;

                result += totalItem;

                logger.logInformation("       * NCM: " + ncm);
                logger.logInformation("       * Valor: " + vProd);
                logger.logInformation("       * Cálculo: (" + vProd + " * " + ipiAliquot + "%) = " + totalItem);
            } catch (Exception e) {
                logger.logInformation("****** Erro na leitura do item " + (i + 1) + ", a execução irá continuar. Erro: " + e.getMessage());
            }
        }

        logger.logInformation("Leitura do XML " + xmlFile.getName() + " finalizada.");

        return result;
    }

    private Node getSiblingNodeByName(Node firstSibling, String sibToFind) {
        var temp = firstSibling;

        while (!temp.getNodeName().equals(sibToFind)) {
            temp = temp.getNextSibling();
        }

        return temp;
    }

    private void warmupTipiTable(String csvTipiFile) {
        String line;

        logger.logInformation("");
        logger.logInformation("***********************************************************");
        logger.logInformation("************* Iniciando warmup da tabela TIPI *************");

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(csvTipiFile);
        } catch (FileNotFoundException e) {
            logger.logInformation("Ocorreu um erro ao inicializar o FileReader, a tabela TIPI não será carregada na memória.");
            logger.logInformation("StackTrace: " + e.getMessage());
            return;
        }
        var br = new BufferedReader(fileReader);
        int counter = 0;

        while (true) {
            try {
                if ((line = br.readLine()) == null) break;
            } catch (IOException e) {
                logger.logInformation("Ocorreu um erro inesperado ao ler a linha " + counter + ": " + e.getMessage());
                continue;
            }
            String[] values = line.split(COMMA_SEPARATOR);
            if (values.length < 2) {
                logger.logInformation("Linha " + counter + " ignorada, não foram fornecidos todas as colunas necessárias.");
                break;
            }
            tipiTable.put(values[0], Double.parseDouble(values[1]));
            counter++;
        }

        logger.logInformation("****** Warmup da tabela TIPI finalizado com sucesso! ******");
        logger.logInformation("***********************************************************");
        logger.logInformation("");
    }

    private DocumentBuilderFactory getDocumentBuilderFactory() {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        return factory;
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
            throw new Exception("O caminho fornecido para leitura dos arquivos XML não é um diretório.");
        }

        return directory;
    }
}
