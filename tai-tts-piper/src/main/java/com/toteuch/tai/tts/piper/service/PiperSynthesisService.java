package com.toteuch.tai.tts.piper.service;

import com.toteuch.tai.tts.piper.config.TtsPiperProperties;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PiperSynthesisService {
    private static final Logger log = LoggerFactory.getLogger(PiperSynthesisService.class);

    private final TtsPiperProperties properties;

    public PiperSynthesisService(TtsPiperProperties properties) {
        this.properties = properties;
    }

    public Path synthesize(String correlationId, String text) {
        try {
            Path outputDir = Paths.get(properties.getPiper().getOutputDir());
            Files.createDirectories(outputDir);

            Path outputFile = outputDir.resolve("tts_" + correlationId + ".wav");

            List<String> command =
                    List.of(
                            properties.getPiper().getExecutable(),
                            "--model",
                            properties.getPiper().getModel(),
                            "--config",
                            properties.getPiper().getConfig(),
                            "--output_file",
                            outputFile.toString());

            log.info(
                    "Starting Piper process | correlationId={} command={}", correlationId, command);

            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

            try (BufferedWriter writer =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(text);
                writer.newLine();
            }

            boolean completed =
                    process.waitFor(
                            properties.getPiper().getProcessTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new IllegalStateException("Piper process timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IllegalStateException("Piper process failed with exitCode=" + exitCode);
            }

            if (!Files.exists(outputFile) || Files.size(outputFile) == 0) {
                throw new IllegalStateException(
                        "Piper output file missing or empty: " + outputFile);
            }

            return outputFile;
        } catch (Exception e) {
            throw new IllegalStateException("Piper synthesis failed", e);
        }
    }

    public boolean isPiperConfigured() {
        return Files.exists(Paths.get(properties.getPiper().getExecutable()))
                && Files.exists(Paths.get(properties.getPiper().getModel()))
                && Files.exists(Paths.get(properties.getPiper().getConfig()));
    }
}
