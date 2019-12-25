import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main {

    private main() {
    }

    ;

    private void readCsv() throws IOException {
        File transcript = new File("../audio/transcriptions.csv");
        System.out.println(transcript);
        BufferedReader csvReader = new BufferedReader(new FileReader(transcript));
        String row = "";
        List<List<Pair>> transcriptionsList;
        String[] header = csvReader.readLine().split(";");
        Map transcription = new HashMap();
        while ((row = csvReader.readLine()) != null) {
            List<Pair<String, String>> item = null;
            String[] data = row.split(";");
            transcription.put(data[0], data[1]);

            // do something with the data
        }
        csvReader.close();
//        CSVReader csvReader2 = new CSVReader(new InputStreamReader(csvFile.getInputStream()), '\t');
//        List transcriptionParser = new CsvToBeanBuilder(new FileReader(transcript)).withSeparator(';').withType(transcription.class).build().parse();
//        for (Object transcription :transcriptionParser) {
//            System.out.println(Arrays.toString(transcription.getClass().getMethods()));
//            try {
//                System.out.println(transcription.getClass().getMethod("getFile"));
//                System.out.println(transcription.);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//        }
//
////        // Hashmap to map CSV data to
////        // Bean attributes.
////        Map<String, String> mapping = new HashMap<String, String>();
////        mapping.put("File", "File");
////        mapping.put("Transcript", "Transcript");
////
////        // HeaderColumnNameTranslateMappingStrategy
////        // for transcription class
////        HeaderColumnNameTranslateMappingStrategy<transcription> strategy =
////                new HeaderColumnNameTranslateMappingStrategy<transcription>();
////        strategy.setType(transcription.class);
////        strategy.setColumnMapping(mapping);
//
//
//        // Create csvtobean and csvreader object
//        CSVReader csvReader = null;
//        List<transcription> list = null;
//        try {
//            csvReader = new CSVReader(new FileReader("../audio/transcriptions.csv"));
//        }
//        catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        // print details of Bean object
//        for (transcription e : list) {
//            System.out.println(e);
//        }
//
        File audioFolder = new File("../audio/audio");
        for (File audioSubFolder : audioFolder.listFiles()) {
            String audioFolderPath = audioSubFolder.getCanonicalPath() + "/";
            String name = audioSubFolder.getName();
            System.out.println(audioFolderPath);
            System.out.println(name);
            File newCsv = new File("../audio/output/" + name + ".csv");
            BufferedWriter Writer = new BufferedWriter(new FileWriter(newCsv));
            CSVWriter csvWriter = new CSVWriter(Writer,
                    ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
//        String[] headerRecord = {"File", "Reference", "Hypothesis"};
            String[] headerRecord = {"File", "Reference", "Hypothesis", "subs", "dels", "inserts", "corrects", "wer", "wcr", "precision", "recall", "fScore"};
            csvWriter.writeNext(headerRecord);
//            System.out.printf("absolute path: %s\ncanonical path: %s\nname: %s\n", audioSubFolder.getAbsolutePath(), audioSubFolder.getCanonicalPath(),audioSubFolder.getName());
//        String audioFolderPath = "../audio/Homonyms/";
            String[] postFixes = {".wav", "_gustav.wav", "_ronald.wav", "_vincent.wav"};
            for (Object key : transcription.keySet()) {
                Object value = transcription.get(key);
                for (String postFix : postFixes) {
//                System.out.printf("file: %s\ntranscript: %s\n",(audioFolderPath+key.toString()+postFix), value.toString());

                    File file = new File((audioFolderPath + "/" + key.toString() + postFix));

                    System.out.println(file.getCanonicalPath());
                    System.out.println(file.getName());
                    if (file.exists()) {
                        System.out.printf("file: %s\ntranscript: %s\n", (audioFolderPath + key.toString() + postFix), value.toString());
//                    csvWriter.writeNext(new String[]{key.toString()+postFix, value.toString()});
                        Object[] results = toText(file, value.toString());
                        String hypothesis = results[0].toString();
                        double[] measurements = (double[]) results[1];
                        csvWriter.writeNext(new String[]{
                                key.toString() + postFix,
                                value.toString(),
                                hypothesis,
                                String.valueOf(measurements[0]),
                                String.valueOf(measurements[1]),
                                String.valueOf(measurements[2]),
                                String.valueOf(measurements[3]),
                                String.valueOf(measurements[4]),
                                String.valueOf(measurements[5]),
                                String.valueOf(measurements[6]),
                                String.valueOf(measurements[7]),
                                String.valueOf(measurements[8])
                        });
                        System.out.println(Arrays.toString(new String[]{
                                key.toString() + postFix,
                                value.toString(),
                                hypothesis,
                                String.valueOf(measurements[0]),
                                String.valueOf(measurements[1]),
                                String.valueOf(measurements[2]),
                                String.valueOf(measurements[3]),
                                String.valueOf(measurements[4]),
                                String.valueOf(measurements[5]),
                                String.valueOf(measurements[6]),
                                String.valueOf(measurements[7]),
                                String.valueOf(measurements[8])
                        }));
                    }
//                else
//                    System.out.println(file.getAbsolutePath()+ " <-- does not exist!");
                }
            }
//        csvWriter.notify();
            csvWriter.close();
            Writer.close();
        }
    }

    private double[] checkOutput(String[] reference, String[] hypothesis) {
        System.out.printf("comparing %s to %s\n", Arrays.toString(hypothesis), Arrays.toString(reference));
//        for (String word: resultByWord) {
//            for (String expectedWord: expectedResultByWord) {
//                int comparison = word.compareToIgnoreCase(expectedWord);
//                System.out.printf("is [%s] the same as [%s]?\n%d\n", word, expectedWord, comparison);
//            }
//        }
        WordSequenceAligner werEval = new WordSequenceAligner();
        String[] ref = reference;
        String[] hyp = hypothesis;

        WordSequenceAligner.Alignment a = werEval.align(ref, hyp);
//        WordSequenceAligner.SummaryStatistics stats = new ;
        double subs = a.numSubstitutions;
        double dels = a.numDeletions;
        double inserts = a.numInsertions;
        double corrects = a.getNumCorrect();
        double numRefWords = a.getReferenceLength();
        double wer = (subs + dels + inserts) / numRefWords;
        double wcr = corrects / numRefWords;

        System.out.println(a);
        System.out.println("WER == " + String.valueOf(wer));
        System.out.println("WCR == " + String.valueOf(wcr));

        List<String> relevantSet = Arrays.asList(reference);
        List<String> retrievedSet = Arrays.asList(hypothesis);

        double precision;
        double recall;
        double fraction = 0;
        for (String word : relevantSet) {
            if (retrievedSet.contains(word)) {
                fraction += 1;
            }
        }
        precision = fraction / relevantSet.size();
        fraction = 0;

        for (String word : retrievedSet) {
            if (relevantSet.contains(word)) {
                fraction += 1;
            }
        }
        recall = fraction / retrievedSet.size();
        System.out.println("precision == " + String.valueOf(precision));
        System.out.println("recall == " + String.valueOf(recall));

        double fScore = (2 * precision * recall) / (precision + recall);
        System.out.println("f-score == " + String.valueOf(fScore));
        //TODO
        return new double[]{subs, dels, inserts, corrects, wer, wcr, precision, recall, fScore};
    }

    private Object[] toText(File audio, String expectedOutput) {
        // letting the SDK manage the IAM token
        Authenticator authenticator = new IamAuthenticator("RUDHTgSHCwsLZQ_YZqWd6SvU2WI9Ewh5kt-3yXc4gXXg");
        SpeechToText service = new SpeechToText(authenticator);
        service.setServiceUrl("https://gateway-lon.watsonplatform.net/speech-to-text/api");

//        File audio = file;
        RecognizeOptions options = null;
        try {
            options = new RecognizeOptions.Builder()
                    .audio(audio)
                    .contentType(HttpMediaType.AUDIO_WAV)
                    //                    .contentType(RecognizeOptions.ContentType.AUDIO_WAV)
                    .build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SpeechRecognitionResults transcript = service.recognize(options).execute().getResult();
        String[] hypothesis = transcript.getResults().get(0).getAlternatives().get(0).getTranscript().split(" ");
        String[] reference = expectedOutput.split(" ");
        double[] measurements = checkOutput(reference, hypothesis);

//        System.out.println("correct rate = "+ String.valueOf(stats.getCorrectRate()));
        return new Object[]{transcript.getResults().get(0).getAlternatives().get(0).getTranscript(), measurements};
    }

    private void run() {
//        tryOut();
        try {
            readCsv();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String pathToFolder = "../audio/all";
//        File audioFolder = new File(pathToFolder);
//        for (File file : audioFolder.listFiles()) {
//            System.out.println("++++++++++++++++");
//            System.out.println(file.getName());
//            System.out.println("++++++++++++++++");
//            toText(file);
//            System.out.println("================");
//        }

    }

    ;

    public static void main(String[] args) {
        new main().run();
    }
}
