
import java.io.*;
import java.util.*;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.BasicAuthenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.opencsv.CSVReader;
//import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
//import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class main {
    private main(){

    };
    private void readCsv() {

//        // Hashmap to map CSV data to
//        // Bean attributes.
//        Map<String, String> mapping = new HashMap<String, String>();
//        mapping.put("File", "File");
//        mapping.put("Transcript", "Transcript");
//
//        // HeaderColumnNameTranslateMappingStrategy
//        // for transcription class
//        HeaderColumnNameTranslateMappingStrategy<transcription> strategy =
//                new HeaderColumnNameTranslateMappingStrategy<transcription>();
//        strategy.setType(transcription.class);
//        strategy.setColumnMapping(mapping);


        // Create csvtobean and csvreader object
        CSVReader csvReader = null;
        List<transcription> list = null;
        try {
            csvReader = new CSVReader(new FileReader("../audio/transcriptions.csv"));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // print details of Bean object
        for (transcription e : list) {
            System.out.println(e);
        }

        String audioFolderPath = "../audio/all/";
        String[] postFix = {"_gustav.wav", "_ronald.wav", "_vincent.wav"};
        toText(new File(audioFolderPath + "tongue twisters" + postFix[0]));
    }
    private void toText(File file) {
        // letting the SDK manage the IAM token
        Authenticator authenticator = new IamAuthenticator("RUDHTgSHCwsLZQ_YZqWd6SvU2WI9Ewh5kt-3yXc4gXXg");
        SpeechToText service = new SpeechToText(authenticator);
        service.setServiceUrl("https://gateway-lon.watsonplatform.net/speech-to-text/api");

        File audio = new File("src/test/resources/speech_to_text/sample1.wav");
        File audio2 = file;
        RecognizeOptions options = null;
        try {
            options = new RecognizeOptions.Builder()
                        .audio(audio2)
                        .contentType(HttpMediaType.AUDIO_WAV)
    //                    .contentType(RecognizeOptions.ContentType.AUDIO_WAV)
                        .build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SpeechRecognitionResults transcript = service.recognize(options).execute().getResult();

        System.out.println(transcript);
    }

    private void run(){
//        tryOut();
        readCsv();
    };

    public static void main(String[] args) {
        new main().run();
    }
}
