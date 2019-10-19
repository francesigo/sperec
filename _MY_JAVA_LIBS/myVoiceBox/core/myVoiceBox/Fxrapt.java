package myVoiceBox;

import java.util.ArrayList;
import java.util.Arrays;

import Jama.Matrix;
import myDSP.MyComplexArrayFloat;
import myDSP.MyFilter;
import myMath.MyMath;
import myMath.MyMatrix;
import myMath.MySort.SortDir;

/**
 * 
 * RAPT pitch tracker [FX,VUV]=(S,FS)
 *
 * Input:   s(ns)      Speech signal
 *          fs         Sample frequency (Hz)
 *          mode       'u' will include unvoiced fames (with fx=NaN)
 *
 * Outputs: fx(nframe)     Larynx frequency for each frame (or NaN for silent/unvoiced)
 *          tt(nframe,3)  Start and end samples of each frame. tt(*,3)=1 at the start of each talk spurt
 *
 * The algorithm is taken from [1] with the following differences:
 *
 *      (a)  the factor AFACT which in the Talkin algorithm corresponds roughly
 *           to the absolute level of harmonic noise in the correlation window. This value
 *           is here calculated as the maximum of three figures:
 *                   (i) an absolute floor set by PP.rapt_absnoise
 *                  (ii) a multiple of the peak signal set by PP.rapt_signoise
 *                 (iii) a multiple of the noise floor set by PP.rapt_relnoise
 *      (b) The LPC used in calculating the Itakura distance uses a Hamming window rather than
 *          a Hanning window.
 *
 * A C implementation of this algorithm by Derek Lin and David Talkin is included as  "get_f0.c"
 * in the esps.zip package available from http://www.speech.kth.se/esps/esps.zip under the BSD
 * license.
 *
 * Refs:
 *      [1]   D. Talkin, "A Robust Algorithm for Pitch Tracking (RAPT)"
 *            in "Speech Coding and Synthesis", W B Kleijn, K K Paliwal eds,
 *            Elsevier ISBN 0444821694, 1995

 * @author FS
 *
 */
public class Fxrapt {

	static final private double f0min = 50;       // Min F0 (Hz)                               [50]
	static final private double f0max = 500;      // Max F0 (Hz)                               [500]
	static final private double tframe = 0.01;    // frame size (s)                            [0.01]
	static final private double tlpw = 0.005;     // low pass filter window size (s)           [0.005]
	static final private double tcorw = 0.0075;   // correlation window size (s)               [0.0075]
	static final private double candtr = 0.3;     // minimum peak in NCCF                      [0.3]
	static final private double lagwt = 0.3;      // linear lag taper factor                   [0.3]
	static final private double freqwt = 0.02;    // cost factor for F0 change                 [0.02]
	static final private double vtranc = 0.005;   // fixed voice-state transition cost         [0.005]
	static final private double vtrac = 0.5;      // delta amplitude modulated transition cost [0.5]
	static final private double vtrsc = 0.5;      // delta spectrum modulated transition cost  [0.5]
	static final private double vobias = 0.0;     // bias to encourage voiced hypotheses       [0.0]
	static final private double doublec = 0.35;   // cost of exact doubling or halving         [0.35]
	static final private double absnoise = 0;     // absolute rms noise level                  [0]
	static final private double relnoise = 2.0;   // rms noise level relative to noise floor   [2.0]
	static final private double signoise = 0.001; // ratio of peak signal rms to noise floor   [0.001]
	static final private int    ncands = 20;      // max hypotheses at each frame              [20]
	static final private double trms = 0.03;      // window length for rms measurement         [0.03]
	static final private double dtrms = 0.02;     // window spacing for rms measurement        [0.02]
	static final private double preemph = -7000;  // s-plane position of preemphasis zero      [-7000]
	static final private int nfullag = 7;      // number of full lags to try (must be odd)  [7]
	
	// Output
	public double [] fx = null;
	public double [][] tt = null;
	//public boolean [] spurt = null;

	/**
	 * Compute pitch
	 * @param sfloat the input audio signal
	 * @param fs the sample rate of the input signal
	 * @param mode must be equals to "u"
	 * @throws Exception
	 */
	public Fxrapt(float [] sfloat, double fs, String mode) throws Exception {

		if (!mode.equals("u"))
			throw new Exception("Fxrapt: unsupported mode: " + mode);
		
		// I prefer to work in double precision
		double [] s = MyMath.todouble(sfloat);

		//derived parameters (mostly dependent on sample rate fs)

		int krms         = (int)Math.round(trms*fs);            // window length for rms measurement
		int kdrms        = (int)Math.round(dtrms*fs);           // window spacing for rms measurement
		
		//rmswin=hanning(krms).^2;
		double [] rmswin = MyMath.hanning(krms);
		rmswin           = MyMath.arrayTimes(rmswin, rmswin);
		
		int kdsmp        = (int)Math.round(0.25*fs/f0max);
		int hlpw         = (int)Math.round(tlpw*fs/2);          // force window to be an odd length
		
		// blp=sinc((-hlpw:hlpw)/kdsmp).*hamming(2*hlpw+1).';
		double [] blp    = MyMath.applyHammingWindow(
							MyMath.sinc(
							MyMath.times(
							MyMath.colon(-hlpw, hlpw), 1.0/kdsmp))); // sinc((-hlpw:hlpw)/kdsmp).*hamming(2*hlpw+1).';

		double fsd  = fs/kdsmp; // fsd=fs/kdsmp;
		int kframed = (int)Math.round(fsd*tframe);      // downsampled frame length
		int kframe  = kframed*kdsmp;                    // frame increment at full rate
		
		// ATTENZIONE, LA VARIABILE rmsix e' UNA SEQUENZA DI krms elementi a partire da 1 + (int)Math.floor((double)(kdrms-kframe)/2
		// Di fatto cio' che serve sono soltanto il primo indice e l'ultimo.
		// Converra' quindi modificare il codice per sfruttare questo fatto.
		// rmsix=(1:krms)+floor((kdrms-kframe)/2); % rms index according to Talkin; better=(1:krms)+floor((kdrms-krms+1)/2)
		//int [] rmsix = MyMath.add(MyMath.colon(1, krms), (int)Math.floor((double)(kdrms-kframe)/2)); //ATTENZIONE SONO INDICI, FORSE SEVO CORREGGERE // rms index according to Talkin; better=(1:krms)+floor((kdrms-krms+1)/2)
		int rmsix_1 = 1 + (int)Math.floor((double)(kdrms-kframe)/2);
		int rmsix_end = rmsix_1 + krms -1;
		int [] rmsix = MyMath.colon(rmsix_1, rmsix_end);
		
		int minlag  = (int)Math.ceil(fsd/f0max);
		int maxlag  = (int)Math.round(fsd/f0min);       // use round() only because that is what Talkin does
		int kcorwd  = (int)Math.round(fsd*tcorw);       // downsampled correlation window
		int kcorw   = kcorwd*kdsmp;                     // full rate correlation window
		int spoff   = Math.max(hlpw - (int)Math.floor(kdsmp/2.0), 1 + kdrms - rmsix[0]-kframe); //max(hlpw - floor(kdsmp/2) , 1+kdrms-rmsix(1)-kframe);  % offset for first speech frame at full rate
		int sfoff   = spoff - hlpw + (int)Math.floor(kdsmp/2.0); // offset for downsampling filter
		int [] sfi  = MyMath.colon(0, kcorwd-1);        // -1 because they are indexes sfi = 1:kcorwd;     % initial decimated correlation window index array
		int [] sfhi = MyMath.colon(0, kcorw-1);         // initial correlation window index array
		//  sfj=1:kcorwd+maxlag;
		// Beacause sfj are consecutives, just the first and the last are really useful. Also, they need to be adjusted (-1) when used
		int sfj_1 = 1;
		int sfj_end = kcorwd+maxlag;
		//sfmi=repmat((minlag:maxlag)',1,kcorwd)+repmat(sfi,maxlag-minlag+1,1);
		int lagoff  = (minlag-1)*kdsmp;          // lag offset when converting to high sample rate
		double beta = lagwt*f0min/fs;            // bias towards low lags
		double log2 = Math.log(2);
		int lpcord  = 2 + (int)Math.round(fs/1000);        // lpc order for itakura distance
		int hnfullag    = (int)Math.floor((double)nfullag/2);
		double jumprat  = Math.exp((doublec+log2)/2);  // lag ratio at which octave jump cost is lowest
		double [] ssq   = MyMath.arrayTimes(s, s); //s.^2;
		double [] csssq = MyMath.cumsum(ssq);
		// ????? sqrt(min(csssq(kcorw+1:end)-csssq(1:end-kcorw))/kcorw);

		//afact=max([absnoise^2,max(ssq)*signoise^2,min(csssq(kcorw+1:end)-csssq(1:end-kcorw))*(relnoise/kcorw)^2])^2*kcorw^2;
		double [] _temp1 = new double [] {absnoise*absnoise , 
				MyMath.max(ssq) * signoise * signoise,
				MyMath.min(
						MyMath.minus(MyMath.select(csssq, kcorw, csssq.length-1), MyMath.select(csssq, 0, csssq.length-1-kcorw)) )
						* (relnoise/kcorw) * (relnoise/kcorw)};
		double afact = Math.pow(MyMath.max(_temp1), 2)*kcorw*kcorw;

		// downsample signal to approx 2 kHz to speed up autocorrelation calculation
		// kdsmp is the downsample factor

		double [] sf = MyFilter.filter(MyMath.times(blp,  1.0/MyMath.sum(blp)), 1.0, MyMath.select(s, sfoff, s.length-1)); // sf = filter(blp/sum(blp), 1, s(sfoff+1:end));
		double [] sp = MyFilter.filter(new double [] {1.0, Math.exp(preemph/fs)}, 1.0, s); //sp=filter([1 exp(preemph/fs)],1,s); % preemphasised speech for LPC calculation
		sf = MyMath.select(sf, blp.length-1, sf.length-1); //sf(1:length(blp)-1)=[];         % remove startup transient
		sf = MyMath.select(sf, 0, sf.length-1, kdsmp); //sf = sf(1:kdsmp:end);             % downsample to =~2kHz
		int nsf= sf.length;                 // length of downsampled speech
		int ns = s.length;                   // length of full rate speech

		// Calculate the frame limit to ensure we don't run off the end of the speech or decimated speech:
		//   (a) For decimated autocorrelation when calculating sff():  (nframe-1)*kframed+kcorwd+maxlag <= nsf
		//   (b) For full rate autocorrelation when calculating sfh():  max(fho)+kcorw+maxlag*kdsamp+hnfllag <= ns
		//   (c) For rms ratio window when calculating rr            :  max(fho)+rmsix(end) <= ns
		// where max(fho) = (nframe-1)*kframe + spoff

		//nframe=floor(1+min((nsf-kcorwd-maxlag)/kframed,(ns-spoff-max(kcorw-maxlag*kdsmp-hnfullag,rmsix(end)))/kframe));
		double _t1 = (double)(nsf-kcorwd-maxlag)/kframed;
		double _t2 = (double)(ns - spoff -Math.max(kcorw-maxlag*kdsmp-hnfullag, rmsix[rmsix.length-1]))/kframe;
		int nframe = (int)Math.floor(1 + Math.min(_t1,  _t2));

		// now search for autocorrelation peaks in the downsampled signal

		double [][] cost = new double[nframe][ncands]; // No actual need to initialize to zero //MyMatrix cost = new MyMatrix(nframe, ncands, 0.0); // .zeros(nframe,ncands);      % cumulative cost
		int [][] prev = new int[nframe][ncands]; // no need to inizalize ; Matrix(nframe, ncands, 0.0).getArray(); //zeros(nframe,ncands);      % traceback pointer
		int [] mcands = new int [nframe]; Arrays.fill(mcands,  0);// zeros(nframe,1);         % number of actual candidates excluding voiceless
		MyMatrix lagval = new MyMatrix(nframe, ncands-1, Double.NaN); //repmat(NaN,nframe,ncands-1);    % lag of each voiced candidate
		double [][] tv = new Matrix(nframe, 6, 0.0).getArray(); //zeros(nframe,3);   It actually will use 6 columns          % diagnostics: 1=voiceless cost, 2=min voiced cost, 3:cumulative voiceless-min voiced
		//if doback
		//    costms=cell(nframe,1);
		//end

		// Main processing loop for each 10 ms frame

		for (int iframe = 0; iframe<nframe; iframe++) {//for iframe=1:nframe       % loop for each frame (~10 ms)

			// Find peaks in the normalized autocorrelation of subsampled (2Khz) speech
			// only keep peaks that are > 30% of highest peak

			int fho = iframe*kframe + spoff; // fho=(iframe-1)*kframe+spoff;
			double [] sff = MyMath.select(sf, iframe*kframed + sfj_1 -1, iframe*kframed + sfj_end -1); //sff=sf((iframe-1)*kframed+sfj);
			double sffdc = MyMath.mean(sff, sfi); //sffdc=mean(sff(sfi));       % mean of initial correlation window length
			MyMath.minusSelf(sff,  sffdc); //sff = sff-sffdc;              % subtract off the mean
			double [] nccfd = normxcor(MyMath.select(sff, 0, kcorwd-1), MyMath.select(sff, minlag, sff.length-1)); //nccfd=normxcor(sff(1:kcorwd),sff(minlag+1:end));
			V_findpeaks v_findpeaks = new V_findpeaks(nccfd, "q"); //[ipkd,vpkd]=v_findpeaks(nccfd,'q');
			double [] ipkd = v_findpeaks.kk;
			double [] vpkd = v_findpeaks.v;
			
			MyMatrix Vipkd = null;
			if ((ipkd!=null) && (ipkd.length>0))
			{
				// Debugging: execute the line below to plot the autocorrelation peaks.
				// v_findpeaks(nccfd,'q'); xlabel(sprintf('Lag = (x+%d)*%g ms',minlag-1,1000*kdsmp/fs)); ylabel('Normalized Cross Correlation'); title (sprintf('Frame %d/%d',iframe,nframe));
				// 			vipkd=[vpkd ipkd];
				double [][]vipkd = new double[][] {vpkd, ipkd};

				Vipkd = new MyMatrix(vipkd).transpose();
				Vipkd = Vipkd.removeRows(MyMath.lt(vpkd, MyMath.max(vpkd)*candtr)); // vipkd(vpkd<max(vpkd)*candtr,:)=[];          % eliminate peaks that are small
			}	
			int nlcan = 0;
			int [] lagcan = null;
		    if (Vipkd != null)
		    { // if size(vipkd,1)
		        if (Vipkd.getRowDimension() >ncands-1) { //if size(vipkd,1)>ncands-1
		        	Vipkd = Vipkd.sortrows(SortDir.ASCENDING); //vipkd=sortrows(vipkd);
		        	Vipkd = Vipkd.removeRows(0, Vipkd.getRowDimension()-ncands+1-1); // vipkd(1:size(vipkd,1)-ncands+1,:)=[];   % eliminate lowest to leave only ncands-1
		        }
		        // lagcan=round(vipkd(:,2)*kdsmp+lagoff);        % convert the lag candidate values to the full sample rate
		        lagcan = new int [Vipkd.getRowDimension()];
		        for (int i=0; i<lagcan.length; i++)
		        	lagcan[i] = (int)Math.round((Vipkd.get(i, 1)+1)*kdsmp + lagoff); // il +1 a secondo membro e' necessario tenendo conto del significato di quella varaibile
		        nlcan = lagcan.length; //nlcan=length(lagcan);
		    }
		    
		    // If there are any candidate lag values (nlcan>0) then refine their accuracy at the full sample rate
		    int mc = 0;
		    MyMatrix Vipk = null;
		    double e0 = 0;
		    if (nlcan>0)
		    {
		    	// UNUSED laglist=reshape(repmat(lagcan(:)',nfullag,1)+repmat((-hnfullag:hnfullag)',1,nlcan),nfullag*nlcan,1);
		    	// sfh=s(fho+(1:kcorw+max(lagcan)+hnfullag));
		    	double [] sfh = MyMath.select(s, fho+1-1, fho + kcorw + MyMath.max(lagcan) + hnfullag-1); 
		    	double sfhdc = MyMath.mean(MyMath.select(sfh, sfhi));// sfhdc=mean(sfh(sfhi));
		    	MyMath.minusSelf(sfh,  sfhdc);// sfh=sfh-sfhdc;
		    	
		    	// e0=sum(sfh(sfhi).^2);                     % energy of initial correlation window (only needed to store in tv(:,6)
		    	double [] _t3 = MyMath.select(sfh, sfhi);
		    	e0 = MyMath.dot(_t3,  _t3);
		    	
		    	//lagl2=repmat(lagcan(:)',nfullag+kcorw-1,1)+repmat((1-hnfullag:hnfullag+kcorw)',1,nlcan);
		        int [] _t4 = MyMath.colon(1-hnfullag, hnfullag+kcorw);
		        int [][] lagl2 = new int[nfullag+kcorw-1][nlcan];
		        for (int i=0; i<lagl2.length; i++)
		        	for (int j=0; j<nlcan; j++)
		        		lagl2[i][j] = lagcan[j] + _t4[i] -1; // per usarlo come indice, devo sottratrre 1
		        
		        MyMatrix nccf = normxcor(MyMath.select(sfh, 0, kcorw-1), MyMath.select(sfh, lagl2), afact); // nccf=normxcor(sfh(1:kcorw),sfh(lagl2),afact);
		        
		        // [maxcc,maxcci]=max(nccf,[],1);
		        ArrayList<MyMatrix> maximax = nccf.maximax(1);
		        double [] maxcc         = maximax.get(0).getRow(0);
		        double [] maxcci_double = maximax.get(1).getRow(0);
		        int [] maxcci = MyMath.toint(maxcci_double);
		        // vipk=[maxcc(:) lagcan(:)+maxcci(:)-hnfullag-1];
		        // vipk=vipk(:,[1 2 2]);
		        double [] _t5 = new double[maxcc.length];
		        for (int i=0; i<maxcc.length; i++)
		        	_t5[i] = lagcan[i]+maxcci_double[i]-hnfullag-1;
		        
		        Vipk = new MyMatrix(new double[][] {maxcc, _t5, _t5}).transpose();
		        
		        // maxccj=maxcci(:)'+nfullag*(0:nlcan-1);    % vector index into nccf array
		        int [] maxccj = new int[maxcci.length];
		        for (int i=0; i<maxccj.length; i++)
		        	maxccj[i] = maxcci[i] + nfullag*i; 
		        
		        boolean [] msk = new boolean[maxcci.length];
		        for (int i =0; i<msk.length; i++)
		        	msk[i] = (MyMath.mod(maxcci[i]+1, nfullag-1) != 1) &&
		        			( 2*nccf.get(maxccj[i]) - nccf.get(MyMath.mod(maxccj[i]+1-2,  nfullag*nlcan)+1-1) - nccf.get(MyMath.mod(maxccj[i]+1, nfullag*nlcan)+1-1) >0)   ; // CONTROLLARE la qustione degli indici!!!!!
		        // msk=mod(maxcci,nfullag-1)~=1 & 2*nccf(maxccj)-nccf(mod(maxccj-2,nfullag*nlcan)+1)-nccf(mod(maxccj,nfullag*nlcan)+1)>0;  % don't do quadratic interpolation for the end ones

		        if (MyMath.any(msk))
		        {
		        	maxccj = MyMath.select(maxccj, msk); // maxccj=maxccj(msk);
		        	// vipk(msk,3)=vipk(msk,3)+(nccf(maxccj+1)-nccf(maxccj-1))'./(2*(2*nccf(maxccj)-nccf(maxccj-1)-nccf(maxccj+1)))';
		        	int [] msk_i = MyMath.find(msk);
		        	for (int i=0; i<msk_i.length; i++)
		        	{
		        		double _t6 = Vipk.get(msk_i[i], 2) + (nccf.get(maxccj[i]+1) - nccf.get(maxccj[i]-1)) / (2* (2*nccf.get(maxccj[i]) - nccf.get(maxccj[i]-1))) ;
		        		Vipk.set(msk_i[i], 2, _t6);
		        	}
		        }
		        
		        Vipk = Vipk.removeRows(MyMath.lt(maxcc, MyMath.max(maxcc)*candtr)); // vipk(maxcc<max(maxcc)*candtr,:)=[];          % eliminate peaks that are small
		        if ((Vipk!= null) && (Vipk.getRowDimension() > ncands-1)) {// if size(vipk,1)>ncands-1
		        	Vipk = Vipk.sortrows();
		        	Vipk = Vipk.removeRows(0, Vipk.getRowDimension() - ncands+1-1);
		        	// vipk(1:size(vipk,1)-ncands+1,:)=[];   % eliminate lowest to leave only ncands-1
		        }
		        		
		        // vipk(:,1) has NCCF value, vipk(:,2) has integer peak position, vipk(:,3) has refined peak position
		        mc = (Vipk==null) ? 0 : Vipk.getRowDimension(); 
		    }
		    
		    // We now have mc lag candidates at the full sample rate
		    int mc1=mc+1;               // total number of candidates including "unvoiced" possibility
		    mcands[iframe] = mc; //mcands(iframe)=mc;      % save number of lag candidates (needed for pitch consistency cost calculation)
		    
		    if (mc>0)
		    {
		    	lagval.setMatrix(iframe,  iframe,  0,  mc-1, MyMatrix.fromRowVector(Vipk.getColumn(2))); // lagval(iframe,1:mc)=vipk(:,3)'
		    	//cost(iframe,1)=vobias+max(vipk(:,1));   % voiceless cost
		    	cost[iframe][0] = vobias+ MyMath.max(Vipk.getColumn(0)); // cost.set(iframe, 1,  vobias+ MyMath.max(Vipk.getColumn(0)));
		    	// cost(iframe,2:mc1)=1-vipk(:,1)'.*(1-beta*vipk(:,3)');   % local voiced costs
		    	
		    	for (int i=0; i<mc1-1; i++)
		    		cost[iframe][i+1] = 1 - Vipk.get(i, 0)*(1-beta*Vipk.get(i, 2)); //cost.set(iframe, i+1, 1 - Vipk.get(i, 0)*(1-beta*Vipk.get(i, 2)));
		    	//tv(iframe,2)=min(cost(iframe,2:mc1));
		    	tv[iframe][1] = MyMath.min(cost[iframe], 1, mc1-1); 
		    }
		    else
		    {
		    	cost[iframe][0] = vobias; //cost.set(iframe, 0,  vobias); // cost(iframe,1)=vobias;
		    }
		    tv[iframe][0] = cost[iframe][0]; //cost.get(iframe, 0); // tv(iframe,1)=cost(iframe,1);
		 
		    double [][] costm = null;
		    if (iframe>0) // if it is not the first frame, then calculate pitch consistency and v/uv transition costs
		    {
		    	int mcp = mcands[iframe-1];		 // mcp=mcands(iframe-1);       
		        costm = new MyMatrix(mcp+1, mc1, 0.0).getArray(); // costm=zeros(mcp+1,mc1);         % cost matrix: rows and cols correspond to candidates in previous and current frames (incl voiceless)   
		        
		        // if both frames have at least one lag candidate, then calculate a pitch consistency cost
		        if (mc*mcp>0)
		        {
		        	//lrat=abs(log(repmat(lagval(iframe,1:mc),mcp,1)./repmat(lagval(iframe-1,1:mcp)',1,mc)));
		            double [][] lrat = new double[mcp][mc];
		            double [][] _t8 = lagval.getArray();
		            for (int i=0; i<mcp; i++)
		            	for (int j=0; j<mc; j++)
		            		lrat[i][j] = Math.abs(Math.log( _t8[iframe][j] /  _t8[iframe-1][i]));
		            //costm(2:end,2:end)=freqwt*min(lrat,doublec+abs(lrat-log2));  % allow pitch doubling/halving
		            for (int i=0; i<mcp; i++)
		            	for (int j=0; j<mc1-1; j++)
		            		costm[i+1][j+1] = freqwt * Math.min(lrat[i][j], doublec+Math.abs(lrat[i][j]-log2));
		        }

		        // if either frame has a lag candidate, then calculate the cost of voiced/voiceless transition and vice versa

		        if (mc+mcp>0)
		        {
		        	// rr=sqrt((rmswin'*s(fho+rmsix).^2)/(rmswin'*s(fho+rmsix-kdrms).^2)); % amplitude "gradient"
		        	double _t10 = 0.0;
		        	double _t11 = 0.0;
		        	for (int i=0; i<rmswin.length; i++) {
		        		_t10 += rmswin[i] * s[fho + rmsix[i]-1] * s[fho + rmsix[i]-1];
		        		_t11 += rmswin[i] * s[fho + rmsix[i] - kdrms-1] * s[fho + rmsix[i] - kdrms-1];
		        	}
		        	double rr = Math.sqrt(_t10/_t11); //CONTROLLARE QUESTO RISULTATO
		        	
		            // ss=0.2/(distitar(lpcauto(sp(fho+rmsix),lpcord),lpcauto(sp(fho+rmsix-kdrms),lpcord),'e')-0.8);   % Spectral stationarity: note: Talkin uses Hanning instead of Hamming windows for LPC
		        	double [][] _t12 = new double [][] {MyMath.select(sp, fho+rmsix_1-1, fho+rmsix_end-1)}; // -1 is due to java indexing
		        	Lpcauto L1 = Lpcauto.fromDoubleFramed(_t12, lpcord);
		        	double [][] _t13 = new double [][] {MyMath.select(sp, fho+rmsix_1-kdrms-1, fho+rmsix_end-kdrms-1)}; // -1 is due to java indexing
		        	Lpcauto L2 = Lpcauto.fromDoubleFramed(_t13, lpcord);
		        	double[][] _t14 = Distitar.exe(L1.getAR(), L2.getAR(), "e");
		        	double ss = 0.2 / (_t14[0][0] - 0.8);
		            
		        	for (int i=2; i<=costm[0].length; i++) // costm(1,2:end)= vtranc+vtrsc*ss+vtrac/rr;   % voiceless -> voiced cost
		        		costm[0][i-1] = vtranc+vtrsc*ss+vtrac/rr; //il lato destro sembra essere uno scalare
		        	for (int i=2; i<=costm.length; i++) //costm(2:end,1)= vtranc+vtrsc*ss+vtrac*rr;
		        		costm[i-1][0] = vtranc+vtrsc*ss+vtrac*rr; //il lato destro sembra essere uno scalare
		            
		        	//tv(iframe,4:5)=[costm(1,mc1) costm(mcp+1,1)];
		        	tv[iframe][3] = costm[0][mc1-1];
		        	tv[iframe][4] = costm[mcp][0];
		        }
		        // costm=costm+repmat(cost(iframe-1,1:mcp+1)',1,mc1);  % add in cumulative costs
		        for (int i=0; i<costm.length; i++)
		        	for (int j=0; j<costm[0].length; j++)
		        		costm[i][j] += cost[iframe-1][i];
		        
		        //[costi,previ]=min(costm,[],1);
		        double [][] _t15 = MyMath.minimin_1(costm);
		        double []costi = _t15[0];
		        double [] previ = _t15[1];
		        
		        //cost(iframe,1:mc1)=cost(iframe,1:mc1)+costi;
		        //prev(iframe,1:mc1)=previ;
		        for (int i=0; i<mc1; i++)
		        {
		        	cost[iframe][i] += costi[i];
		        	prev[iframe][i] = (int)previ[i];
		        }
		    }
		    else // first ever frame
		    {
		    	costm = new double[1][mc1]; //costm=zeros(1,mc1); % create a cost matrix in case doing a backward recursion
		    	Arrays.fill(costm[0], 0);
		    }
		    if (mc>0)
		    {
		    	tv[iframe][2] = cost[iframe][0] - MyMath.min(cost[iframe], 1, mc-1); //tv(iframe,3)=cost(iframe,1)-min(cost(iframe,2:mc1));
		    	tv[iframe][5] = 5.0*Math.log10(e0*e0/afact); //tv(iframe,6)=5*log10(e0*e0/afact);
		    }
	    
	    //if doback
	    //    costms{iframe}=costm; % need to add repmatted cost into this
	    //end
		}
		
		// now do traceback
		int [] best = new int[nframe]; //best=zeros(nframe,1);  // "best" are indexes starting from 0 (in java)
		Arrays.fill(best,  0);
		//[cbest,best(nframe)]=min(cost(nframe,1:mcands(nframe)+1));
		double [] _t16 = MyMath.minimin(cost[nframe-1], 0, mcands[nframe-1] + 1-1);
		double cbest = _t16[0];
		best[nframe-1] = (int)_t16[1];
		//for i=nframe:-1:2 ,  best(i-1)=prev(i,best(i)); end
		for (int i=nframe; i>=2; i--)
			best[i-2] = prev[i-1][best[i-1]];
		
		 // vix= find(best>1);
		boolean _t17 [] = new boolean[best.length];
		for (int i=0; i<best.length; i++)
			_t17[i] = best[i]>0; // SHOULD BE >0? 
		int [] vix = MyMath.find(_t17);
		
		// fx=repmat(NaN,nframe,1);                        % unvoiced frames will be NaN
		double [] fx = new double[nframe];
		Arrays.fill(fx,  Double.NaN);
		
		//fx(vix)=fs*lagval(vix+nframe*(best(vix)-2)).^(-1); % leave as NaN if unvoiced
		// With respect to matlab, I need a +1 near lagval, and a +1 near to -2
		for (int i=0; i<vix.length; i++)
			fx[vix[i]] = fs/(1+lagval.get(vix[i] + nframe*(best[vix[i]]-2+1)));
		
		// Here I make a change: I moved tt[:, 3] in a separate variable: spurt
		double [][] tt = new double[nframe][3]; // No need to set to zero; 		tt=zeros(nframe,3);
		boolean [] spurt = new boolean[nframe];
		for (int i=0; i<nframe; i++)
		{
			tt[i][0] = (i+1)*kframe + spoff; // tt(:,1)=(1:nframe)'*kframe+spoff;       % find frame times
			tt[i][1] = tt[i][0] + kframe-1; // tt(:,2)=tt(:,1)+kframe-1;
		}
		double jratm = (jumprat + 1/jumprat)/2;/// jratm=(jumprat+1/jumprat)/2;
		for (int i=1; i<nframe; i++)
			tt[i][2] = (Math.abs(fx[i]/fx[i-1]-jratm)>(jumprat-jratm)) ? 1.0 : 0.0;//tt(2:end,3)=abs(fx(2:end)./fx(1:end-1)-jratm)>jumprat-jratm;    % new spurt if frequency ratio is outside (1/jumprat,jumprat)
		tt[0][2] = 1.0; // tt(1,3)=1;           % first frame always starts a spurt
		// tt(1+find(isnan(fx(1:end-1))),3)=1; % NaN always forces a new spurt
		for (int i=0; i<fx.length-1; i++)
			if (fx[i]== Double.NaN)
				tt[1+i][2]= 1.0;
		
		//if ~any(mode=='u')
		//tt(isnan(fx),:)=[];    % remove NaN spurts
		//fx(isnan(fx),:)=[];
		//end
		
	    this.fx = fx;
	    this.tt = tt;
	    //this.spurt = spurt;
	}
	
	private static MyMatrix normxcor(double [] x, double [][] yy, double d) throws Exception {
		
		MyMatrix Y = new MyMatrix(yy);
		double [][] outTr = new double[Y.getColumnDimension()][];
		
		for (int i=0; i<outTr.length; i++)
			outTr[i] = normxcor(x, Y.getColumn(i), d);
		
		return new MyMatrix(outTr).transpose();
		
	}
	private static double [] normxcor(double []x, double [] y) throws Exception {
		return normxcor(x, y, 0);
	}
	/**
	 * % Calculate the normalized cross correlation of column vectors x and y
			% we can calculate this in two ways but fft is much faster even for nx small
			% We must have nx<=ny and the output length is ny-nx+1
			% note that this routine does not do mean subtraction even though this is normally a good idea
			% if y is a matrix, we correlate with each column
			% d is a constant added onto the normalization factor
			% v(j)=x'*yj/sqrt(d + x'*x * yj'*yj) where yj=y(j:j+nx-1) for j=1:ny-nx+1
	 * @param x
	 * @param y
	 * @param d
	 * @return
	 * @throws Exception 
	 */
	private static double [] normxcor(double [] x, double [] y, double d) throws Exception {

		int nx = x.length;
		int ny = y.length;
		//int my = 1; // [ny,my]=size(y);
		int nv=1+ny-nx;
		if (nx>ny)
			throw new Exception("second argument is shorter than the first");

		int nf = (int) Math.pow(2, MyMath.nextpow2(ny)); //nf=pow2(nextpow2(ny));

		MyComplexArrayFloat _t1 = Rfft.exe(x,nf).conjSelf();
		MyComplexArrayFloat _t2 = Rfft.exe(y, nf);
		MyComplexArrayFloat _t3 = _t1.arrayTimes(_t2);

		float[] w = Irfft.exe(_t1.arrayTimes(_t2));  //w=irfft(repmat(conj(rfft(x,nf,1)),1,my).*rfft(y,nf,1));
		
		//s=zeros(ny+1,my);
		//s(2:end,:)=cumsum(y.^2,1);
		double [] s = new double[ny+1];
		s[0] = 0;
		s[1] = y[0]*y[0];
		for (int i=2; i<s.length; i++)
			s[i] = s[i-1] + y[i-1]*y[i-1];

		//v = w(1:nv,:)./sqrt(d+(x'*x).*(s(nx+1:end,:)-s(1:end-nx,:)));
		double xx = MyMath.dot(x, x);
		double [] v = new double [nv];
		for (int i=0; i<nv; i++)
			v[i] = w[i] / Math.sqrt(d + xx*(s[nx+i] - s[i])); 

		return v;
	}

}
