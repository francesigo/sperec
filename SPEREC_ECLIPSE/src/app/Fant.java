package app;

import java.util.Arrays;
import java.util.Random;

public class Fant {
	
	public static class PARAMETER	{
		public char  []input_list = null;
		public char  []output_list = null;
		public char  []noise_file = null;
		public char  []index_list = null;
		public int    filter_type = NONE;
		public float  norm_level = NONE;
		public float  snr = NONE;
		public float  snr_range;
		public int    seed = -1;
		public char  []log_file = null;
		public int    mode = 0;
		
		public PARAMETER(String[] args) throws Exception { // tratto da anal_comline
			int argc = args.length;
			int i=0;
			String p = null;
			//es .{"-u", "-m", "snr_8khz", "-d", "-s", "6.0", "-l", "-20.0"};
			// Inoltre -n, ma senza noise file, perch gli do i noise samples
			while(i<argc)
			{
				p = args[i++];
				switch(p) {
				//case "-n" questo lo gestiso al di fuori
				case "-l":
					this.norm_level = Float.parseFloat(args[i++]);
					this.mode = this.mode | NORM;
					break;
				case "-s":
					this.snr = Float.parseFloat(args[i++]);
					break;
				case "-u":
					this.mode = this.mode | SAMP16K;
					break;
				case "-d":
					this.mode = this.mode | DC_COMP;
					break;
					
				case "-m":
					String optarg = args[i++];
					if (optarg=="snr_4khz")
						this.mode = this.mode | SNR_4khz;
					else if (optarg=="snr_8khz")
						this.mode = this.mode | SNR_8khz;
					else if (optarg=="a_weight")
						this.mode = this.mode | A_WEIGHT;
					else
						throw new Exception("Opzione -m non valida");
					
					break;

				}
			}
			
			if (((this.mode & SAMP16K)!=0) && (this.filter_type == P341))
			{
				this.filter_type = P341_16K;
			}
		
		}
	} // End of class PARAMETER
		
	
	static final int NONE   = 9999;
	static final int FILTER = 0x1;
	static final int NORM   = 0x2;
	static final int ADD    = 0x4;
	static final int SAMP16K    = 0x8;
	static final int SNRANGE    = 0x10;
	static final int SNR_4khz   = 0x20;
	static final int SNR_8khz   = 0x40;
	static final int DC_COMP    = 0x80;
	static final int IND_LIST   = 0x100;
	static final int A_WEIGHT   = 0x200;

	static final int P341_FILTER_SHIFT  = 125;
	static final int IRS_FILTER_SHIFT    = 75;
	static final int MIRS_FILTER_SHIFT  = 182;
	static final int P341_16K_FILTER_SHIFT  = 296;
	

	static final int G712 = 0;
	static final int P341 = 1;
	static final int IRS = 2;
	static final int MIRS = 3;
	static final int G712_16K = 4;
	static final int P341_16K = 5;
	static final int DOWN = 6;

	
	
	/**
	 * 
	 * @param speech: float array of speech samples
	 * @param noise: float array of noise samples
	 * @param pars
	 * @throws Exception 
	 */
	public static float [] filter_add_noise(float [] speech, float [] noise, String[] args) throws Exception {
		
		
		float noise_g712[]=null, noise_buf[]=null;
		int i, no, start; //long;

		
		int no_noise_samples = noise.length;
		final int no_speech_samples = speech.length;
		double      speech_level, noise_level, factor, fmax, snr;
		int         count;
		
		PARAMETER pars = new PARAMETER(args);
		if (noise!=null)
			pars.mode = pars.mode | ADD; // Anzichè dargli il noise file nella command line...
			
		SVP56_state volt_state = new SVP56_state();
		
		Random generator; // Java , per i random
		
		// Copio i campioni iniziali
		float [] orig_speech = new float[speech.length];
		System.arraycopy( speech, 0, orig_speech, 0, speech.length );
				
				
		noise_g712 = noise;
		
		if ((pars.mode & SAMP16K)!=0)  /*  16 kHz data  */
		{
		    if ((pars.mode & SNR_4khz)!=0)  /*  full 4 kHz bandwidth for calculating noise level N  */
		    {
			filter_samples(noise_g712, no_noise_samples, DOWN);  /*  downsampling  16 --> 8 kHz  */
			if ((pars.mode & DC_COMP)!=0)
				DCOffsetFil(noise_g712, no_noise_samples/2, 8000);
		    }
		    else if ((pars.mode & A_WEIGHT)!=0)
		    {
		        AWeightFil(noise_g712, no_noise_samples, 16000);
		    }
		    else if((pars.mode & SNR_8khz)!=0)
		    {
			if ((pars.mode & DC_COMP)!=0)
				DCOffsetFil(noise_g712, no_noise_samples, 16000);
		    }
		    else
		    {
			filter_samples(noise_g712, no_noise_samples, G712_16K);  /*  G.712 filtering of 16 K data  */
			if ((pars.mode & DC_COMP)!=0)
				DCOffsetFil(noise_g712, no_noise_samples/2, 8000);
		    }
		}
		else  /*  8 Khz data  */
		{
		    if ((pars.mode & A_WEIGHT)!=0)  /* filtering with A-weighting curve */
			AWeightFil(noise_g712, no_noise_samples, 8000);
		    else if ((pars.mode & SNR_4khz)==0)  /* If NOT full 4 kHz bandwidth --> G.712 filtering  */
			filter_samples(noise_g712, no_noise_samples, G712);
		    if ( ((pars.mode & DC_COMP)!=0) && ((pars.mode & A_WEIGHT)==0) )
		    	DCOffsetFil(noise_g712, no_noise_samples, 8000);
		 }
		
		
		/* filter noise signal in buffer "noise" */
		if ((pars.mode & FILTER)!=0)
		{
			filter_samples(noise, no_noise_samples, pars.filter_type);
			//fprintf(fp_log, " Noise signal filtered\n");
		}
		if (pars.seed == -1)
		{
			generator = new Random(System.currentTimeMillis());//srand((unsigned int) time(NULL));
			//fprintf(fp_log, " random seed (actual time) for the extraction of the noise segment\n");
		}
		else
		{
			generator = new Random(pars.seed);//srand(pars.seed);
			//fprintf(fp_log, " seed for the extraction of the noise segment: %d\n", pars.seed);
		}
		
		
		/* load samples of speech signal for calculating speech level S */
		//no_speech_samples = flen(fp_speech)/2;
		//speech = load_samples(fp_speech, no_speech_samples);
		
		if ((pars.mode & SAMP16K)!=0)  /*  16 kHz data  */
		{
		    if ((pars.mode & SNR_4khz)!=0)  /*  full 4 kHz bandwidth for calculating speech level S  */
		    {
		    	filter_samples(speech, no_speech_samples, DOWN);  /*  downsampling  16 --> 8 kHz  */
		    }
		    else if ((pars.mode & A_WEIGHT)!=0)
		    {
		        AWeightFil(speech, no_speech_samples, 16000);
		    }
		    else if ((pars.mode & SNR_8khz)!=0)
		    {
		    }
		    else
		    {
		  	filter_samples(speech, no_speech_samples, G712_16K); /*  G.712 filtering of 16 K data  */
		    }
		    if ((pars.mode & SNR_8khz)!=0)  /* FULL 8 kHz bandwidth */
		    {
		    	volt_state.init_speech_voltmeter(16000); // init_speech_voltmeter(&volt_state, 16000.);
		    	if ((pars.mode & DC_COMP)!=0)
		    		DCOffsetFil(speech, no_speech_samples, 16000);
			
		    	speech_level = volt_state.speech_voltmeter(speech, no_speech_samples);
		    }
		    else
		    {
		    	volt_state.init_speech_voltmeter((float) 8000.);
		    	if ( ((pars.mode & DC_COMP)!=0) && ((pars.mode & A_WEIGHT)==0) )
		    		DCOffsetFil(speech, no_speech_samples/2, 8000);
		    	
		    	speech_level = volt_state.speech_voltmeter(speech, no_speech_samples/2);
		    }
		}
		else  /*  8 kHz data  */
		{
		    if ((pars.mode & A_WEIGHT)!=0)  /* filtering with A-weighting curve */
		    	AWeightFil(speech, no_speech_samples, 8000);
		    
		    else if ((pars.mode & SNR_4khz)==0)  /* If NOT full 4 kHz bandwidth --> G.712 filtering  */
		    	filter_samples(speech, no_speech_samples, G712);
		    
		    volt_state.init_speech_voltmeter((float) 8000.);
		    if ( ((pars.mode & DC_COMP)!=0) && ((pars.mode & A_WEIGHT)==0) )
		    	DCOffsetFil(speech, no_speech_samples, 8000);
		    
		    speech_level = volt_state.speech_voltmeter(speech, no_speech_samples);
		}

		/*count = 0;
		for (i= (strlen(filename) - 1); i>= 0; i--)
			if (filename[i] == '/')
			{
				count++;
				if (count == 2) break;
			}
		fprintf(fp_log, " file:%s  s-level:%6.2f  ", &filename[i+1], speech_level);
		free(speech);
		 */

		/* load samples of speech signal again */
		//speech = load_samples(fp_speech, no_speech_samples);
		// No, prendo la copia che avevo fatto prima
		speech = orig_speech;

		/* filter speech signal */
		if ((pars.mode & FILTER)!=0)
		{
		    filter_samples(speech, no_speech_samples, pars.filter_type);
		}

		/* normalize level of speech signal to desired level  */
		if ((pars.mode & NORM)!=0)
		{
			factor = Math.pow(10., (pars.norm_level - speech_level)/20.);
			scale(speech, no_speech_samples, factor);
			speech_level = pars.norm_level;
		}

		if ((pars.mode & ADD)!=0)  /*  Noise adding  */
		{
			noise_buf = new float[no_speech_samples];
			Arrays.fill(noise_buf, 0);
			/*if ( ( noise_buf = (float*)calloc((size_t)no_speech_samples, sizeof(float))) == NULL)
			{
				fprintf(stderr, "cannot allocate enough memory to buffer noise samples!\n");
				exit(-1);
			}
			*/
			if (no_noise_samples > no_speech_samples)  /* noise signal longer than speech signal */
			{
				/* select segment randomly out of noise signal */
				if ((pars.mode & IND_LIST)!=0)
				{
					throw new Exception("TODO: pars.mode & IND_LIST");
				   /*if ( fscanf(fp_index, "%ld", &start) == EOF)
				   {
					fprintf(stderr, "\nInsufficient number of indices defined in index list file!\n");
					exit(-1);
				   }*/
				}
				else {
				   //start = (long) ( (double)(rand())/(RAND_MAX) * (double)(no_noise_samples - no_speech_samples));
					start = (int)(generator.nextDouble() * (double)(no_noise_samples - no_speech_samples));
				}

				//fprintf(fp_log, "1st noise sample:%ld  ", start);

				/* calculate noise level of selected segment  */
				if ((pars.mode & SAMP16K)!=0)  /*  16 kHz data  */
				{
					if ((pars.mode & SNR_8khz)!=0)  /* calculate noise level from 16 kHz data  */
		    		{
						System.arraycopy(noise_g712, start, noise_buf, 0, no_speech_samples); //memcpy(noise_buf, &noise_g712[start], (size_t)(no_speech_samples*sizeof(float)));
						volt_state.init_speech_voltmeter((float)16000.);
						volt_state.speech_voltmeter(noise_buf, no_speech_samples);
		    		}
		    		else  /* calculate noise level from downsampled 8 kHz data  */
		    		{
		    			System.arraycopy(noise_g712, start/2, noise_buf, 0, no_speech_samples/2); //memcpy(noise_buf, &noise_g712[start/2], (size_t)(no_speech_samples/2*sizeof(float)));
		    			volt_state.init_speech_voltmeter((float)8000.);
		    			volt_state.speech_voltmeter(noise_buf, no_speech_samples/2);
				    }
				}
				else  /*  8 kHz data  */
				{
				   System.arraycopy(noise_g712, start, noise_buf, 0, no_speech_samples); //memcpy(noise_buf, &noise_g712[start], (size_t)(no_speech_samples*sizeof(float)));
				   volt_state.init_speech_voltmeter((float)8000.);
				   volt_state.speech_voltmeter(noise_buf, no_speech_samples);
				}

				noise_level = volt_state.get_rms_dB(); //SVP56_get_rms_dB(volt_state);
				//fprintf(fp_log, "n-level:%6.2f", noise_level);
				System.arraycopy(noise,  start,  noise_buf,  0, no_speech_samples); //memcpy(noise_buf, &noise[start], (size_t)(no_speech_samples*sizeof(float)));
			}
			else /* speech signal longer than noise signal */
			     /* use noise signal several times by starting with the 1st sample again at the end  */
			{
				no = 0;
				if ((pars.mode & SAMP16K)!=0)  /*  16 kHz data  */
				{
				  if ((pars.mode & SNR_8khz)!=0)  /* FULL 8 kHz bandwidth  */
				  {
				     while (no < no_speech_samples)
				     {
						 if ((no_speech_samples-no) > no_noise_samples)
						 {
							System.arraycopy(noise_g712, 0, noise_buf, no, no_noise_samples); //memcpy(&noise_buf[no], noise_g712,(size_t)(no_noise_samples*sizeof(float)));
							no += no_noise_samples;
						 }
						 else
						 {
							System.arraycopy(noise_g712, 0, noise_buf, no, no_speech_samples-no);//memcpy(&noise_buf[no], noise_g712,(size_t)((no_speech_samples-no)*sizeof(float)));
							no = no_speech_samples;
						 }
				     }
				     volt_state.init_speech_voltmeter((float)16000.);
				     volt_state.speech_voltmeter(noise_buf, no_speech_samples);
				  }
				  else  /* in case of 4 kHz bandwidth with or without G.712 filtering  */
				        /* process downsampled version of noise signal */
				  {
				    while (no < no_speech_samples/2)
				    {
					if ((no_speech_samples/2-no) > no_noise_samples/2)
					{
						System.arraycopy(noise_g712, 0, noise_buf, no, no_noise_samples/2); //memcpy(&noise_buf[no], noise_g712,(size_t)(no_noise_samples/2*sizeof(float)));
						no += no_noise_samples/2;
					}
					else
					{
						System.arraycopy(noise_g712, 0, noise_buf, no, no_speech_samples/2);//memcpy(&noise_buf[no], noise_g712,(size_t)((no_speech_samples/2-no)*sizeof(float)));
						no = no_speech_samples/2;
					}
				    }
				    volt_state.init_speech_voltmeter( 8000.f);
				    volt_state.speech_voltmeter(noise_buf, no_speech_samples/2);
				  }
				}
				else  /*  8 kHz data  */
				{
				  while (no < no_speech_samples)
				  {
					if ((no_speech_samples-no) > no_noise_samples)
					{
						System.arraycopy(noise_g712, 0, noise_buf, no, no_noise_samples);//memcpy(&noise_buf[no], noise_g712,(size_t)(no_noise_samples*sizeof(float)));
						no += no_noise_samples;
					}
					else
					{
						System.arraycopy(noise_g712, 0, noise_buf, no, no_speech_samples-no); //memcpy(&noise_buf[no], noise_g712,(size_t)((no_speech_samples-no)*sizeof(float)));
						no = no_speech_samples;
					}
				  }
				  volt_state.init_speech_voltmeter(8000.f);
				  volt_state.speech_voltmeter(noise_buf, no_speech_samples);
				}

				noise_level = volt_state.get_rms_dB(); //SVP56_get_rms_dB(volt_state);
				//fprintf(fp_log, "noise too short! n-level:%6.2f", noise_level);
				no = 0;
				while (no < no_speech_samples)
				{
					if ((no_speech_samples-no) > no_noise_samples)
					{
						System.arraycopy(noise, 0, noise_buf, no, no_noise_samples);//memcpy(&noise_buf[no], noise, (size_t)(no_noise_samples*sizeof(float)));
						no += no_noise_samples;
					}
					else
					{
						System.arraycopy(noise, 0, noise_buf, no, no_speech_samples-no);//memcpy(&noise_buf[no], noise, (size_t)((no_speech_samples-no)*sizeof(float)));
						no = no_speech_samples;
					}
				}
			}
			if ((pars.mode & SNRANGE)!=0)
			{ 
			  //snr = (double)pars.snr + ( (double)(rand())/(double)(RAND_MAX) * (double)(pars.snr_range) );
				snr = (double)pars.snr + (generator.nextDouble() * (double)(pars.snr_range));
			  //fprintf(fp_log, "  SNR:%f", snr);
			}
			else
			  snr = pars.snr;
			factor = Math.pow(10., ((speech_level - snr) - noise_level)/20.);
			scale(noise_buf, no_speech_samples, factor);
			/*fmax = 0.; */
			for (i=0; i<no_speech_samples; i++)
			{
				speech[i] += noise_buf[i];
				/*if (fabs((double)speech[i]) > fmax)
					fmax = fabs((double)speech[i]);*/
			}
			/*if (fmax > 1.)
			{
				fprintf(fp_log, "\n ATTENTION!!! overload: %6.2f", fmax);
				for (i=0; i<no_speech_samples; i++)
					speech[i] /= (float)fmax;
			} */
			
			//free(noise_buf);
		}
		/* The overload check has been moved here!
		   Now the check is also done in case of a level normalization only! */
		fmax = 0.;
		for (i=0; i<no_speech_samples; i++)
		{
			if (Math.abs((double)speech[i]) > fmax)
				fmax = Math.abs((double)speech[i]);  
		}
		if (fmax > 1.)
		{
			//fprintf(fp_log, "\n ATTENTION!!! overload by factor %6.2f", fmax);
			System.out.println("ATTENTION!!! overload by factor " + fmax);
			for (i=0; i<no_speech_samples; i++)
				speech[i] /= (float)fmax;
			if ((pars.mode & NORM)!=0)
			{
				System.out.println("ATTENTION !!!\n" );
				System.out.println("Due to overload the speech level could only be normalized to " + (pars.norm_level - 20*Math.log10(fmax)));
				//fprintf(fp_log, "\n Due to overload the speech level could only be normalized to %6.2f", pars.norm_level - 20*log10(fmax));
			}
		} 
		
		// NON VOGLIO CHE VENGANO SCRITTI SU FILE
		//write_samples(speech, no_speech_samples, out_filename);
		
		//free(speech);
		//fclose(fp_speech);
		//fprintf(fp_log, "\n");
	
	
	//fprintf(fp_log," --------------------------------------------------------------------------\n\n");
	//fclose(fp_log);
		
		return speech; // Nota che questo puntatore non  è più quello in input, qui va ritornato alla funzione chiamante
	}
	
	
	
	//static void filter_samples(float []signal, long no_samples, int type)
	static void filter_samples(float []signal, int no_samples, int type)
	{
	    CASCADE_IIR g712_state=null;
		SCD_FIR     p341_state, irs_state, mirs_state, up_ptr, down_ptr;
		float  []buf;
		float []buf1;
		float []buf2;
		float []signal_buf;
		
		long    no=0;
		int filter_shift=0; //long
		
	    switch(type)
		{
		  case G712:
		  case G712_16K:
			filter_shift = 0;
			break;
		  case P341:
			filter_shift = P341_FILTER_SHIFT;
			break;
		  case IRS:
			filter_shift = IRS_FILTER_SHIFT;
			break;
		  case MIRS:
			filter_shift = MIRS_FILTER_SHIFT;
			break;
		  case P341_16K:
			filter_shift = P341_16K_FILTER_SHIFT;
			break;
		}
		
	    buf = new float[no_samples+filter_shift];
	    Arrays.fill(buf,  0);
	    /*
		if ( ( buf = (float*)calloc((size_t)(no_samples+filter_shift), sizeof(float))) == NULL)
		{
			fprintf(stderr, "cannot allocate enough memory to filter samples!\n");
			exit(-1);
		}
		*/
	    signal_buf = new float[no_samples+filter_shift];
	    Arrays.fill(signal_buf,  0);
	    /*
		if ( ( signal_buf = (float*)calloc((size_t)(no_samples+filter_shift), sizeof(float))) == NULL)
		{
			fprintf(stderr, "cannot reallocate enough memory to filter samples!\n");
			exit(-1);
		}
		*/
	    System.arraycopy(signal, 0, signal_buf, 0, no_samples); //memcpy(signal_buf, signal, (size_t)(no_samples*sizeof(float)));
		switch(type)
		{
		  case G712:
			g712_state = iir_G712_8khz_init();
			no = g712_state.cascade_iir_kernel((no_samples+filter_shift), signal_buf, buf);//no = cascade_iir_kernel((no_samples+filter_shift), signal_buf, g712_state, buf);
			g712_state = null;; //cascade_iir_free(g712_state);
			/* next lines only for testing the A weighting filter
			AWeightFil(signal_buf, no_samples, 16000);
			no = no_samples;
			memcpy(buf, signal_buf, (size_t)(no_samples*sizeof(float))); */
			break;
		  /*Sigona 
		   * case P341:
			p341_state = fir_hp_8khz_init();
			no = hq_kernel((no_samples+filter_shift), signal_buf, p341_state, buf);
			hq_free(p341_state);
			break;
		  case IRS:
			irs_state = irs_8khz_init();
			no = hq_kernel((no_samples+filter_shift), signal_buf, irs_state, buf);
			hq_free(irs_state);
			break;
		  case MIRS:
			mirs_state = mod_irs_16khz_init();
			up_ptr = hq_up_1_to_2_init();
			down_ptr = hq_down_2_to_1_init();
			if ( ( buf1 = (float*)calloc((size_t)2*(no_samples+filter_shift), sizeof(float))) == NULL)
			{
				fprintf(stderr, "cannot allocate enough memory to filter samples!\n");
				exit(-1);
			}
			if ( ( buf2 = (float*)calloc((size_t)2*(no_samples+filter_shift), sizeof(float))) == NULL)
			{
				fprintf(stderr, "cannot allocate enough memory to filter samples!\n");
				exit(-1);
			}
			no = hq_kernel((no_samples+filter_shift), signal_buf, up_ptr, buf1);
			no = hq_kernel(2 * (no_samples+filter_shift), buf1, mirs_state, buf2);
			no = hq_kernel(2 * (no_samples+filter_shift), buf2, down_ptr, buf);
			hq_free (up_ptr);
			hq_free (mirs_state);
			hq_free (down_ptr);
			free(buf1);
			free(buf2);
			break;
			
		  case G712_16K:
			g712_state = iir_G712_8khz_init();
			down_ptr = hq_down_2_to_1_init();
			if ( ( buf1 = (float*)calloc((size_t)((no_samples+1)/2+filter_shift), sizeof(float))) == NULL)
			{
				fprintf(stderr, "cannot allocate enough memory to filter samples!\n");
				exit(-1);
			}
			no = hq_kernel((no_samples+filter_shift), signal_buf, down_ptr, buf1);
			no_samples /= 2;
			no = cascade_iir_kernel((no_samples+filter_shift), buf1, g712_state, buf);
			cascade_iir_free(g712_state);
			hq_free (down_ptr);
			free(buf1);
			break;
		  case P341_16K:
			p341_state = p341_16khz_init();
			no = hq_kernel((no_samples+filter_shift), signal_buf, p341_state, buf);
			hq_free(p341_state);
			break;
		  case DOWN:
			down_ptr = hq_down_2_to_1_init();
			no = hq_kernel((no_samples+filter_shift), signal_buf, down_ptr, buf);
			no_samples /= 2;
			hq_free (down_ptr);
			break;
			*/
		}
		if (no != (no_samples+filter_shift))
			System.out.println("Number of samples at output of filtering NOT equal to number of input samples!");
		
		System.arraycopy(buf, filter_shift, signal, 0, no_samples);//memcpy(signal, &buf[filter_shift], (size_t)(no_samples*sizeof(float)));
		buf = null; //free(buf);
		signal_buf = null; //free(signal_buf);
	}

	
	/***  DC offset compensation filtering  ***/
	static void DCOffsetFil(float []signal, long no_samples, int samp_freq)
	{
	  int i;
	  float aux, prev_x, prev_y, coeff;

	  if (samp_freq == 8000)
	  {
	  	/* y[n] = x[n] - x[n-1] + 0.999 * y[n-1] */
		coeff = (float) 0.999;
	  }
	  else
	  {
	  	/* y[n] = x[n] - x[n-1] + 0.9995 * y[n-1] */
		coeff = (float) 0.9995;
	  }

	  prev_x = (float) 0.0;
	  prev_y = (float) 0.0;
	  for (i=0 ; i<no_samples ; i++)
	        {
	          aux = signal[i];
	          signal[i] = (float) (signal[i] - prev_x + 0.999 * prev_y);
	          prev_x = aux;
	          prev_y = signal[i];
	        }
	}

	
	
	
	/***  A weighting filter  ***/
	/* The filter characteristic of the A-weighting is realized as
	   a combination of a 2nd order IIR HP filter and a FIR filter.
	   The filters have been designed with MATLAB to match
	   Ra(f) = 12200^2*f^4 / ( (f^2+20.6^2)*(f^2+12200^2)*sqrt(f^2+107.7^2)*sqrt(f^2+737.9^2) )
	*/
	static void AWeightFil(float []signal, int no_samples, int samp_freq) //long no_samples
	{
	  //long i, j;
	  int i, j;
	  int nrfircoef, nr2;
	  double aux, prev_x1, prev_y1, prev_x2, prev_y2, sig;
	  //double *b, *b1, *a1, *buf;
	  double b[];
	  double b1[];
	  double a1[];
	  double buf[];
	/* coefficients of HP IIR filter for 8 kHz */
	double b1_8[] = { 0.97803047920655972192, -1.95606095841311944383,  0.97803047920655972192};
	double a1_8[] = { 1.00000000000000000000, -1.95557824031503546536,  0.95654367651120331129};
	/* coefficients of HP IIR filter for 16 kHz */
	double b1_16[] = { 0.98211268665798745481, -1.96422537331597490962,  0.98211268665798745481};
	double a1_16[] = { 1.00000000000000000000, -1.96390539174032729974,  0.96454535489162229744};
	/* coefficients of FIR filter for 8 kHz */
	double b_8[] = {     //401
		-0.00000048447483696946, -0.00000022512318749614, -0.00000026294025838101, 
		 0.00000001064770950293,  0.00000003833470677151,  0.00000051126078952589, 
		 0.00000069953309206104,  0.00000098541838241537,  0.00000081475746826278, 
		 0.00000071268849316663,  0.00000047296424495409,  0.00000045167560549269, 
		 0.00000025625324551665,  0.00000029659517075128,  0.00000012973089907994, 
		 0.00000021259865101836,  0.00000006731555126460,  0.00000020685105369918, 
		 0.00000011425817192679,  0.00000041296073931891,  0.00000044791493633252, 
		 0.00000070403634084571,  0.00000055460203484093,  0.00000059180030971632, 
		 0.00000037291369217390,  0.00000043930757040800,  0.00000022350428357547, 
		 0.00000032417428619399,  0.00000010931231083113,  0.00000023960656238246, 
		 0.00000002518603320581,  0.00000020360644134332,  0.00000002294875525823, 
		 0.00000036684242335179,  0.00000032206388036039,  0.00000067239882378009, 
		 0.00000047076577332115,  0.00000062355204864449,  0.00000033329059818567, 
		 0.00000049980041816919,  0.00000019054258683724,  0.00000038619575844507, 
		 0.00000005987624410169,  0.00000028352923650070, -0.00000006065134972245, 
		 0.00000021066197086678, -0.00000011559068267007,  0.00000035187682882953, 
		 0.00000018911184032816,  0.00000071185755567156,  0.00000039628928325198, 
		 0.00000071727603322036,  0.00000028502865756631,  0.00000061047935453455, 
		 0.00000013533215782697,  0.00000048963932090098, -0.00000002412770048109, 
		 0.00000035905116933378, -0.00000019714163211650,  0.00000023343303301514, 
		-0.00000032883174661595,  0.00000034112098540144, -0.00000002013183789564, 
		 0.00000076876433367610,  0.00000025451747786098,  0.00000083392066985720, 
		 0.00000016702345929555,  0.00000074451146044984,  0.00000000368726942094, 
		 0.00000061262145802736, -0.00000019658915230727,  0.00000044360091722352, 
		-0.00000044388652434922,  0.00000024103770006125, -0.00000069300296642645, 
		 0.00000028299231855406, -0.00000040858734051716,  0.00000077228655642159, 
		-0.00000007206267809928,  0.00000089818974605587, -0.00000014399904288098, 
		 0.00000082277817797847, -0.00000033588376942978,  0.00000066948742873521, 
		-0.00000060057886334042,  0.00000044146432002551, -0.00000096039560383583, 
		 0.00000012053414322732, -0.00000139567180432868,  0.00000003499817058916, 
		-0.00000120127431401377,  0.00000054929811663290, -0.00000084136392860322, 
		 0.00000071187362632263, -0.00000093656445044485,  0.00000062086550172766, 
		-0.00000120620094216959,  0.00000040541476845153, -0.00000159893445870286, 
		 0.00000006116939341500, -0.00000215817883874979, -0.00000046805016014766, 
		-0.00000291567502959354, -0.00000081034476310266, -0.00000295972826281522, 
		-0.00000038208445373341, -0.00000270158074324256, -0.00000028610530331721, 
		-0.00000295437553011978, -0.00000051213557187288, -0.00000346084232168490, 
		-0.00000093544688046448, -0.00000417377051206647, -0.00000157785035711933, 
		-0.00000517283917494427, -0.00000256013415986944, -0.00000658452041834079, 
		-0.00000348617114338415, -0.00000725051504153352, -0.00000348145442182330, 
		-0.00000748452095899366, -0.00000381670662519333, -0.00000833912676894361, 
		-0.00000460581537897684, -0.00000960803218461066, -0.00000575052259069264, 
		-0.00001126971690354723, -0.00000731471724310772, -0.00001347382962085700, 
		-0.00000953631917028709, -0.00001652423778949483, -0.00001204562230261030, 
		-0.00001898108292544097, -0.00001359523368806706, -0.00002102023713909460, 
		-0.00001567362600146969, -0.00002402916745522739, -0.00001859827094821024, 
		-0.00002792481555180809, -0.00002236632163891675, -0.00003278682614223167, 
		-0.00002716785941170217, -0.00003894078939793811, -0.00003350000474485616, 
		-0.00004707261529101495, -0.00004117953534261129, -0.00005549229201502203, 
		-0.00004854831975713259, -0.00006428602304202158, -0.00005751749178242488, 
		-0.00007547082178730966, -0.00006888306835026006, -0.00008937608730225377, 
		-0.00008304065366192661, -0.00010653315756608934, -0.00010069556565277376, 
		-0.00012791706451173652, -0.00012317597326295306, -0.00015534957920904628, 
		-0.00015119559113842406, -0.00018743511243768355, -0.00018315735451190256, 
		-0.00022492450567979631, -0.00022252894483161461, -0.00027191753804259159, 
		-0.00027210151946550513, -0.00033091855632192026, -0.00033462449100590694, 
		-0.00040528057920629279, -0.00041401099044859757, -0.00049996344641314633, 
		-0.00051634251110413301, -0.00062296124512021081, -0.00064870975228481043, 
		-0.00077929439175705522, -0.00081642092721146428, -0.00097961081175696862, 
		-0.00103615592056275404, -0.00124532298732607061, -0.00133067510583451858, 
		-0.00160455574020486129, -0.00173377904846212577, -0.00210208956880716599, 
		-0.00230122036337402887, -0.00281419951501742224, -0.00313155247940679650, 
		-0.00388062211113633822, -0.00440501290142665362, -0.00555548659253778630, 
		-0.00647018812272768650, -0.00837591414127070166, -0.01010231987300322896, 
		-0.01356061827384147031, -0.01708437456947442534, -0.02402258013383605159, 
		-0.03176179634779018046, -0.04725099052436917274, -0.06500419192239298427, 
		-0.10495713406978124382, -0.13001585049351535583,  1.00202934757439754421, 
		-0.13001585049351260803, -0.10495713406978261772, -0.06500419192239276223, 
		-0.04725099052436897151, -0.03176179634779023597, -0.02402258013383685303, 
		-0.01708437456947343655, -0.01356061827384207226, -0.01010231987300251599, 
		-0.00837591414127101912, -0.00647018812272795105, -0.00555548659253785135, 
		-0.00440501290142657816, -0.00388062211113645011, -0.00313155247940646083, 
		-0.00281419951501782339, -0.00230122036337384803, -0.00210208956880718334, 
		-0.00173377904846190763, -0.00160455574020492830, -0.00133067510583440777, 
		-0.00124532298732645832, -0.00103615592056261765, -0.00097961081175704668, 
		-0.00081642092721146472, -0.00077929439175693650, -0.00064870975228487461, 
		-0.00062296124512023910, -0.00051634251110382412, -0.00049996344641339472, 
		-0.00041401099044828212, -0.00040528057920655793, -0.00033462449100599172, 
		-0.00033091855632189261, -0.00027210151946566391, -0.00027191753804250599, 
		-0.00022252894483150621, -0.00022492450567991013, -0.00018315735451169751, 
		-0.00018743511243781582, -0.00015119559113829513, -0.00015534957920906756, 
		-0.00012317597326292961, -0.00012791706451191086, -0.00010069556565275803, 
		-0.00010653315756613178, -0.00008304065366182717, -0.00008937608730207478, 
		-0.00006888306835033379, -0.00007547082178731399, -0.00005751749178236594, 
		-0.00006428602304224104, -0.00004854831975686155, -0.00005549229201534046, 
		-0.00004117953534267964, -0.00004707261529099294, -0.00003350000474499829, 
		-0.00003894078939759813, -0.00002716785941168689, -0.00003278682614236486, 
		-0.00002236632163880632, -0.00002792481555207722, -0.00001859827094805007, 
		-0.00002402916745519642, -0.00001567362600153142, -0.00002102023713907599, 
		-0.00001359523368790508, -0.00001898108292530993, -0.00001204562230221977, 
		-0.00001652423778936245, -0.00000953631917073583, -0.00001347382962091341, 
		-0.00000731471724375662, -0.00001126971690382800, -0.00000575052259002190, 
		-0.00000960803218588184, -0.00000460581537765319, -0.00000833912676879278, 
		-0.00000381670662386180, -0.00000748452096203882, -0.00000348145442048370, 
		-0.00000725051504105103, -0.00000348617114306444, -0.00000658452041804100, 
		-0.00000256013416030796, -0.00000517283917536835, -0.00000157785035723070, 
		-0.00000417377051229723, -0.00000093544688056833, -0.00000346084232157659, 
		-0.00000051213557197883, -0.00000295437552986686, -0.00000028610530303880, 
		-0.00000270158074328942, -0.00000038208445346697, -0.00000295972826305478, 
		-0.00000081034476315829, -0.00000291567502957262, -0.00000046805016033679, 
		-0.00000215817883858096,  0.00000006116939340522, -0.00000159893445868161, 
		 0.00000040541476858004, -0.00000120620094225548,  0.00000062086550181404, 
		-0.00000093656445052059,  0.00000071187362625699, -0.00000084136392873181, 
		 0.00000054929811657256, -0.00000120127431400949,  0.00000003499817062538, 
		-0.00000139567180421291,  0.00000012053414316903, -0.00000096039560378374, 
		 0.00000044146432006458, -0.00000060057886339091,  0.00000066948742887569, 
		-0.00000033588376961814,  0.00000082277817799631, -0.00000014399904286968, 
		 0.00000089818974600899, -0.00000007206267791168,  0.00000077228655631520, 
		-0.00000040858734051298,  0.00000028299231857979, -0.00000069300296651602, 
		 0.00000024103770018627, -0.00000044388652449250,  0.00000044360091722030, 
		-0.00000019658915231848,  0.00000061262145802356,  0.00000000368726950959, 
		 0.00000074451146042521,  0.00000016702345934702,  0.00000083392066980561, 
		 0.00000025451747789236,  0.00000076876433370927, -0.00000002013183795176, 
		 0.00000034112098553517, -0.00000032883174674481,  0.00000023343303310152, 
		-0.00000019714163212011,  0.00000035905116921998, -0.00000002412770030070, 
		 0.00000048963932070069,  0.00000013533215787171,  0.00000061047935452411, 
		 0.00000028502865736597,  0.00000071727603351564,  0.00000039628928307606, 
		 0.00000071185755575744,  0.00000018911184033621,  0.00000035187682854291, 
		-0.00000011559068225173,  0.00000021066197066498, -0.00000006065134949705, 
		 0.00000028352923609170,  0.00000005987624482286,  0.00000038619575789876, 
		 0.00000019054258687088,  0.00000049980041800381,  0.00000033329059839068, 
		 0.00000062355204865072,  0.00000047076577331007,  0.00000067239882386183, 
		 0.00000032206388025069,  0.00000036684242346853,  0.00000002294875522055, 
		 0.00000020360644131705,  0.00000002518603326988,  0.00000023960656229525, 
		 0.00000010931231087879,  0.00000032417428617176,  0.00000022350428353128, 
		 0.00000043930757046438,  0.00000037291369211717,  0.00000059180030972943, 
		 0.00000055460203483238,  0.00000070403634081861,  0.00000044791493638141, 
		 0.00000041296073931221,  0.00000011425817192819,  0.00000020685105369570, 
		 0.00000006731555124485,  0.00000021259865104789,  0.00000012973089908160, 
		 0.00000029659517075261,  0.00000025625324549990,  0.00000045167560549383, 
		 0.00000047296424497843,  0.00000071268849317641,  0.00000081475746827296, 
		 0.00000098541838238358,  0.00000069953309205412,  0.00000051126078953913, 
		 0.00000003833470675649,  0.00000001064770952157, -0.00000026294025842014, 
		-0.00000022512318749586, -0.00000048447483693658 };
	/* coefficients of FIR filter for 16 kHz */
	double b_16[] = {  //301
		-0.00000163823566567235, -0.00000129349101568055, -0.00000173855867999297, 
		-0.00000138886083315020, -0.00000186074944599914, -0.00000150193139198946, 
		-0.00000200604496185638, -0.00000163127987422071, -0.00000217132557278059, 
		-0.00000176741808887155, -0.00000234382073006229, -0.00000183094344655654, 
		-0.00000250084283910075, -0.00000199864660566820, -0.00000262789002458754, 
		-0.00000215386043860660, -0.00000289217185109260, -0.00000239378523373080, 
		-0.00000322662206448115, -0.00000269434283679307, -0.00000362531450279619, 
		-0.00000305075217416252, -0.00000408817430627477, -0.00000346416689290217, 
		-0.00000461920770121783, -0.00000393932206526000, -0.00000522530308400259, 
		-0.00000448351321445257, -0.00000591564184061565, -0.00000510612699627384, 
		-0.00000670136346012773, -0.00000581822564278744, -0.00000759496947841621, 
		-0.00000663144143348438, -0.00000860841360320021, -0.00000755451505352295, 
		-0.00000974743477173025, -0.00000858432874211604, -0.00001099850081772816, 
		-0.00000951231180346016, -0.00001227514374058944, -0.00001069770708620096, 
		-0.00001351961693721276, -0.00001190082625789161, -0.00001508067422270440, 
		-0.00001334654683810277, -0.00001689161756572877, -0.00001502283369917000, 
		-0.00001895702883557266, -0.00001693675311730079, -0.00002129105878855550, 
		-0.00001910487054790810, -0.00002391661786573805, -0.00002155212349973529, 
		-0.00002686481719856254, -0.00002431145023492758, -0.00003017501508719200, 
		-0.00002742414172429308, -0.00003389545048083550, -0.00003094066284628364, 
		-0.00003808392712044369, -0.00003492107295288400, -0.00004280677257843587, 
		-0.00003943266482462276, -0.00004813086069643492, -0.00004454795915910790, 
		-0.00005411064105123142, -0.00004978108057728533, -0.00006053146558008195, 
		-0.00005591629066482250, -0.00006726077312005088, -0.00006247107842621700, 
		-0.00007501845211855596, -0.00006996429387079926, -0.00008383355250755138, 
		-0.00007848989593105642, -0.00009381300436270244, -0.00008815897211855582, 
		-0.00010508611888928092, -0.00009910600683043637, -0.00011781065580099466, 
		-0.00011149480231451605, -0.00013217771097594411, -0.00012552361433682034, 
		-0.00014841748019445592, -0.00014143186724447642, -0.00016680726333668429, 
		-0.00015950964206924115, -0.00018768220029857365, -0.00018011048605059599, 
		-0.00021144733901080876, -0.00020366775396491089, -0.00023858297765662684, 
		-0.00023076411910554420, -0.00026970431005075987, -0.00026068439401730485, 
		-0.00030470598160643134, -0.00029539153470893316, -0.00034372546700033225, 
		-0.00033441055096840278, -0.00038864335568094714, -0.00037937084585191045, 
		-0.00044046553493560527, -0.00043137871260839591, -0.00050044145280684585, 
		-0.00049173817484151965, -0.00057009787755619838, -0.00056205601515835267, 
		-0.00065134093406154082, -0.00064435205571055265, -0.00074658366829230961, 
		-0.00074120024680015531, -0.00085891975554849679, -0.00085592507999440650, 
		-0.00099237016459549198, -0.00099288457416451940, -0.00115224073327841446, 
		-0.00115788645226965956, -0.00134564572909820685, -0.00135881539184466910, 
		-0.00158226727446100215, -0.00160686124447576299, -0.00187589512010747717, 
		-0.00191361708126458496, -0.00224313077365659395, -0.00230285969530687976, 
		-0.00270869448954760448, -0.00280161130222293274, -0.00331284571947834715, 
		-0.00345545877593055615, -0.00411288499378428731, -0.00433074047660635814, 
		-0.00519546795915075323, -0.00552823600867017109, -0.00669413267342846511, 
		-0.00720362577421732476, -0.00881837437615939912, -0.00960052818708471110, 
		-0.01190264418816686619, -0.01310369434291633675, -0.01649112537915961921, 
		-0.01832242291243315474, -0.02349169549765109388, -0.02620808279888530226, 
		-0.03448966393581323620, -0.03812774396888992529, -0.05263240033584821315, 
		-0.05486415897428192912, -0.08877452476680040838, -0.01261484753501346777, 
		 1.00356872825050946751, -0.01261484753503031193, -0.08877452476679281723, 
		-0.05486415897428669614, -0.05263240033584432043, -0.03812774396889240247, 
		-0.03448966393581100881, -0.02620808279888764414, -0.02349169549764856813, 
		-0.01832242291243483048, -0.01649112537915836327, -0.01310369434291740708, 
		-0.01190264418816549055, -0.00960052818708617174, -0.00881837437615880758, 
		-0.00720362577421778446, -0.00669413267342735836, -0.00552823600867079299, 
		-0.00519546795915030307, -0.00433074047660710320, -0.00411288499378345464, 
		-0.00345545877593135715, -0.00331284571947772829, -0.00280161130222301470, 
		-0.00270869448954737420, -0.00230285969530739628, -0.00224313077365622315, 
		-0.00191361708126515785, -0.00187589512010717446, -0.00160686124447636755, 
		-0.00158226727446038416, -0.00135881539184473936, -0.00134564572909785601, 
		-0.00115788645226987423, -0.00115224073327792136, -0.00099288457416505543, 
		-0.00099237016459540590, -0.00085592507999461337, -0.00085891975554801476, 
		-0.00074120024680079542, -0.00074658366829181760, -0.00064435205571094784, 
		-0.00065134093406116699, -0.00056205601515879231, -0.00057009787755602219, 
		-0.00049173817484126367, -0.00050044145280631915, -0.00043137871260933070, 
		-0.00044046553493469183, -0.00037937084585278541, -0.00038864335568060160, 
		-0.00033441055096828900, -0.00034372546700017510, -0.00029539153470938310, 
		-0.00030470598160636098, -0.00026068439401741235, -0.00026970431005102376, 
		-0.00023076411910555691, -0.00023858297765633738, -0.00020366775396491997, 
		-0.00021144733901044975, -0.00018011048605068918, -0.00018768220029861783, 
		-0.00015950964206965665, -0.00016680726333593294, -0.00014143186724488663, 
		-0.00014841748019449227, -0.00012552361433689789, -0.00013217771097567499, 
		-0.00011149480231447122, -0.00011781065580081225, -0.00009910600683070414, 
		-0.00010508611888905710, -0.00008815897211896660, -0.00009381300436261302, 
		-0.00007848989593102117, -0.00008383355250745122, -0.00006996429387099301, 
		-0.00007501845211851302, -0.00006247107842627319, -0.00006726077311992514, 
		-0.00005591629066486161, -0.00006053146558001904, -0.00004978108057739705, 
		-0.00005411064105111847, -0.00004454795915914179, -0.00004813086069638520, 
		-0.00003943266482468589, -0.00004280677257856477, -0.00003492107295277574, 
		-0.00003808392712035809, -0.00003094066284633193, -0.00003389545048065900, 
		-0.00002742414172435651, -0.00003017501508728730, -0.00002431145023493965, 
		-0.00002686481719855991, -0.00002155212349960679, -0.00002391661786585488, 
		-0.00001910487054809171, -0.00002129105878838215, -0.00001693675311722188, 
		-0.00001895702883560177, -0.00001502283369927165, -0.00001689161756547775, 
		-0.00001334654683827559, -0.00001508067422280056, -0.00001190082625786406, 
		-0.00001351961693711764, -0.00001069770708634584, -0.00001227514374056167, 
		-0.00000951231180320429, -0.00001099850081810239, -0.00000858432874199897, 
		-0.00000974743477169181, -0.00000755451505353840, -0.00000860841360316040, 
		-0.00000663144143354042, -0.00000759496947838709, -0.00000581822564273562, 
		-0.00000670136346019077, -0.00000510612699626390, -0.00000591564184057631, 
		-0.00000448351321447595, -0.00000522530308401066, -0.00000393932206526943, 
		-0.00000461920770117965, -0.00000346416689293377, -0.00000408817430624457, 
		-0.00000305075217416854, -0.00000362531450277131, -0.00000269434283683277, 
		-0.00000322662206448276, -0.00000239378523369839, -0.00000289217185106983, 
		-0.00000215386043867678, -0.00000262789002454381, -0.00000199864660567825, 
		-0.00000250084283909178, -0.00000183094344656972, -0.00000234382073002803, 
		-0.00000176741808886073, -0.00000217132557281426, -0.00000163127987426894, 
		-0.00000200604496179164, -0.00000150193139199939, -0.00000186074944604085, 
		-0.00000138886083315537, -0.00000173855867992306, -0.00000129349101569237, 
		-0.00000163823566572743 };

	  if (samp_freq == 8000)
	  {
		b = b_8;
		b1 = b1_8;
		a1 = a1_8;
		nrfircoef = 401;
	  }
	  else
	  {
		b = b_16;
		b1 = b1_16;
		a1 = a1_16;
		nrfircoef = 301;
	  }

	  nr2 = (int)nrfircoef/2;
	  buf = new double[no_samples+nrfircoef-1];
	  /*
	  if ( ( buf = (double*)calloc((size_t)(no_samples+nrfircoef-1), sizeof(double))) == NULL)
	  {
		fprintf(stderr, "cannot allocate enough memory to filter samples!\n");
		exit(-1);
	  }*/

	  /* IIR filter 2nd order */
	  prev_x1 = 0.;
	  prev_y1 = 0.;
	  prev_x2 = 0.;
	  prev_y2 = 0.;
	  for (i=0 ; i<no_samples ; i++)
	  {
	  	aux = (double)signal[i];
		buf[i+nr2] = (double)signal[i]*b1[0] + prev_x1*b1[1] + prev_x2*b1[2];
		buf[i+nr2] -= (a1[1] * prev_y1 + a1[2] * prev_y2);
	        prev_x2 = prev_x1;
	        prev_x1 = aux;
	        prev_y2 = prev_y1;
	        prev_y1 = buf[i+nr2];
	  }

	  /* FIR filter */
	  for (i=0 ; i<no_samples ; i++)
	  {
		sig = 0.;
	  	for (j=0 ; j<nrfircoef ; j++)
	  	{
		   sig += b[j]*buf[i+j];
		}
		signal[i] = (float) sig;
	  }
	  buf = null; //free(buf);
	}


	
	/**
	 * 
	 * @param noise_file
	 * @return
	 */
	public static float [] load_noise(char noise_file[]) {
		// TODO:
		float noise[] = null;
		
		int no_noise_samples = noise.length;

		
		System.out.println("" + no_noise_samples + "noise samples loaded from " + noise_file);

		return noise;
	}
	
	//========================================================================================================
	//========================================================================================================
	// sv-p56.c
	
	/* State for speech voltmeter function */
	static class SVP56_state
	{
	  float           f;            /* sampling frequency, in Hz */
	  int a[] = new int[15]; //unsigned long   a[15];        /* activity count */
	  double  c[] = new double[15]; //        c[15];        /* threshold level; 15 is the no.of thres. */
	  int hang [] = new int[15]; //unsigned long   hang[15];     /* hangover count */
	  int n; //unsigned long   n;            /* number of samples read since last reset */
	  double          s;            /* sum of all samples since last reset */
	  double          sq;           /* squared sum of samples since last reset */
	  double          p;            /* intermediate quantities */
	  double          q;            /* envelope */
	  double          max;          /* max absolute value found since last reset */
	  double          refdB;        /* 0 dB reference point, in [dB] */
	  double          rmsdB;        /* rms value found since last reset */
	  double          maxP, maxN;   /* maximum pos/neg.values since last reset */
	  double          DClevel;      /* average level since last reset */
	  double          ActivityFactor; /* Activity factor since last reset */
	  
	  static final double T = 0.03; //#define T        0.03	/* in [s] */
	  static final double H = 0.20; // #define H        0.20	/* in [s] */
	  static final double M = 15.9; //#define M        15.9	/* in [dB] */
	  static final int THRES_NO = 15; //#define THRES_NO 15     /* number of thresholds in the speech voltmeter */
	  
	  
	  /*#define SVP56_get_rms_dB(state) (state.rmsdB)
	  #define SVP56_get_DC_level(state) ((state).DClevel)
	  #define SVP56_get_activity(state) ((state.ActivityFactor) * 100.0)
	  #define SVP56_get_pos_max(state) ((state).maxP)
	  #define SVP56_get_neg_max(state) ((state).maxN)
	  #define SVP56_get_abs_max(state) ((state).max)
	  #define SVP56_get_smpno(state) ((state).n)
	  */
	  double get_rms_dB() {return rmsdB;}
	  double get_DC_level() {return DClevel;}
	  double get_activity() {return ActivityFactor * 100.0;}
	  double get_pos_max() {return maxP;}
	  double get_neg_max() {return maxN;}
	  double get_abs_max() {return max;}
	  int get_smpno() {return n;}
	  
	  
	  public void init_speech_voltmeter(float sampl_freq) {
		  SVP56_state  state = this;
		  
		  double          x;
		  int I, j; //long            I, j;

		  /* First initializations */
		  state.f = sampl_freq;
		  I = (int) Math.floor(H * state.f + 0.5);

		  /* Inicialization of threshold vector */
		    for (x = 0.5, j = 1; j <= THRES_NO; j++, x /= 2.0)
		      state.c[THRES_NO - j] = x;

		  /* Inicialization of activity and hangover count vectors */
		    for (j = 0; j < THRES_NO; j++)
		    {
		      state.a[j] = 0;
		      state.hang[j] = I;
		    }

		    /* Inicialization for the quantities used in the two P.56's processes */
		    state.s = state.sq = state.p = state.q = 0;
		    state.n = 0;

		    /* Inicialization of other quantities referring to state variables */
		    state.max = 0;
		    state.maxP = -32768.;
		    state.maxN = 32767.;

		    /* Defining the 0 dB reference level in terms of normalized values */
		    state.refdB = 0 /* dBov */;
		    
	  }
	  
	  
	  //#define T        0.03	/* in [s] */
	  //#define H        0.20	/* in [s] */
	  //#define M        15.9	/* in [dB] */
	  //#define THRES_NO 15     /* number of thresholds in the speech voltmeter */

	  

	  double  speech_voltmeter(float []buffer, long smpno) {
	    int             I, j;
	    int k; //long            k;
	    double          g, x, AdB, CdB, AmdB, CmdB, ActiveSpeechLevel;
	    double          LongTermLevel;
	    double [] Delta = new double[15];
	    
	    /* Hooked to eliminate sigularity with log(0.0) (happens w/all-0 data blocks */
		final double MIN_LOG_OFFSET = 1.0e-20; //  #define MIN_LOG_OFFSET 1.0e-20

		SVP56_state  state = this;

		  
	    /* Some initializations */
	    I = (int) Math.floor(H * state.f + 0.5);
	    g = Math.exp(-1.0 / (state.f * T));

	    /* Calculates statistics for all given data points */
	    for (k = 0; k < smpno; k++)
	    {
	      x = (double) buffer[k];
	      /* Compares the sample with the max. already found for the file */
	      if (Math.abs(x) > state.max)
	        state.max = Math.abs(x);
	      /* Check for the max. pos. value */
	      if (x > state.maxP)
	        state.maxP = x;
	      /* Check for the max. neg. value */
	      if (x < state.maxN)
	        state.maxN = x;

	      /* Implements Process 1 of P.56 */
	      (state.sq) += x * x;
	      (state.s) += x;
	      (state.n)++;

	      /* Implements Process 2 of P.56 */
	      state.p = g * (state.p) + (1 - g) * ((x > 0) ? x : -x);
	      state.q = g * (state.q) + (1 - g) * (state.p);

	      /* Applies threshold to the envelope q */
	      for (j = 0; j < THRES_NO; j++)
	      {
	        if ((state.q) >= state.c[j])
	        {
	  	state.a[j]++;
	  	state.hang[j] = 0;
	        }
	        if (((state.q) < state.c[j]) && (state.hang[j] < I))
	        {
	  	state.a[j]++;
	  	state.hang[j] += 1;
	        }
	     /* if (((state->q)<state->c[j])&&(state->hang[j]=I)), do nothing */
	      }		   /* [j] */
	    }		   /* [k] */

	    /* Computes the statistics */
	    state.DClevel = (state.s) / (state.n);
	    LongTermLevel = 10 * Math.log10((state.sq) / (state.n) + MIN_LOG_OFFSET);
	    state.rmsdB = LongTermLevel - state.refdB;
	    state.ActivityFactor = 0;
	    ActiveSpeechLevel = -100.0;

	    /* Test the lower active counter; if 0, is silence */
	    if (state.a[0] == 0) 
	      return(ActiveSpeechLevel);
	    else
	      AdB = 10 * Math.log10(((state.sq) / state.a[0]) + MIN_LOG_OFFSET);

	    /* Test if the lower act.counter is below the margin: if yes, is silence */
	    CdB = 20 * Math.log10((double) state.c[0]);
	    if (AdB - CdB < M) 
	      return(ActiveSpeechLevel);

	    /* Proceed serially for steps 2 and up -- this is the most common case*/
	    for (j = 1; j < THRES_NO; j++)
	    {
	      if (state.a[j] != 0)
	      {
	        AdB = 10 * Math.log10(((state.sq) / state.a[j]) + MIN_LOG_OFFSET);
	        CdB = 20 * Math.log10(((double) state.c[j]) + MIN_LOG_OFFSET);
	        Delta[j] = AdB - CdB;
	        if (Delta[j] <= M)	/* then interpolates to find the active */
	  	/* level and the activity factor and exits */
	        {
	  	/* AmdB is AdB for j-1, CmdB is CdB for j-1 */
	  	AmdB = 10 * Math.log10(((state.sq) / state.a[j - 1]) + MIN_LOG_OFFSET);
	  	CmdB = 20 * Math.log10(((double) state.c[j - 1] + MIN_LOG_OFFSET));

	  	ActiveSpeechLevel = bin_interp(AdB, AmdB, CdB, CmdB, M, 0.5 /* dB */ );

	  	state.ActivityFactor =
	  	  Math.pow(10.0, ((LongTermLevel - ActiveSpeechLevel) / 10));
	  	ActiveSpeechLevel -= (state.refdB);
	          break;
	        }
	      }
	    }

	    return (ActiveSpeechLevel);
	  }
	  
	  
	  /**
	   * 
	   * @param upcount
	   * @param lwcount
	   * @param upthr
	   * @param lwthr
	   * @param Margin
	   * @param tol
	   * @return
	   */
	  double bin_interp(double upcount, double lwcount, double upthr, double lwthr, double Margin, double tol) {
	  double          midcount, midthr, diff;
	  long iterno; //register long   iterno;

	  /* Consistency check */
	  if (tol < 0.)
	    tol = -tol;

	  /* Check if extreme counts are not already the true active value */
	  iterno = 1;
	  if ((diff = Math.abs((upcount - upthr) - Margin)) < tol)
	    return upcount;
	  if ((diff = Math.abs((lwcount - lwthr) - Margin)) < tol)
	    return lwcount;

	  /* Initialize first middle for given (initial) bounds */
	  midcount = (upcount + lwcount) / 2.0;
	  midthr = (upthr + lwthr) / 2.0;

	  /* Repeats loop until `diff' falls inside the tolerance (-tol<=diff<=tol) */
	  
	  //while ((diff = (midcount - midthr) - Margin, fabs(diff)) > tol)
	  diff = (midcount - midthr) - Margin;
	  while(Math.abs(diff)>tol)
	  {
	    /* if tolerance is not met up to 20 iteractions, then relax the 
	       tolerance by 10 % */
	    if (++iterno>20) 
	      tol *= 1.1; 

	    if (diff > tol)             /* then new bounds are ... */
	    {
	      midcount = (upcount + midcount) / 2.0; /* upper and middle activities */
	      midthr = (upthr + midthr) / 2.0;	     /* ... and thresholds */
	    }
	    else if (diff < -tol)	/* then new bounds are ... */
	    {
	      midcount = (midcount + lwcount) / 2.0; /* middle and lower activities */
	      midthr = (midthr + lwthr) / 2.0;       /* ... and thresholds */
	    }
	    diff = (midcount - midthr) - Margin; // devo ggiungerlo
	  }

	  /* Since the tolerance has been satisfied, midcount is selected 
	   * as the interpolated value with a tol [dB] tolerance. */

	  return (midcount);
	}
	  
	}
	
	//========================================================================================================
	//========================================================================================================
	// ugst-utl.c
	static long  scale(float [] buffer, long smpno, double factor)	{
	 int j; //register long   j;
	  float f; //register float  f;
	 
	  /* scales all of the samples */
	  for (f = (float) factor, j = 0; j < smpno; j++)
	    buffer[j] *= f;
	 
	  /* and return the number of scaled samples */
	  return (j);
	}
	
	
	//=========================================================================================================
	//=========================================================================================================
	// iirflt
	static class CASCADE_IIR {
		  long            nblocks;	/* number of stages in cascade        */
		  long            idown;	/* down sampling factor               */
		  long            k0;		/* start index in next segment        */
		  double          gain;		/* gain factor                        */
		  float [][]a; //float           (*a)[2];	/* In     : numerator coefficients    */
		  float [][]b; //float           (*b)[2];	/* In     : denominator coefficients  */
		  float [][] T; // float           (*T)[4];	/* In/Out : state variables, 1 for each stage*/
		  char            hswitch;	/* "U": upsampling; else downsampling */
		  
		  // The following method was in iir-lib
		  long cascade_iir_kernel(long lseg, float []x_ptr, float []y_ptr)
		  //long            lseg;
		  //float          *x_ptr;
		  //CASCADE_IIR    *iir_ptr;
		  //float          *y_ptr;
		  {
			
			  CASCADE_IIR    iir_ptr = this;

			  if (iir_ptr.hswitch == 'U') {
				  return
						  cascade_form_iir_up_kernel( /* returns number of output samples */
							  lseg,	          /* In : input signal leng. */
							  x_ptr,	  /* In : input sample array */
							  y_ptr,	  /* Out: outp. sample array */
							  iir_ptr.idown, /* In : dwnsmpl.factor */
							  iir_ptr.nblocks, /*In: no.IIR-coeffs */
							  iir_ptr.gain,  /* In : gain factor*/
							  iir_ptr.a,	  /* In : num.coeffs */
							  iir_ptr.b,	  /* In : denom.coeffs */
							  iir_ptr.T	  /* I/O: state vars */
						  );
			  }
			  else {
				  long k0[] = { iir_ptr.k0 };
				  long v = cascade_form_iir_down_kernel(/* returns number of output samples */
							   lseg,	  /* In : input signal leng. */
							   x_ptr,	  /* In : input sample array */
							   y_ptr,	  /* Out: outp. sample array */
							   k0, //&(iir_ptr->k0), /*I/O: start idx,x-array */
							   iir_ptr.idown, /*In : dwnsmpl.factor */
							   iir_ptr.nblocks, /*In:no.of IIR-coeffs */
							   iir_ptr.gain, /* In : gain factor */
							   iir_ptr.a,	  /* In : numerator coeffs */
							   iir_ptr.b,	  /* In : denom.coeffs */
							   iir_ptr.T	  /* I/O: state vars */
						  );
				  iir_ptr.k0 = k0[0];
				  return v;
				  }
		  }

		}
		
	//=========================================================================================================
	//=========================================================================================================
	// firflt
	class SCD_FIR {
	        long  lenh0;                    /* number of FIR coefficients        */
	        long  dwn_up;                   /* down sampling factor              */
	        long  k0;                       /* start index in next segment       */
	                                        /* (needed in segmentwise filtering) */
	        float []h0;                      /* pointer to array with FIR coeff.  */
	        float []T;                       /* pointer to delay line             */
	        char  hswitch;                  /* switch to FIR-kernel              */
	}
	
	
	//=========================================================================================================
	//=========================================================================================================
	// cascg712
	static CASCADE_IIR iir_G712_8khz_init()
	{
	  float [][]a_cof;
	  float [][]b_cof; //float         **a_cof, **b_cof; /* pointer to numerator/denominator */
	  int nblocks; //long            nblocks;	  /* number of 2'nd order blocks */


	  final float a_G712_8khz[][] = new float [][]  { //       T[L]1   ,      T[L]2 
		     {0.197140840E+01f, 0.100000000E+01f},
		     {-0.199301310E+01f, 0.100000000E+01f}
		   };
		
	  final float  b_G712_8khz[][] =  { /*  T[L]3   ,      T[L]4 */
	     {0.156814950E+01f, 0.690445310E+00f},
	     {-0.179704400E+01f, 0.830129300E+00f}
	               };
	  final int nblocks_8khz = 2; // #define nblocks_8khz        2	  /* number of 2'nd order blocks */

	  a_cof = a_G712_8khz;
	  b_cof = b_G712_8khz;
	  nblocks = nblocks_8khz;
		   
	  //fill_iir_G712_8khz(&a_cof, &b_cof, &nblocks); /* get pointer to filter-coefficients */
	    

	  return cascade_iir_init(
				 nblocks, // In: number of 2'nd order blocks
				 a_cof,	// In: 24-bit repres. of  numer. coef.
				 b_cof,	// In: 24-bit repres. of  denom. coef.
				 0.695296250,  // In: gain factor for filter
				 (long) 1,// In: Down-sampling factor
				 'D');	  // -> call down-sampling routine
	  
	  /*return cascade_iir_init(	  // Returns: pointer to CASCADE_IIR-struct
				 nblocks, // In: number of 2'nd order blocks
				 (float (*)[2]) a_cof,	// In: 24-bit repres. of  numer. coef.
				 (float (*)[2]) b_cof,	// In: 24-bit repres. of  denom. coef.
				 0.695296250,  // In: gain factor for filter
				 (long) 1,// In: Down-sampling factor
				 'D');	  // -> call down-sampling routine
*/
	}
	
	/*
	void            fill_iir_G712_8khz(a_cof, b_cof, nblocks)
	  float        ***a_cof;
	  float        ***b_cof;
	  long           *nblocks;
	{
	#define nblocks_8khz        2	  // number of 2'nd order blocks 

	  // Numerator coefficients
	  static float a_G712_8khz[nblocks_8khz][2] = 
	                           { //       T[L]1   ,      T[L]2 
				     {0.197140840E+01, 0.100000000E+01},
				     {-0.199301310E+01, 0.100000000E+01}
				   };

	  // Denominator coefficients 
	  static float  b_G712_8khz[nblocks_8khz][2] =
	                           { //  T[L]3   ,      T[L]4 
				     {0.156814950E+01, 0.690445310E+00},
				     {-0.179704400E+01, 0.830129300E+00}
	                           };


	  *nblocks = nblocks_8khz;
	  *a_cof = (float **) a_G712_8khz;
	  *b_cof = (float **) b_G712_8khz;
	}
	#undef nblocks_8khz
*/

	
	//=========================================================================================================
	//=========================================================================================================
	// iir-lib
	static CASCADE_IIR cascade_iir_init(int nblocks, float [][]a, float [][]b, double gain, long idown, char hswitch) {
		// long nblocks
	  //float           (*a)[2], (*b)[2];
	
	  CASCADE_IIR ptrIIR = null;	  /* pointer to the new struct */
	  //float           fak;
	  float [][]T_ptr; //float           (*T_ptr)[4];
	  int n; //long             n;


	  /* Allocate memory for a new struct */
	  ptrIIR = new CASCADE_IIR(); //ptrIIR = (CASCADE_IIR *) malloc((long) sizeof(CASCADE_IIR));
	  /*if (ptrIIR == (CASCADE_IIR *) 0L)
	  {
	    return 0;
	  }
	  */

	  /* Allocate memory for state variables */
	  ptrIIR.T = new float[nblocks][4];
	  
	  /*if ((ptrIIR->T = (float (*)[4]) malloc((nblocks * 4) * sizeof(fak)))
	      == (float (*)[4]) 0)
	  {
	    free(ptrIIR);
	    return 0;
	  }*/


	  /* fill coefficient sets */
	  ptrIIR.nblocks = nblocks;	  /* store number of 2'nd order blocks */
	  ptrIIR.a = a;
	  ptrIIR.b = b;

	  /* store down-sampling factor/gain/direct-path coefficient */
	  ptrIIR.idown = idown;
	  ptrIIR.gain = gain;

	  /* Store switch to IIR-kernel procedure */
	  ptrIIR.hswitch = hswitch;

	  /* Clear state variables */
	  T_ptr = ptrIIR.T;
	  for (n = 0; n < nblocks; n++)
	  {
	    T_ptr[n][0] = 0.0f;
	    T_ptr[n][1] = 0.0f;
	    T_ptr[n][2] = 0.0f;
	    T_ptr[n][3] = 0.0f;
	  }

	  ptrIIR.k0 = idown;		  /* modulo counter for down-sampling */


	  /* Exit returning pointer to struct */
	  return (ptrIIR);
	  
	} // End of method Fant.cascade_iir_init
	
	
	
	/**
	 * 
	 * @param lenx
	 * @param x
	 * @param y
	 * @param iup
	 * @param nblocks
	 * @param gain
	 * @param a
	 * @param b
	 * @param T
	 * @return
	 */
	static long cascade_form_iir_up_kernel(long lenx, float []x, float[]y, long iup, long nblocks, double gain, float[][]a, float[][]b, float[][]T)
//float          *x, *y;
//long            iup, nblocks;
//float           (*a)[2], (*b)[2], (*T)[4];
	{
		int kx, ky, n; //long kx, n, ky;
		double xj, yj;
		
		yj=0; //inutile

		kx = 0;			  /* starting index in input array (x) */
		for (ky = 0; ky < iup * lenx; ky++)	/* loop over all input samples */
		{
			/* Compute output only every "iup" compute output only every "iup" 
			* samples by taking one input sample direct path OR by using a 
			* zero-valued sample */
			if (ky % iup == 0)		  
				xj = x[kx];                
			else                          
				xj = 0.;
			
			/* Filter samples through all cascade stages */
			for (n = 0; n < nblocks; n++)    
			{
				yj =  xj + a[n][0] * T[n][0] + a[n][1] * T[n][1];
				yj -= (b[n][0] * T[n][2] + b[n][1] * T[n][3]);
				
				/* Save samples in memory */
				T[n][1] = T[n][0];
				T[n][0] = (float)xj; //xj;
				T[n][3] = T[n][2];
				T[n][2] = (float)yj; // yj
				
				/* The yj of this stage is the xj of the next */
				xj = yj;
			}
			
			/* Apply the gain and update x counter if needed */
			y[ky] = (float)(yj*gain); //yj * gain;
			if (ky % iup == 0) kx++;
		}
		return ky;
		
	} // End of Fant.cascade_form_iir_up_kernel


	/**
	 * 
	 * @param lenx
	 * @param x
	 * @param y
	 * @param k0
	 * @param idown
	 * @param nblocks
	 * @param gain
	 * @param a
	 * @param b
	 * @param T
	 * @return
	 */
	static long cascade_form_iir_down_kernel(long lenx, float []x, float[]y, long[]k0, long idown, long nblocks, double gain, float[][]a, float[][]b, float[][]T)
	//long            lenx;
	//float          *x, *y;
	//long           *k0, idown, nblocks;
	//float           (*a)[2], (*b)[2], (*T)[4];
	{
		int             kx, ky, n; //long            kx, ky, n;
		double   xj;
		double yj = 0; //inutile inizializzare
		
		
		ky = 0;			  /* starting index in output array (y) */
		for (kx = 0; kx < lenx; kx++)	  /* loop over all input samples */
		{
			xj = x[kx]; /* direct path */
			for (n = 0; n < nblocks; n++)	/* loop over all second order filter */
			{
					yj =  xj + a[n][0] * T[n][0] + a[n][1] * T[n][1];
					yj -= (b[n][0] * T[n][2] + b[n][1] * T[n][3]);
		
				/* Save samples in memory */
				T[n][1] = T[n][0];
				T[n][0] = (float) xj;
				T[n][3] = T[n][2];
				T[n][2] = (float) yj;
				
				/* The yj of this stage is the xj of the next */
				xj = yj;
			}
		
			if ( Math.floorMod(k0[0], idown)==0) //if (*k0 % idown == 0)	  // compute output only every "idown" samples 
			{
				/* Apply gain and update y-samples' counter */
				y[ky] = (float)(yj * gain);
				ky++;
			}
			k0[0]++; //(*k0)++;
		}
		k0[0] = Math.floorMod(k0[0],  idown); //*k0 %= idown;			  /* avoid overflow by (*k0)++ */
		return ky;
		
	} // End of method Fant.cascade_form_iir_down_kernel


	//=========================================================================================================
	//=========================================================================================================
	// fir-hp
		
	SCD_FIR fir_hp_8khz_init ()
	{
	  float          []h0;		/* pointer to array with FIR coeff. */
	  int lenh0; //long            lenh0;	/* number of FIR coefficients */


	  /* allocate array for FIR coeff. and fill with coefficients */
	  //fill_fir_hp_8khz(&h0, &lenh0);
	  final int HP_8K_LEN =251; //#define HP_8K_LEN 251

	   //static float hp_8khz_coeff[HP_8K_LEN] ={
	  final float hp_8khz_coeff[] = {
	   0.00019984355069981700f,  0.00020324149375248640f,  0.00020712997490214150f, 
	   0.00021152561920372840f,  0.00021643855204390890f,  0.00022187211750733480f, 
	   0.00022782262112960940f,  0.00023427909838851350f,  0.00024122311020115020f, 
	   0.00024862856660538220f,  0.00025646157970953580f,  0.00026468034689519780f, 
	   0.00027323506515432210f,  0.00028206787733416060f,  0.00029111285095212080f, 
	   0.00030029599012787860f,  0.00030953528106237000f,  0.00031874077137303870f, 
	   0.00032781468347237000f,  0.00033665156205269610f,  0.00034513845561499400f, 
	   0.00035315513185332860f,  0.00036057432658018790f,  0.00036726202575168100f, 
	   0.00037307778002584000f,  0.00037787505116262120f,  0.00038150158945098860f, 
	   0.00038379984122724620f,  0.00038460738542994400f,  0.00038375739802067220f, 
	   0.00038107914298735200f,  0.00037639848853759820f,  0.00036953844698482680f, 
	   0.00036031973672939980f,  0.00034856136464161210f,  0.00033408122706313280f, 
	   0.00031669672755894550f,  0.00029622540947323300f,  0.00027248560127034850f, 
	   0.00024529707257629140f,  0.00021448169877723660f,  0.00017986413197991390f, 
	   0.00014127247609420350f,  0.00009853896376142904f,  0.00005150063282266082f, 
	   -0.00000000000000000017f,  -0.00005611427054948061f, 
	   -0.00011698670615276900f,  -0.00018275437435563430f, 
	   -0.00025354624380472720f,  -0.00032948255958947780f, 
	   -0.00041067423532885210f,  -0.00049722226425247260f, 
	   -0.00058921715148209200f,  -0.00068673836966793720f, 
	   -0.00078985384007517500f,  -0.00089861944114887200f, 
	   -0.00101307854651154700f,  -0.00113326159426583600f, 
	   -0.00125918568938637600f,  -0.00139085424088980000f, 
	   -0.00152825663537017000f,  -0.00167136794837955500f, 
	   -0.00182014869502006900f,  -0.00197454462099491100f, 
	   -0.00213448653524230700f,  -0.00229989018514782000f, 
	   -0.00247065617519815000f,  -0.00264666992980327100f, 
	   -0.00282780170087432200f,  -0.00301390662060243200f, 
	   -0.00320482479973900600f,  -0.00340038147153162800f, 
	   -0.00360038718132193300f,  -0.00380463802166317200f, 
	   -0.00401291591266622700f,  -0.00422498892713407600f, 
	   -0.00444061165989654500f,  -0.00465952564061029000f, 
	   -0.00488145978914371900f,  -0.00510613091252350300f, 
	   -0.00533324424227905100f,  -0.00556249401088406000f, 
	   -0.00579356406586089700f,  -0.00602612851998403700f, 
	   -0.00625985243589418000f,  -0.00649439254331473100f, 
	   -0.00672939798694814500f,  -0.00696451110302112500f, 
	   -0.00719936822234543700f,  -0.00743360049766550500f, 
	   -0.00766683475297530700f,  -0.00789869435240567000f, 
	   -0.00812880008620923100f,  -0.00835677107130444600f, 
	   -0.00858222566378198500f,  -0.00880478238072741900f, 
	   -0.00902406082867291600f,  -0.00923968263595819400f, 
	   -0.00945127238625747200f,  -0.00965845855051422600f, 
	   -0.00986087441451983200f,  -0.01005815899937536000f, 
	   -0.01024995797208802000f,  -0.01043592454357474000f, 
	   -0.01061572035137586000f,  -0.01078901632442047000f, 
	   -0.01095549352723289000f,  -0.01111484398102595000f, 
	   -0.01126677145919164000f,  -0.01141099225477229000f, 
	   -0.01154723591757671000f,  -0.01167524595869412000f, 
	   -0.01179478052025484000f,  -0.01190561300839013000f, 
	   -0.01200753268745304000f,  -0.01210034523367925000f, 
	   -0.01218387324658887000f,  -0.01225795671655838000f, 
	   -0.01232245344712549000f,  -0.01237723943072760000f, 
	   -0.01242220917671699000f,  -0.01245727599064251000f, 
	   -0.01248237220393670000f,  -0.01249744935330040000f,  0.98769578642927660000f,
	    -0.01249744935330040000f,  -0.01248237220393670000f, 
	   -0.01245727599064251000f,  -0.01242220917671699000f, 
	   -0.01237723943072760000f,  -0.01232245344712549000f, 
	   -0.01225795671655838000f,  -0.01218387324658887000f, 
	   -0.01210034523367925000f,  -0.01200753268745304000f, 
	   -0.01190561300839013000f,  -0.01179478052025484000f, 
	   -0.01167524595869412000f,  -0.01154723591757671000f, 
	   -0.01141099225477229000f,  -0.01126677145919164000f, 
	   -0.01111484398102595000f,  -0.01095549352723289000f, 
	   -0.01078901632442047000f,  -0.01061572035137586000f, 
	   -0.01043592454357473000f,  -0.01024995797208802000f, 
	   -0.01005815899937536000f,  -0.00986087441451983200f, 
	   -0.00965845855051423000f,  -0.00945127238625747200f, 
	   -0.00923968263595819400f,  -0.00902406082867291800f, 
	   -0.00880478238072741900f,  -0.00858222566378198500f, 
	   -0.00835677107130444700f,  -0.00812880008620923400f, 
	   -0.00789869435240566800f,  -0.00766683475297530800f, 
	   -0.00743360049766550300f,  -0.00719936822234543800f, 
	   -0.00696451110302112700f,  -0.00672939798694814600f, 
	   -0.00649439254331473200f,  -0.00625985243589418300f, 
	   -0.00602612851998403500f,  -0.00579356406586089600f, 
	   -0.00556249401088406100f,  -0.00533324424227905100f, 
	   -0.00510613091252350500f,  -0.00488145978914371900f, 
	   -0.00465952564061029000f,  -0.00444061165989654700f, 
	   -0.00422498892713407800f,  -0.00401291591266622800f, 
	   -0.00380463802166317500f,  -0.00360038718132193200f, 
	   -0.00340038147153162600f,  -0.00320482479973900800f, 
	   -0.00301390662060243200f,  -0.00282780170087432200f, 
	   -0.00264666992980327200f,  -0.00247065617519815100f, 
	   -0.00229989018514782000f,  -0.00213448653524230800f, 
	   -0.00197454462099491200f,  -0.00182014869502006800f, 
	   -0.00167136794837955500f,  -0.00152825663537017000f, 
	   -0.00139085424088980000f,  -0.00125918568938637600f, 
	   -0.00113326159426583600f,  -0.00101307854651154700f, 
	   -0.00089861944114887270f,  -0.00078985384007517520f, 
	   -0.00068673836966793730f,  -0.00058921715148209260f, 
	   -0.00049722226425247240f,  -0.00041067423532885190f, 
	   -0.00032948255958947790f,  -0.00025354624380472720f, 
	   -0.00018275437435563430f,  -0.00011698670615276900f, 
	   -0.00005611427054948063f,  -0.00000000000000000017f,  0.00005150063282266088f,
	    0.00009853896376142908f,  0.00014127247609420340f,  0.00017986413197991390f, 
	   0.00021448169877723650f,  0.00024529707257629120f,  0.00027248560127034860f, 
	   0.00029622540947323300f,  0.00031669672755894540f,  0.00033408122706313300f, 0.00034856136464161220f,  0.00036031973672940000f,  0.00036953844698482710f, 
	   0.00037639848853759800f,  0.00038107914298735160f,  0.00038375739802067220f, 
	   0.00038460738542994400f,  0.00038379984122724600f,  0.00038150158945098870f, 
	   0.00037787505116262130f,  0.00037307778002584000f,  0.00036726202575168140f, 
	   0.00036057432658018840f,  0.00035315513185332880f,  0.00034513845561499400f, 
	   0.00033665156205269610f,  0.00032781468347236990f,  0.00031874077137303890f, 
	   0.00030953528106237010f,  0.00030029599012787860f,  0.00029111285095212100f, 
	   0.00028206787733416070f,  0.00027323506515432210f,  0.00026468034689519800f, 
	   0.00025646157970953590f,  0.00024862856660538200f,  0.00024122311020115020f, 
	   0.00023427909838851340f,  0.00022782262112960940f,  0.00022187211750733490f, 
	   0.00021643855204390890f,  0.00021152561920372840f,  0.00020712997490214150f, 
	   0.00020324149375248640f,  0.00019984355069981700f };


	   lenh0 = HP_8K_LEN;           /* store 'number of coefficients' */
	   h0  = hp_8khz_coeff;		/* store pointer to []-array */
	  //}
	  //#undef HP_8K_LEN

	  return
	    fir_initialization(		/* Returns: pointer to SCD_FIR-struct */
			       lenh0,	/* In: number of FIR-coefficients */
			       h0,	/* In: pointer to array with FIR-cof. */
			       1.0,	/* In: gain factor for FIR-coeffic. */
			       1l,	/* In: Down-sampling factor */
			       'D'	/* In: switch to down-sampling proc. */
	    );				/* (works here as simple FIR-fil. */
	
	} // End of Fant.fir_hp_8khz_init method
	
	
	
	//=========================================================================================================
	//=========================================================================================================
	// fir-lib
		
	SCD_FIR fir_initialization(int lenh0, float []h0, double gain, long idwnup, int hswitch) //long lenh0
	  //long            lenh0;
	  //float           h0[];
	  //long            idwnup;
	  //int /* char */  hswitch;
	{
	  SCD_FIR        ptrFIR=null;	/* pointer to the new struct */
	  float           fak;
	  int k; //long            k;


	/*
	 * ......... ALLOCATION OF MEMORY .........
	 */

	  ptrFIR = new SCD_FIR();
	  /*
	  // Allocate memory for a new struct
	  if ((ptrFIR = (SCD_FIR *) malloc((long) sizeof(SCD_FIR))) ==(SCD_FIR *) NULL)
	  {
	    return 0;
	  }
	  */

	  /* Allocate memory for delay line */
	  ptrFIR.T = new float[lenh0 - 1];
	  /*if ((ptrFIR->T = (float *) malloc((lenh0 - 1) * sizeof(fak))) == (float *) 0)
	  {
	    free(ptrFIR);		// deallocate struct FIR
	    return 0;
	  }*/

	  /* Allocate memory for impulse response */
	  ptrFIR.h0 = new float[lenh0];
	  /*if ((ptrFIR->h0 = (float *) malloc(lenh0 * sizeof(fak))) == (float *) 0)
	  {
	    free(ptrFIR->T);		// deallocate delay line
	    free(ptrFIR);		/( deallocate struct FIR
	    return 0;
	  }*/

	/*
	 * ......... STORE VARIABLES INTO STATE VARIABLE .........
	 */

	  /* Store number of FIR-coefficients */
	  ptrFIR.lenh0 = lenh0;

	  /* Fill FIR coefficients into struct; for upsampling tasks the
	   * FIR-coefficients are multiplied by the upsampling factor 'gain' */
	  for (k = 0; k <= ptrFIR.lenh0 - 1; k++)
	    ptrFIR.h0[k] = (float)(gain * h0[k]);

	  /* Store down-/up-sampling factor */
	  ptrFIR.dwn_up = idwnup;

	  /* Store switch to FIR-kernel (up- or downsampling function) */
	  ptrFIR.hswitch = (char) hswitch; //era hswitch;

	  /* Clear Delay Line */
	  for (k = 0; k < ptrFIR.lenh0 - 1; k++)
	    ptrFIR.T[k] = 0.0f;

	  /* Store default starting index for the x-array */
	  /* NOTE: for down-sampling: if the number of input samples is not a
	   * multiple of the down-sampling factor, k0 points to the first sample in
	   * the next input segment to be processed */
	  ptrFIR.k0 = 0;

	  /* Return pointer to struct */
	  return (ptrFIR);
	}


} // End of class Fant
