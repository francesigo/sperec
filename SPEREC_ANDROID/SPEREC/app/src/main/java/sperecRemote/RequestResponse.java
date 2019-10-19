package sperecRemote;

/**
 * Created by FS on 02/07/2017.
 */

public class RequestResponse {
    public String errMsg; // if not null, an error has occurred
    public String responseType; // the meaning of the current response
    public String idUser; //user identified of authenticated, or null
    public String idResultMsg;

    public RequestResponse() {
        errMsg = null;
        responseType = null;
        idUser = null;
        idResultMsg = null;
    }
}
