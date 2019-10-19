package sperec_common;


/**
 * Created by FS on 27/02/2018.
 */

public interface IEnrolledSpeakersDatabase {
	
    public boolean enrollSpeaker(SpeakerIdentity speakerUID, SpeakerModel spkModel) throws Exception;
    
    /**
     * 
     * @param speakerUID
     * @return the model of the speaker owning "speakerUID" or null
     */
    public SpeakerModel getSpeaker(SpeakerIdentity speakerUID);
    
    /**
     * 
     * @param speakerUID
     * @return true if the speakerUID is in the database, false otherwise
     */
    public boolean hasIdentity(SpeakerIdentity speakerUID);
    
    public SpeakerIdentity[] getSpeakerIdentities();
    
    public String[] getSpeakerIdentitiesStrings();
    
    public SpeakerIdentity getSpeakerIdentityByUserName(String username);
}
