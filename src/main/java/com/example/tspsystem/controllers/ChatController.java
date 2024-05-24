package com.example.tspsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.tspsystem.model.ChatGroup;
import javafx.stage.Stage;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.sound.sampled.*;

public class ChatController {

    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 1024;
    private static final int NUM_MFCC_COEFFICIENTS = 13;
    private static List<String> words = new ArrayList<>();
    private static MultiLayerNetwork model;

    @FXML
    private TextArea messageInput;

    @FXML
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    private ListView<String> messageDisplay;

    @FXML
    private Button sendButton;

    @FXML
    private Button recognizeWord; // Dodane pole dla przycisku

    @FXML
    private ChatGroup currentGroup; // Zmienna przechowująca aktualną grupę czatu

    // Zmienne do nagrywania dźwięku
    private ByteArrayOutputStream currentRecording;
    private TargetDataLine line;
    private Thread recordingThread;
    private AtomicBoolean isRecording = new AtomicBoolean(false);

    @FXML
    private void initialize() {
        // Ładowanie etykiet z pliku
        loadLabels("labels.txt");

        // Wczytanie modelu z pliku
        loadModel("speech_model.zip");

        // Możesz zaimplementować logikę inicjalizacji ogólnej, jeśli potrzebna
        recognizeWord.setOnAction(event -> {
            try {
                if (isRecording.get()) {
                    stopRecordingAndRecognize();
                } else {
                    startRecording();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadLabels(String filePath) {
        try {
            words = Files.readAllLines(Paths.get(filePath));
            System.out.println("Labels loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load labels.");
        }
    }

    private void loadModel(String modelFilePath) {
        File modelFile = new File(modelFilePath);
        if (!modelFile.exists()) {
            System.out.println("Model file not found.");
            return;
        }
        try {
            model = ModelSerializer.restoreMultiLayerNetwork(modelFile);
            System.out.println("Model loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load model: " + e.getMessage());
        }
    }

    private void startRecording() throws Exception {
        System.out.println("Naciśnij przycisk ponownie, aby zakończyć nagrywanie.");

        currentRecording = new ByteArrayOutputStream();
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            return;
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        isRecording.set(true);
        recordingThread = new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            int numBytesRead;
            while (isRecording.get()) {
                numBytesRead = line.read(buffer, 0, buffer.length);
                if (numBytesRead > 0) {
                    currentRecording.write(buffer, 0, numBytesRead);
                }
            }
        });
        recordingThread.start();
        System.out.println("Recording started.");
    }

    private void stopRecordingAndRecognize() throws Exception {
        System.out.println("Recording stopped. Processing...");

        isRecording.set(false);
        recordingThread.join();
        line.stop();
        line.close();

        byte[] audioData = currentRecording.toByteArray();
        if (audioData.length == 0) {
            System.out.println("No audio data recorded.");
            return;
        }
        System.out.println("Audio data length: " + audioData.length);

        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize());
        double[] mfccFeatures = processAudio(audioInputStream, NUM_MFCC_COEFFICIENTS, SAMPLE_RATE);
        System.out.println("MFCC Features: " + Arrays.toString(mfccFeatures));

        if (model == null) {
            System.out.println("Model not loaded.");
            return;
        }

        INDArray inputFeatures = Nd4j.create(mfccFeatures).reshape(1, mfccFeatures.length);
        INDArray output = model.output(inputFeatures);
        System.out.println("Model output: " + output);

        double maxPredictionValue = output.getDouble(0, Nd4j.argMax(output, 1).getInt(0));
        System.out.println("Maximum prediction confidence: " + maxPredictionValue);

        String recognizedWord = null;

        // Log probability for each word
        for (int i = 0; i < words.size(); i++) {
            double probability = output.getDouble(0, i) * 100;
            System.out.printf("Probability of '%s': %.2f%%\n", words.get(i), probability);
        }

        if (maxPredictionValue < 0.6) {  // Ustawienie progu pewności
            System.out.println("Recognition confidence is too low.");
        } else {
            int predictedIndex = Nd4j.argMax(output, 1).getInt(0);
            if (predictedIndex < words.size()) {
                recognizedWord = words.get(predictedIndex);
                System.out.println("Recognized word: " + recognizedWord);
            } else {
                System.out.println("Predicted index out of bounds: " + predictedIndex);
            }
        }

        System.out.println("Recording processed.");

        // Dopisanie rozpoznanego słowa do pola messageInput
        if (recognizedWord != null) {
            String currentText = messageInput.getText();
            messageInput.setText(currentText + recognizedWord + " ");
        }
    }


    // Metoda do przetwarzania nagranego dźwięku
    private static double[] processAudio(AudioInputStream audioInputStream, int numCoefficients, int sampleRate) {
        double[] mfcc = new double[0];

        try {
            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            byte[] audioData = out.toByteArray();
            double[] samples = convertBytesToDoubleSamples(audioData); // Konwersja bajtów na próbki

            samples = trimSilenceFromEdges(samples, 0.0001);
            samples = Arrays.copyOf(samples, nextPowerOfTwo(samples.length));

            Complex[] fftResult = transformer.transform(samples, TransformType.FORWARD);
            mfcc = calculateMFCC(fftResult, sampleRate, numCoefficients);

            System.out.println("Nagranie: " + Arrays.toString(mfcc));

        } catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        return mfcc;
    }

    private static int nextPowerOfTwo(int number) {
        int result = 1;
        while (result < number) {
            result <<= 1; // Podwaja wartość result
        }
        return result;
    }

    private static double[] trimSilenceFromEdges(double[] samples, double threshold) {
        int startIndex = 0;
        int endIndex = samples.length - 1;

        for (int i = 0; i < samples.length; i++) {
            if (Math.abs(samples[i]) > threshold) {
                startIndex = i;
                break;
            }
        }

        for (int i = samples.length - 1; i >= 0; i--) {
            if (Math.abs(samples[i]) > threshold) {
                endIndex = i;
                break;
            }
        }

        return Arrays.copyOfRange(samples, startIndex, endIndex + 1);
    }

    // Metoda do obliczania MFCC (Mel-Frequency Cepstral Coefficients)
    private static double[] calculateMFCC(Complex[] fftResult, int sampleRate, int numCoefficients) {
        final int NUM_MEL_FILTERS = 40; // Liczba filtrów melowskich
        final int NUM_CEPSTRAL_COEFFICIENTS = numCoefficients; // Liczba współczynników cepstralnych
        double[] mfcc = new double[NUM_CEPSTRAL_COEFFICIENTS];

        // Obliczanie mocy spektrum
        double[] powerSpectrum = new double[fftResult.length / 2];
        for (int i = 0; i < fftResult.length / 2; i++) {
            powerSpectrum[i] = Math.pow(fftResult[i].abs(), 2);
        }

        // Obliczanie filtrów melowskich
        double[][] melFilters = createMelFilters(sampleRate, fftResult.length / 2, NUM_MEL_FILTERS);

        // Filtracja pasmowa melowska
        double[] melEnergies = new double[NUM_MEL_FILTERS];
        for (int i = 0; i < NUM_MEL_FILTERS; i++) {
            for (int j = 0; j < powerSpectrum.length; j++) {
                melEnergies[i] += melFilters[i][j] * powerSpectrum[j];
            }
        }

        // Obliczanie logarytmu energii
        for (int i = 0; i < NUM_MEL_FILTERS; i++) {
            melEnergies[i] = Math.log(melEnergies[i]);
        }

        // Wykonanie dyskretnej transformaty kosinusowej (DCT)
        for (int i = 0; i < NUM_CEPSTRAL_COEFFICIENTS; i++) {
            double sum = 0;
            for (int j = 0; j < NUM_MEL_FILTERS; j++) {
                sum += melEnergies[j] * Math.cos(Math.PI * i / NUM_MEL_FILTERS * (j + 0.5));
            }
            mfcc[i] = sum;
        }

        return mfcc;
    }

    // Metoda do tworzenia filtrów melowskich
    private static double[][] createMelFilters(int sampleRate, int fftSize, int numFilters) {
        final double fMin = 0; // Minimalna częstotliwość
        final double fMax = sampleRate / 2; // Maksymalna częstotliwość
        final double melMin = 2595 * Math.log10(1 + fMin / 700); // Minimalna częstotliwość melowska
        final double melMax = 2595 * Math.log10(1 + fMax / 700); // Maksymalna częstotliwość melowska
        double[] melPoints = new double[numFilters + 2];
        for (int i = 0; i < melPoints.length; i++) {
            melPoints[i] = melMin + (melMax - melMin) / (numFilters + 1) * i;
        }
        double[][] filters = new double[numFilters][fftSize];
        for (int i = 1; i <= numFilters; i++) {
            for (int j = 0; j < fftSize; j++) {
                double f = j * sampleRate / fftSize;
                if (f < fMin || f > fMax) {
                    filters[i - 1][j] = 0;
                } else {
                    double mel = 2595 * Math.log10(1 + f / 700);
                    if (mel <= melPoints[i - 1]) {
                        filters[i - 1][j] = 0;
                    } else if (mel < melPoints[i]) {
                        filters[i - 1][j] = (mel - melPoints[i - 1]) / (melPoints[i] - melPoints[i - 1]);
                    } else if (mel < melPoints[i + 1]) {
                        filters[i - 1][j] = (melPoints[i + 1] - mel) / (melPoints[i + 1] - melPoints[i]);
                    } else {
                        filters[i - 1][j] = 0;
                    }
                }
            }
        }
        return filters;
    }

    private static double[] oneHotEncode(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        double[] oneHotEncoded = new double[size];
        oneHotEncoded[index] = 1.0;
        return oneHotEncoded;
    }

    private static byte[] convertDoubleSamplesToBytes(double[] samples) {
        byte[] byteData = new byte[samples.length * 2];  // każda próbka będzie miała 2 bajty (16-bitowe wartości)
        for (int i = 0; i < samples.length; i++) {
            int sampleAsInt = (int) (samples[i] * 32767.0);  // przeskalowanie do zakresu 16-bitowego int
            byteData[i * 2] = (byte) (sampleAsInt & 0xFF);  // młodszy bajt
            byteData[i * 2 + 1] = (byte) ((sampleAsInt >> 8) & 0xFF);  // starszy bajt
        }
        return byteData;
    }

    // Metoda do konwersji bajtów na próbki typu double
    private static double[] convertBytesToDoubleSamples(byte[] audioBytes) {
        double[] samples = new double[audioBytes.length / 2];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (audioBytes[2 * i] | (audioBytes[2 * i + 1] << 8)) / 32768.0;
        }
        return samples;
    }

    @FXML
    public void initializeWithGroup(ChatGroup group) {
        this.currentGroup = group;
        updateChatTitle();
        loadChatHistory();
    }

    @FXML
    private void updateChatTitle() {
        // Możesz zaktualizować tytuł czatu na pasku aplikacji, jeśli jest taka potrzeba
        if (messageInput != null) {
            messageInput.setPromptText("Napisz wiadomość do: " + currentGroup.getName());
        }
    }

    @FXML
    private void loadChatHistory() {
        // Tutaj możesz dodać logikę do ładowania historii czatu dla grupy
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/home.fxml"));
            Parent homeView = loader.load();
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.getScene().setRoot(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            sendToServer(message);
            messageDisplay.getItems().add(message); // Można dodać użytkownika i czas do wyświetlanej wiadomości
            messageInput.clear();
        }
    }

    private void sendToServer(String message) {
        try {
            String jsonPayload = "{\"groupId\": " + currentGroup.getId() + ", \"message\":\"" + message.replace("\"", "\\\"") + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/chat_messages")) // Upewnij się, że to jest właściwy endpoint
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        System.out.println("Odpowiedź serwera: " + responseBody);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

