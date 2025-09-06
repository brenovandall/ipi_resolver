package com.resolver.services.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileLoggerImpl implements FileLogger {
    private final List<String> logBuffer = new ArrayList<>();
    private final String logPath;

    public FileLoggerImpl(String logPath) {
        validateOrCreatePath(logPath);
        this.logPath = logPath;
    }

    @Override
    public void logInformation(String info) {
        var date = returnFormattedDateTime(LocalDateTime.now(), "dd/MM/yyyy HH:mm:ss");
        logBuffer.add(date + " - " + info);
    }

    @Override
    public void flush() {
        var logFile = getLogFile();

        try {
            var fw = new FileWriter(logFile, true);
            var bw = new BufferedWriter(fw);

            for (var log : logBuffer) {
                bw.write(log);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            logBuffer.clear();
        } catch (IOException e) {
            throw new RuntimeException("Um erro ocorreu na tentativa de escrever no arquivo de logs: " + e.getMessage());
        }
    }

    private File getLogFile() {
        var localTime = LocalDateTime.now();
        var year = returnFormattedDateTime(localTime, "yyyy");
        var month = returnFormattedDateTime(localTime, "MM");
        var fileName = returnFormattedDateTime(localTime, "dd-MM-yyyy") + ".txt";

        File yearDir = new File(logPath, year);
        File monthDir = new File(yearDir, month);

        if (!monthDir.exists() && !monthDir.mkdirs()) {
            throw new RuntimeException("Não foi possível criar diretórios: " + monthDir.getAbsolutePath());
        }

        File logFile = new File(monthDir, fileName);

        try {
            if (!logFile.exists() && !logFile.createNewFile()) {
                throw new RuntimeException("Não foi possível criar o arquivo: " + logFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar arquivo de log: " + logFile.getAbsolutePath(), e);
        }

        return logFile;
    }

    private static String returnFormattedDateTime(LocalDateTime localTime, String format) {
        var formatter = DateTimeFormatter.ofPattern(format);
        return localTime.format(formatter);
    }

    private void validateOrCreatePath(String path) {
        var file = new File(path);

        if (!file.exists()) {
            var dirs = file.mkdirs();
            if (!dirs) {
                throw new RuntimeException("O caminho fornecido não existe, ao tentar criá-lo, ocorreu um erro. " +
                        "Verifique se a aplicação está rodando em modo administrador.");
            }
        } else if (!file.isDirectory()) {
            throw new IllegalArgumentException(
                    "Para inicializar o Logger da aplicação, o caminho fornecido deve ser um diretório.");
        }
    }
}
