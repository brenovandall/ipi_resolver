package com.resolver;

import com.resolver.services.xmlEngine.XmlServiceImpl;

public class Main {
    private static final XmlServiceImpl xmlService = new XmlServiceImpl();

    public static void main(String[] args) throws Exception {
        checkArgs(args);
        xmlService.doOperation(args[0], args[1]);
    }

    private static void checkArgs(String[] args) {
        if (args.length < 2) {
            System.out.println("Usabilidade: ipi_resolver [0] [1]");
            System.out.println("   [0]  caminho físico do diretório que contém os arquivos XML");
            System.out.println("   [1]  caminho físico do arquivo .csv da tabela TIPI");
        }
    }
}