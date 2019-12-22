import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;

public class transcription implements Serializable{
    @CsvBindByName
    private String File;
    @CsvBindByName
    private String Transcript;

    public transcription(){};

    public String getFile() {
        return File;
    }

    public void setFile(String File) {
        this.File = File;
    }

    public String getTranscript() {
        return Transcript;
    }

    public void setTranscript(String Transcript) {
        this.Transcript = Transcript;
    }
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("transcription{file=").append(File).append(", Transcript=")
                .append(Transcript);

        return builder.toString();
    }
}
