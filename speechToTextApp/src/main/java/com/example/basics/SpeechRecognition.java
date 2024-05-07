package com.example.basics;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.*;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class SpeechRecognition {

    // Zmienne klasowe
    private static final int SAMPLE_RATE = 44100;
    private static final int NUM_MFCC_COEFFICIENTS = 13;
    private static final int NUM_RECORDINGS = 100;
    private static List<String> words = new ArrayList<>();
    private static MultiLayerNetwork model;
    private static final String TRAINING_DATA_FILE = "training_data.csv";


    public static void main(String[] args) {
        System.out.println("Starting main method...");
        try (Scanner scanner = new Scanner(System.in)) {
            loadWordsFromTrainingDataFile();

            if (getUserConfirmation(scanner, "Czy chcesz dodać dane treningowe?")) {
                addTrainingData(scanner);
            }

            if (getUserConfirmation(scanner, "Czy chcesz utworzyć model DNN?")) {
                trainAndSaveModel();
            }

            recognizeWordsFromAudio();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean getUserConfirmation(Scanner scanner, String message) {
        System.out.println(message + " (t - tak, n - nie)");
        String response = scanner.nextLine();
        return response.equalsIgnoreCase("t");
    }

    private static void trainModelIfNeeded() throws Exception {
        System.out.println("Checking if model training is needed...");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Czy chcesz utworzyć model DNN? (t - tak, n - nie)");
        String response = scanner.nextLine();

        if (response.equalsIgnoreCase("t")) {
            System.out.println("Training model...");
            MultiLayerNetwork model = trainModel();
            if (model != null) {
                System.out.println("Model trained successfully, evaluating and saving...");
                evaluateAndSaveModel(model, new File("speech_model.zip"));
                System.out.println("Recognizing words from audio...");
                recognizeWordsFromAudio();
            } else {
                System.out.println("Model training failed, program will terminate.");
            }
        } else {
            System.out.println("No model creation chosen, program will terminate.");
        }

        System.out.println("Closing scanner after model decision...");
        scanner.close();
    }

    private static void loadWordsFromTrainingDataFile() {
        File file = new File(TRAINING_DATA_FILE);
        if (!file.exists()) {
            System.out.println("Training data file does not exist.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String word = line.split(",")[0];
                if (!words.contains(word)) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading words from file: " + e.getMessage());
        }
    }

    private static boolean modelNeedsTraining() {
        return words.isEmpty() || model == null;
    }

    private static void trainAndSaveModel() {
        System.out.println("Training model...");
        try {
            model = trainModel();
            if (model != null) {
                System.out.println("Model trained successfully, evaluating and saving...");
                evaluateAndSaveModel(model, new File("speech_model.zip"));
            } else {
                System.out.println("Model training failed.");
            }
        } catch (Exception e) {
            System.err.println("Error training model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addTrainingData(Scanner scanner) throws IOException, LineUnavailableException {
        Set<String> uniqueWords = new HashSet<>(words);
        for (String word : uniqueWords) {
            List<double[]> mfccSamples = recordWordSamples(scanner, word);
            for (double[] mfcc : mfccSamples) {
                saveTrainingData(word, mfcc);
            }
        }
    }




    private static void saveTrainingData(String word, double[] mfccFeatures) throws IOException {
        try (FileWriter fw = new FileWriter(TRAINING_DATA_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(word);
            for (double feature : mfccFeatures) {
                out.print("," + feature);
            }
            out.println();
        }
    }


    private static List<DataSet> loadTrainingDataFromFile() {
        List<DataSet> dataSetList = new ArrayList<>();
        File file = new File(TRAINING_DATA_FILE);
        if (!file.exists()) {
            return dataSetList;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String wordLabel = values[0];
                double[] mfccFeatures = Arrays.stream(values, 1, values.length).mapToDouble(Double::parseDouble).toArray();

                int wordIndex = words.indexOf(wordLabel);
                INDArray input = Nd4j.create(new double[][]{mfccFeatures});
                INDArray label = Nd4j.zeros(1, words.size());
                label.putScalar(0, wordIndex, 1);
                dataSetList.add(new DataSet(input, label));
            }
        } catch (IOException e) {
            System.err.println("Error reading training data file.");
            e.printStackTrace();
        }
        return dataSetList;
    }




    private static List<double[]> recordWordSamples(Scanner scanner, String word) throws IOException, LineUnavailableException {
        List<double[]> mfccSamples = new ArrayList<>();
        System.out.println("Nagrywanie próbek dla słowa: " + word);
        for (int i = 0; i < NUM_RECORDINGS; i++) {
            System.out.println("Naciśnij Enter, aby rozpocząć nagrywanie (" + (i + 1) + "/" + NUM_RECORDINGS + ")");
            scanner.nextLine();
            byte[] audioData = recordAudio(SAMPLE_RATE);
            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), new AudioFormat(SAMPLE_RATE, 16, 1, true, true), audioData.length / 2);
            double[] mfccFeatures = processAudio(audioInputStream, NUM_MFCC_COEFFICIENTS, SAMPLE_RATE);
            mfccSamples.add(mfccFeatures);
        }
        return mfccSamples;
    }


    private static MultiLayerNetwork trainModel() throws Exception {
        if (words.isEmpty()) {
            System.out.println("No words loaded. Cannot train model.");
            return null;
        }

        List<DataSet> trainingData = loadTrainingDataFromFile();
        if (trainingData.isEmpty()) {
            System.out.println("No training data available.");
            return null;
        }

        DataSet allData = DataSet.merge(trainingData);
        allData.shuffle();
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.8);

        int numInputs = NUM_MFCC_COEFFICIENTS;
        int numOutputs = words.size();
        int numHiddenNodes = 128;

        MultiLayerNetwork model = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .updater(new Adam())
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.RELU).build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nIn(numHiddenNodes).nOut(numOutputs).activation(Activation.SOFTMAX).build())
                .build());

        model.init();
        model.fit(testAndTrain.getTrain());
        return model;
    }





    private static void trainWord(MultiLayerNetwork model, int wordIndex, List<double[]> mfccSamples) {
        List<DataSet> dataSetList = new ArrayList<>();
        for (double[] mfcc : mfccSamples) {
            INDArray input = Nd4j.create(new double[][]{mfcc}); // Dane wejściowe jako dwuwymiarowa macierz
            INDArray label = Nd4j.create(new double[][]{oneHotEncode(wordIndex, words.size())}); // Etykiety jako dwuwymiarowa macierz
            dataSetList.add(new DataSet(input, label));
        }

        DataSet allData = DataSet.merge(dataSetList);
        allData.shuffle();
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.8); // 80% danych do treningu

        // Trenowanie modelu
        model.fit(testAndTrain.getTrain());
    }

    private static void evaluateAndSaveModel(MultiLayerNetwork model, File file) throws IOException {
        Evaluation eval = new Evaluation(words.size());
        List<DataSet> trainingData = loadTrainingDataFromFile();
        DataSet allData = DataSet.merge(trainingData);
        INDArray output = model.output(allData.getFeatures());
        eval.eval(allData.getLabels(), output);
        System.out.println(eval.stats());
        ModelSerializer.writeModel(model, file, true);
    }

    public static void recognizeWordsFromAudio() throws Exception {
        Terminal terminal = TerminalBuilder.terminal();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        System.out.println("Naciśnij Enter, aby rozpocząć/zakończyć nagrywanie słowa. Program zakończy działanie po 3 sekundach braku aktywności.");

        List<byte[]> audioDataList = new ArrayList<>();
        AtomicBoolean isRecording = new AtomicBoolean(false);
        AtomicBoolean running = new AtomicBoolean(true);
        long lastTimePressed = System.currentTimeMillis();

        ByteArrayOutputStream currentRecording = new ByteArrayOutputStream();
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
        line.open(format);
        line.start();

        Thread recordingThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            int numBytesRead;
            while (!Thread.currentThread().isInterrupted() && running.get()) {
                if (isRecording.get()) {
                    numBytesRead = line.read(buffer, 0, buffer.length);
                    if (numBytesRead > 0) {
                        currentRecording.write(buffer, 0, numBytesRead);
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break; // Przerwanie pętli przy przerwaniu wątku
                }
            }
            line.close();
        });

        recordingThread.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        while (running.get()) {
            if (System.currentTimeMillis() - lastTimePressed > 3000) {
                running.set(false);
                System.out.println("Brak aktywności przez 3 sekundy. Zamykanie programu.");
            }

            Future<String> future = executor.submit(() -> reader.readLine()); // Używając lambda expression
            String input = null;
            try {
                // Ustawienie timeoutu na readLine
                input = future.get(500, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true); // Przerywa blokujące czytanie jeśli przekroczono czas
            } catch (ExecutionException | InterruptedException e) {
                break; // Zakończ pętlę w przypadku błędu
            }

            if (input != null && !input.isEmpty()) {
                lastTimePressed = System.currentTimeMillis(); // Resetowanie timera przy każdej aktywności
                if (isRecording.get()) {
                    isRecording.set(false);
                    audioDataList.add(currentRecording.toByteArray());
                    currentRecording.reset();
                    System.out.println("Słowo zapisane. Rozpocznij nagrywanie kolejnego słowa naciskając Enter.");
                } else {
                    isRecording.set(true);
                    System.out.println("Rozpoczęto nagrywanie. Naciśnij Enter, aby zapisać słowo.");
                }
            }
        }

        executor.shutdownNow();
        recordingThread.interrupt();
        recordingThread.join(); // Czekaj na zakończenie wątku nagrywającego

        // Przetwarzanie zarejestrowanych słów
        for (byte[] audioData : audioDataList) {
            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / 2);
            double[] mfccFeatures = processAudio(audioInputStream, NUM_MFCC_COEFFICIENTS, SAMPLE_RATE);
            float[] mfccFeaturesFloat = new float[mfccFeatures.length];
            for (int i = 0; i < mfccFeatures.length; i++) {
                mfccFeaturesFloat[i] = (float) mfccFeatures[i];
            }
            INDArray inputFeatures = Nd4j.create(mfccFeaturesFloat).reshape(1, mfccFeaturesFloat.length);
            INDArray output = model.output(inputFeatures);
            int predictedIndex = Nd4j.argMax(output, 1).getInt(0);
            System.out.println("Rozpoznane słowo: " + words.get(predictedIndex));
        }
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


    private static MultiLayerNetwork buildModel(int numInputs, int numOutputs, int numHiddenNodes) {
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .updater(new Adam())
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(numHiddenNodes).nOut(numOutputs)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build());
    }


    private static byte[] recordAudio(int sampleRate) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Audio line not supported.");
        }

        byte[] audioData = null;
        try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int numBytesRead;
            while (line.isOpen() && !Thread.currentThread().isInterrupted()) {
                numBytesRead = line.read(data, 0, data.length);
                if (numBytesRead > 0) {
                    out.write(data, 0, numBytesRead);
                }
            }
            audioData = out.toByteArray();
            out.close();
        } catch (IOException e) {
            System.err.println("Error while recording audio: " + e.getMessage());
        }

        double[] samples = convertBytesToDoubleSamples(audioData);
        double[] normalizedFilteredSamples = filterAndNormalize(samples);
        return convertDoubleSamplesToBytes(normalizedFilteredSamples);
    }


    private static double[] filterAndNormalize(double[] samples) {
        double max = Arrays.stream(samples).max().orElse(1.0);
        for (int i = 0; i < samples.length; i++) {
            samples[i] = samples[i] / max; // Normalizacja
            if (Math.abs(samples[i]) < 0.0001) samples[i] = 0; // Prosta filtracja
        }
        return samples;
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

            samples = trimSilenceFromEdges(samples, 0.02);
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

    private static MultiLayerNetwork loadModel(File modelFile) {
        MultiLayerNetwork model = null;
        try {
            model = MultiLayerNetwork.load(modelFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    private static double[] oneHotEncode(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        double[] oneHotEncoded = new double[size];
        oneHotEncoded[index] = 1.0;
        return oneHotEncoded;
    }


    private static void evaluateModel(MultiLayerNetwork model, List<String> words, List<List<double[]>> mfccSamplesList) {
        Evaluation eval = new Evaluation(words.size());  // Inicjalizacja obiektu oceny z liczbą klas/słów

        for (int wordIndex = 0; wordIndex < words.size(); wordIndex++) {
            List<double[]> mfccSamples = mfccSamplesList.get(wordIndex);

            for (double[] mfcc : mfccSamples) {
                INDArray input = Nd4j.create(mfcc, new int[]{1, mfcc.length});  // Tworzenie NDArray dla danych wejściowych
                INDArray output = model.output(input);  // Pobranie predykcji modelu

                INDArray labels = Nd4j.create(oneHotEncode(wordIndex, words.size()), new int[]{1, words.size()});  // Tworzenie NDArray dla etykiet
                eval.eval(labels, output);  // Ocena predykcji
            }
        }

        System.out.println(eval.stats());  // Wyświetlanie statystyk oceny
    }



}
