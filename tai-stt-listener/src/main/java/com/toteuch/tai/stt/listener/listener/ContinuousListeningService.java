package com.toteuch.tai.stt.listener.listener;

import com.toteuch.tai.stt.listener.audio.SpeechSegment;
import com.toteuch.tai.stt.listener.config.SttListenerProperties;
import com.toteuch.tai.stt.listener.pipeline.SttPipelineResult;
import com.toteuch.tai.stt.listener.pipeline.SttPipelineService;
import com.toteuch.tai.stt.listener.pipeline.SttPipelineSummary;
import com.toteuch.tai.stt.listener.transport.OrchestratorSttEventClient;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.TargetDataLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ContinuousListeningService {
    private static final Logger log = LoggerFactory.getLogger(ContinuousListeningService.class);

    private final SpeechSegmentRecorder segmentRecorder;
    private final SttPipelineService pipelineService;
    private final OrchestratorSttEventClient eventClient;
    private final SttListenerProperties properties;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread thread = new Thread(r, "tai-stt-continuous-listener");
                        thread.setDaemon(false);
                        return thread;
                    });

    private final ExecutorService callbackExecutor =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread thread = new Thread(r, "tai-stt-speech-started-callback");
                        thread.setDaemon(true);
                        return thread;
                    });

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<ListeningState> state =
            new AtomicReference<>(ListeningState.STOPPED);
    private final AtomicReference<String> activeCorrelationId = new AtomicReference<>();
    private final AtomicReference<Instant> lastSegmentAt = new AtomicReference<>();
    private final AtomicReference<SttPipelineSummary> lastResult = new AtomicReference<>();
    private final AtomicReference<String> lastError = new AtomicReference<>();

    private volatile Future<?> loopFuture;
    private volatile TargetDataLine activeMicrophone;

    public ContinuousListeningService(
            SpeechSegmentRecorder segmentRecorder,
            SttPipelineService pipelineService,
            OrchestratorSttEventClient eventClient,
            SttListenerProperties properties) {
        this.segmentRecorder = segmentRecorder;
        this.pipelineService = pipelineService;
        this.eventClient = eventClient;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void autoStartOnReady() {
        if (properties.getListener().isAutoStart()) {
            start();
        }
    }

    public ListeningRuntimeStatus start() {
        if (!running.compareAndSet(false, true)) {
            log.info("Continuous listener already running");
            return status();
        }

        lastError.set(null);
        state.set(ListeningState.STARTING);

        loopFuture = executorService.submit(this::runLoop);

        log.info("Continuous listener start requested");

        return status();
    }

    public ListeningRuntimeStatus stop() {
        if (!running.getAndSet(false)) {
            log.info("Continuous listener already stopped");
            state.compareAndSet(ListeningState.ERROR, ListeningState.STOPPED);
            return status();
        }

        log.info("Continuous listener stop requested");

        closeActiveMicrophone();

        return status();
    }

    public ListeningRuntimeStatus status() {
        return new ListeningRuntimeStatus(
                running.get(),
                state.get(),
                activeCorrelationId.get(),
                lastSegmentAt.get(),
                lastResult.get(),
                lastError.get());
    }

    private void runLoop() {
        TargetDataLine microphone = null;

        try {
            microphone = segmentRecorder.openMicrophoneLine();
            activeMicrophone = microphone;

            while (running.get()) {
                state.set(ListeningState.WAITING_FOR_SPEECH);

                AtomicReference<String> segmentCorrelationId = new AtomicReference<>();

                SpeechSegment segment =
                        segmentRecorder.recordNextSegment(
                                microphone,
                                running::get,
                                signal -> {
                                    String correlationId = UUID.randomUUID().toString();

                                    segmentCorrelationId.set(correlationId);
                                    activeCorrelationId.set(correlationId);
                                    state.set(ListeningState.CAPTURING);

                                    publishSpeechStartedAsync(correlationId, signal);
                                });

                if (!running.get() || segment == null) {
                    activeCorrelationId.set(null);
                    continue;
                }

                String correlationId = segmentCorrelationId.get();

                if (correlationId == null || correlationId.isBlank()) {
                    correlationId = UUID.randomUUID().toString();
                    activeCorrelationId.set(correlationId);
                }

                processSegment(segment, correlationId);
            }

            state.set(ListeningState.STOPPED);
        } catch (Exception e) {
            lastError.set(e.getMessage());
            state.set(ListeningState.ERROR);
            running.set(false);

            log.warn("Continuous listener failed", e);
        } finally {
            closeMicrophone(microphone);
            activeMicrophone = null;
            activeCorrelationId.set(null);

            if (state.get() != ListeningState.ERROR) {
                state.set(ListeningState.STOPPED);
            }

            running.set(false);

            log.info("Continuous listener stopped");
        }
    }

    private void processSegment(SpeechSegment segment, String correlationId) {
        activeCorrelationId.set(correlationId);
        lastSegmentAt.set(Instant.now());
        state.set(ListeningState.PROCESSING);

        try {
            SttPipelineResult result = pipelineService.process(segment, correlationId);
            lastResult.set(SttPipelineSummary.from(result));
            lastError.set(null);

            log.info(
                    "Continuous STT segment processed | correlationId={} accepted={} reason={}",
                    correlationId,
                    result.finalDecision().accepted(),
                    result.finalDecision().reason());

            if (properties.getListener().isPublishFinalCallbacks()) {
                eventClient.sendCallback(
                        result.correlationId(),
                        result.segment(),
                        result.transcription(),
                        result.finalDecision());
            }
        } catch (Exception e) {
            lastError.set(e.getMessage());
            log.warn(
                    "Continuous STT segment processing failed | correlationId={}",
                    correlationId,
                    e);

            if (!properties.getListener().isContinueOnError()) {
                running.set(false);
                state.set(ListeningState.ERROR);
            }
        } finally {
            activeCorrelationId.set(null);
        }
    }

    private void closeActiveMicrophone() {
        closeMicrophone(activeMicrophone);
        activeMicrophone = null;
    }

    private void closeMicrophone(TargetDataLine microphone) {
        if (microphone == null) {
            return;
        }

        try {
            microphone.stop();
        } catch (Exception ignored) {
        }

        try {
            microphone.flush();
        } catch (Exception ignored) {
        }

        try {
            microphone.close();
        } catch (Exception ignored) {
        }
    }

    private void publishSpeechStartedAsync(String correlationId, SpeechStartedSignal signal) {
        if (!properties.getListener().isPublishSpeechStartedCallbacks()) {
            return;
        }

        callbackExecutor.submit(
                () -> {
                    try {
                        eventClient.sendSpeechStarted(
                                correlationId, signal.averageEnergy(), signal.peakEnergy());

                        log.info(
                                "Speech started callback sent | correlationId={} averageEnergy={} peakEnergy={}",
                                correlationId,
                                signal.averageEnergy(),
                                signal.peakEnergy());
                    } catch (Exception e) {
                        log.warn(
                                "Failed to publish speech started callback | correlationId={}",
                                correlationId,
                                e);
                    }
                });
    }

    @PreDestroy
    public void shutdown() {
        stop();
        executorService.shutdownNow();
        callbackExecutor.shutdown();
    }
}
