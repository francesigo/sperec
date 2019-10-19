package sperecRemote;

/**
 * Created by FS on 01/07/2017.
 */

public class RequestBody {
    public byte rawSpeechData[];
    public double rawSpeechDataSampleRate;
    public int nbits;
    public String requestOption;
    int counter;

    public RequestBody() {
        counter = 0;
    }
}
