package sperecRemote;

import android.util.Log;

import sperec_common.MyAudioInfo;
import com.google.gson.Gson;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.IOException;


public class SperecRemote {

    static public String sendSaveAsWavRequest(MyAudioInfo aInfo, String fileName) { //For debug

        RequestResponse rr = null;
        byte[] audioBuffer = aInfo.getDataBuffer();

        // Create the clientResource with the uri string
        String username = "debug";
        String uri = "http://212.189.143.131:8192/audio/" + fileName + "&" + username;
        ClientResource cr = new ClientResource(uri);

        String response      = null;
        Representation frRes = null;

        // build the request for the server
        RequestBody reqBody = new RequestBody();
        reqBody.rawSpeechData =  audioBuffer; //myAudioRecorder.getSamples();
        reqBody.rawSpeechDataSampleRate = aInfo.getSampleRate();
        reqBody.nbits = aInfo.getBytesPerSample()*8;
        reqBody.requestOption = "DEBUG_SAVE_AS_WAV"; //"force"; // force the server to verify user

        Gson gson = new Gson();
        String payload = gson.toJson(reqBody, RequestBody.class);

        boolean err = false;
        try {
            frRes = cr.post(payload); //THE POST
            response = frRes.getText();
            rr = gson.fromJson(response, RequestResponse.class);
            if (null == rr.errMsg) {
                String resType = null;
                resType = rr.responseType;
                if (resType.equals("TYPE_UPLOAD_SAMPLES")) {
                    processIdResponse(rr);
                } else if (resType.equals("TYPE_ID_RESPONSE")) {
                    processIdResponse(rr);
                }
            }
            else {
                response = "ERROR: " + rr.errMsg;
                err = true;
            }
        } catch (ResourceException e) {
            response = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
            Log.e("QQQ", response);
            err = true;
        } catch (IOException e) {
            response = "IOException in PostAudioTask";
            Log.e("sendSaveAsWavRequest", response);
            err = true;
        }
        if (err) {
            //Toast.makeText(ctx, response, Toast.LENGTH_SHORT).show(); // CRASH?????
        }
        return response;
    }
    //----------------------------------------------------------------------------------------------
    static private void processIdResponse(RequestResponse rr) {
        String msg = rr.idResultMsg;
        Log.i("processIdResponse", ": " + msg);
    }
}
