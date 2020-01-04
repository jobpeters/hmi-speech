import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main {

    private main() {
    }

    private void readCsv() throws IOException {
        /*
        start reading the transcriptions of the test set
        */
        File transcript = new File("../audio/transcriptions.csv");
        BufferedReader csvReader = new BufferedReader(new FileReader(transcript));
        csvReader.readLine().split(";");
        Map transcription = new HashMap();
        String row = "";
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(";");
            transcription.put(data[0], data[1]);
        }
        csvReader.close();
        /*
        start looping through every audio folder in the root audio folder
        */
        File audioFolder = new File("../audio/audio");
        for (File audioSubFolder : audioFolder.listFiles()) {
            String audioFolderPath = audioSubFolder.getCanonicalPath() + "/";
            String name = audioSubFolder.getName();
            System.out.println(audioFolderPath);
            System.out.println(name);
            /*
            create a new csv file for this audio folder to store its results.
            */
            File newCsv = new File("../audio/output/" + name + ".csv");
            BufferedWriter Writer = new BufferedWriter(new FileWriter(newCsv));
            CSVWriter csvWriter = new CSVWriter(Writer,
                    ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            String[] headerRecord = {"File", "Reference", "Hypothesis", "subs", "dels", "inserts", "corrects", "wer", "wcr", "precision", "recall", "fScore"};
            csvWriter.writeNext(headerRecord);

            String[] postFixes = {".wav", "_gustav.wav", "_ronald.wav", "_vincent.wav"};
            for (Object key : transcription.keySet()) {
                Object value = transcription.get(key);
                for (String postFix : postFixes) {
                    File file = new File((audioFolderPath + "/" + key.toString() + postFix));
                    System.out.println(file.getCanonicalPath());
                    System.out.println(file.getName());
                    /*
                    for every audio file in this folder.
                    get the hypothesis from ibm watson.
                    calculate the error rates and scores.
                    and then append those results to the csv output file of the corresponding audio folder.
                     */
                    if (file.exists()) {
                        System.out.printf("file: %s\ntranscript: %s\n", (audioFolderPath + key.toString() + postFix), value.toString());
                        Object[] results = toText(file, value.toString());
                        String hypothesis = results[0].toString();
                        double[] measurements = (double[]) results[1];
                        csvWriter.writeNext(new String[]{ key.toString() + postFix, value.toString(), hypothesis,
                                String.valueOf(measurements[0]), String.valueOf(measurements[1]), String.valueOf(measurements[2]),
                                String.valueOf(measurements[3]), String.valueOf(measurements[4]), String.valueOf(measurements[5]),
                                String.valueOf(measurements[6]), String.valueOf(measurements[7]), String.valueOf(measurements[8])
                        });
                    }
                }
            }
            csvWriter.close();
            Writer.close();
        }
    }

    private double[] checkOutput(String[] reference, String[] hypothesis) {
        System.out.printf("comparing %s to %s\n", Arrays.toString(hypothesis), Arrays.toString(reference));
        WordSequenceAligner werEval = new WordSequenceAligner();
        String[] ref = reference;
        String[] hyp = hypothesis;

        WordSequenceAligner.Alignment a = werEval.align(ref, hyp);

        double subs = a.numSubstitutions;
        double dels = a.numDeletions;
        double inserts = a.numInsertions;
        double corrects = a.getNumCorrect();
        double numRefWords = a.getReferenceLength();
        double wer = (subs + dels + inserts) / numRefWords;
        double wcr = corrects / numRefWords;

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

        double fScore = (2 * precision * recall) / (precision + recall);

        return new double[]{subs, dels, inserts, corrects, wer, wcr, precision, recall, fScore};
    }

    private Object[] toText(File audio, String expectedOutput) {
        // letting the SDK manage the IAM token
        Authenticator authenticator = new IamAuthenticator("RUDHTgSHCwsLZQ_YZqWd6SvU2WI9Ewh5kt-3yXc4gXXg");
        SpeechToText service = new SpeechToText(authenticator);
        service.setServiceUrl("https://gateway-lon.watsonplatform.net/speech-to-text/api");

        RecognizeOptions options = null;
        try {
            options = new RecognizeOptions.Builder()
                    .audio(audio)
                    .contentType(HttpMediaType.AUDIO_WAV)
                    .build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SpeechRecognitionResults transcript = service.recognize(options).execute().getResult();
        String[] hypothesis = transcript.getResults().get(0).getAlternatives().get(0).getTranscript().split(" ");
        String[] reference = expectedOutput.split(" ");
        double[] measurements = checkOutput(reference, hypothesis);
        return new Object[]{transcript.getResults().get(0).getAlternatives().get(0).getTranscript(), measurements};
    }

    private void run() {
        try {
            readCsv();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ;

    public static void main(String[] args) {
        new main().run();
    }
}
