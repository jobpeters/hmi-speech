import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;

public class main {
    public PrintWriter out;

    private main(){
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream("output.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);
        out.println("START OF OUTPUT");
    };

    private void readCsv() throws IOException {
        File transcript = new File("../audio/transcriptions.csv");
        System.out.println(transcript);
        out.println(transcript);
        BufferedReader csvReader = new BufferedReader(new FileReader(transcript));
        String row = "";
        List<List<Pair>> transcriptionsList;
        String[] header =  csvReader.readLine().split(";");
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

        String audioFolderPath = "../audio/all/";
        String[] postFixes = {".wav", "_gustav.wav", "_ronald.wav", "_vincent.wav"};
        Iterator iterator = transcription.keySet().iterator();
        while(iterator.hasNext()){
            Object key   = iterator.next();
            Object value = transcription.get(key);
            for (String postFix: postFixes) {
                System.out.printf("file: %s\ntranscript: %s\n",(audioFolderPath+key.toString()+postFix), value.toString());

                File file = new File((audioFolderPath+key.toString()+postFix));

                if (file.exists()) {
                    toText(file, value.toString());
                }
                else
                    System.out.println(file.getAbsolutePath()+ " <-- does not exist!");
            }
        }
    }
    private void checkOutput(String[] reference, String[] hypothesis) {
        System.out.printf("comparing %s to %s\n", Arrays.toString(hypothesis), Arrays.toString(reference));
//        for (String word: resultByWord) {
//            for (String expectedWord: expectedResultByWord) {
//                int comparison = word.compareToIgnoreCase(expectedWord);
//                System.out.printf("is [%s] the same as [%s]?\n%d\n", word, expectedWord, comparison);
//            }
//        }
        WordSequenceAligner werEval = new WordSequenceAligner();
        String [] ref = reference;
        String [] hyp = hypothesis;

        WordSequenceAligner.Alignment a = werEval.align(ref, hyp);
//        WordSequenceAligner.SummaryStatistics stats = new ;
        double subs = a.numSubstitutions;
        double dels = a.numDeletions;
        double inserts = a.numInsertions;
        double corrects = a.getNumCorrect();
        double numRefWords = a.getReferenceLength();
        double wer = (subs+dels+inserts)/numRefWords;
        double wcr = corrects/numRefWords;

        System.out.println(a);
        System.out.println("WER == " + String.valueOf(wer));
        System.out.println("WCR == " + String.valueOf(wcr));

        List<String> relevantSet = Arrays.asList(reference);
        List<String> retrievedSet = Arrays.asList(hypothesis);

        double precision = 0;
        double recall = 0;
        double fraction = 0;
        for (String word : relevantSet) {
            if (retrievedSet.contains(word)){
                fraction += 1;
            }
        }
        precision = fraction/relevantSet.size();
        fraction = 0;

        for (String word : retrievedSet) {
            if (relevantSet.contains(word)){
                fraction += 1;
            }
        }
        recall = fraction/retrievedSet.size();
        System.out.println("precision == " + String.valueOf(precision));
        System.out.println("recall == " + String.valueOf(recall));

        double fScore = (2*precision*recall)/(precision+recall);
        System.out.println("f-score == " + String.valueOf(fScore));
    }
    private void toText(File audio, String expectedOutput) {
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
        checkOutput(reference, hypothesis);


//        System.out.println("correct rate = "+ String.valueOf(stats.getCorrectRate()));
    }

    private void run(){
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

    };

    public static void main(String[] args) {
        new main().run();
    }
}
