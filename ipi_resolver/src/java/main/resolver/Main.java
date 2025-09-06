package java.main.resolver;

import java.main.resolver.services.XmlServiceImpl;

public class Main {
    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.out.println("Forneça o caminho físico dos arquivos XML.");
//            System.out.println("Ex: ./programa.exe C:\\personal\\ipi_resolver");
//        }

        try {
            var service = new XmlServiceImpl();
            service.doOperation("C:\\Users\\Breno\\Desktop\\xmls");
            //service.doOperation(args[0]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}