#include <stdio.h>
#include <stdlib.h>
//#include <iostream.h>



class hvsrc_dataconsts {
 public:
  const static int QUANT_FILT = 1024;
  const static int QUANT_FILT_UV = 64;
  const static int GRP_SZ = 4;
  const static int GRP = 2;

  const static int NORMAL = 0x0001;
  const static int VAR_ZOOM = 0x0004;
  const static int FIR = 0x0008;
  const static int PIP  = 0x0010;
  const static int ODDTAP = 0x0020;
  const static int MUX = 0x0040;
  const static int TAPS3 = 0x0080;
  const static int TAPS6 = 0x0100;
  const static int TAPS12 = 0x0200;
  const static int RNDFIRDIR = 0x0400;
  const static int SHIFT_OFFST= 10;
  const static int SHIFT_A = 25 - SHIFT_OFFST;
  const static int SHIFT_B = 18;
  const static int SHIFT_C = 25;

  const static int ADDBEGIN = 0x0001;
  const static int ADDEND = 0x0002;

  const static int MEDIAN = 0x0001;

  const static int INTERL_IN = 0x0002;
  const static int INTERL_OUT = 0x0004;
  const static int FIELD_DELAY = 0x0008;
  const static int VAR_ZOOM_V = 0x0010;

  const static int SHIFTV_OFFST = 10;
  const static int SHIFTV_A = 23 - SHIFTV_OFFST;
  const static int SHIFTV_B = SHIFTV_A + 1;
  const static int SHIFTV_C = 23;
};

class hsrc_params_in{
 public:
  int addbegin;
  int addend;
  int pixels_in;
  int pixels_out;
  int clp_min;
  int clp_max;
  int number_of_lines;
  int taps;
  int phases;
  int quality;
};

class vsrc_params_in{
 public:
  int lines_in;
  int lines_out;
  int clp_min;
  int clp_max;
  int number_of_pixels;
  int phases;
  int median;
  int inter_field;
  int field_type_in;
  int field_type_out;
  int nr_of_cache_lines;
  int load_filter;
  int mem_split;
};

class vsrc_params_out{
 public:
  int   outlen;
  int   tslots;
  int   nr_pixels_div2;
  int   nr_lines;
  int   offset_odd;
  int   offset_even;
  int   st;
  int   phase_shift;
  int   zoom;
  int   clp_min;
  int   dzoom;
  int   fieldtype;
  int   max_lines;
  int   ddzoom;
  int   max_lines_in;
  int   mem_offset;
  int   clp_max;
  int   phase_and;
  int   mode;
  int   mem_mode;
};

class hcontrol_params{
 public:
  int Start;
  int Zoom;                /* contains the zoom factor */
  int Dzoom;
  int DDzoom;
  int D_offset;
  int Mode;                /* determines the mode in which the src operates */
  int Phase_shift;         /* can either be 2 ,3 ,4 ,5,6,7 */
  int Phase_and;        /* can either be 63,31,15,7,3,1 */
  /* should be used correctly in combination with Phase_shift */
  /* Phase_shift and Phase_and enable the possiblity to use */
  /* poly-phase filters with different number of phases */

  int **phases_int;
//  char[][] shift; // not used anymore
  int Loop_Length;
  int Samples_per_line;
  int quant_filt;
  int Samples_out;
  int clp_min;
  int clp_max;
  int addbegin;
  int addend;
  int lines_per_field;

  /*
  hcontrol_params(bool alloc_phases_int) {
   //    if (alloc_phases_int) phases_int = new int[128][6];
  }



  public Object  clone() {
    int i,j;
    hcontrol_params copy = new hcontrol_params(true);
    blackbox(SystemCSim)
    #ifdef DEBUG_PIP
      cout << "hcontrol_params:: clone()" << endl;
    #endif

    copy.Start = this.Start;
    copy.Zoom = this.Zoom;
    copy.Dzoom = this.Dzoom;
    copy.DDzoom = this.DDzoom;
    copy.D_offset = this.D_offset;
    copy.Mode = this.Mode;
    copy.Phase_shift = this.Phase_shift;
    copy.Phase_and = this.Phase_and;
    if (phases_int != null) {
      for (i=0;i<128;i++)
        for (j=0;j<6;j++) {
          copy.phases_int[i][j] = this.phases_int[i][j];
          blackbox(SystemCSim)
            //    if (j==0)
            //              cout << "HCP:";
            //            cout << this->phases_int[i][j] << " ";
            //            if (j==5)
            //              cout << endl;

        }
    }
    copy.Loop_Length = this.Loop_Length;
    copy.Samples_per_line = this.Samples_per_line;
    copy.quant_filt = this.quant_filt;
    copy.Samples_out = this.Samples_out;
    copy.clp_min = this.clp_min;
    copy.clp_max = this.clp_max;
    copy.addbegin = this.addbegin;
    copy.addend = this.addend;
    copy.lines_per_field = this.lines_per_field;
    return(copy);
  }
  */

};

class hcorrect_control_params{
 public:
  int mode;
  int addbegin;
  int addend;
  int Samples_per_line;
};

class line_type{
 public:
  const static int ORIG = 0;
  const static int DELAY = 1;
};

/*
The struct vcontrol_params contains the functional "image" of the VSRC.
(the paramters which are send on field base and ar calculated by the TCP)
*/
class vcontrol_params {                 /* width range  */
public:
  int Start;                /* 18u    [0-200000]        */
  int Zoom;                /* 27u    [0-1.0e8]         */
  int dzoom;                /* 20s    [-1000000-1000000]*/
  int ddzoom;                /* 15s    [-30000-30000]    */
  int Mode;                /*  1u    [0,1]             */
  int Phase_shift;         /*  3u    [0-7]             */
  int Phase_and;                /*  6u    [1,3,7,15,31,63]  */
  // YW:  int[][] phases_int = new int[64][7];
  //int **phases_int;
  int phases_int[64][7];
                                /* 10s    [-1024-1023]      */
  int Samples_per_line;        /* 11u    [0-2000]          */
  int Mem_start;                /* tbd,depends on cache impl*/
  int taps;                /*  3u    [1-7]             */
  int line_memories;        /*  3u    [1-6]             */
  short clp_min;                /*  8u    [0-255]           */
  short clp_max;                /*  8u    [0-255]           */
  short offset_odd;        /* 10u    [0-1023]          */
  short offset_even;        /* 10u    [0-1023]          */
  int Max_lines;                /* 10u    [0-1023]          */
  int Max_lines_in;        /* 10u    [0-1023]          */

  vcontrol_params(bool alloc) {
    //if (alloc) phases_int = new int[64][7];
  }

  /*
  Object  clone() {
    int i,j;
    vcontrol_params cloned;
    if (this.phases_int == null) {
      cloned = new vcontrol_params(false);
    }
    else {
      cloned = new vcontrol_params(true);
    }
    blackbox(SystemCSim)
    #ifdef DEBUG_PIP
      cout << "In vcontrol_params:: clone()" << endl;
    #endif

    cloned.Start = this.Start;
    cloned.Zoom = this.Zoom;
    cloned.dzoom = this.dzoom;
    cloned.ddzoom = this.ddzoom;
    cloned.Mode = this.Mode;
    cloned.Phase_shift = this.Phase_shift;
    cloned.Phase_and = this.Phase_and;
    if (this.phases_int != null) {
       for (i=0;i<64;i++)
         for (j=0;j<7;j++)
            cloned.phases_int[i][j] = this.phases_int[i][j];
       blackbox(SystemCSim)
        #ifdef DEBUG_PIP
         cout << "phases_int: 0,0-6="
         << cloned->phases_int[0][0] << " "
         << cloned->phases_int[0][1] << " "
         << cloned->phases_int[0][2] << " "
         << cloned->phases_int[0][3] << " "
         << cloned->phases_int[0][4] << " "
         << cloned->phases_int[0][5] << " "
         << cloned->phases_int[0][6]
         << endl;
       #endif

    }
    cloned.Samples_per_line = this.Samples_per_line;
    cloned.Mem_start = this.Mem_start;
    cloned.taps = this.taps;
    cloned.line_memories = this.line_memories;
    cloned.clp_min = this.clp_min;
    cloned.clp_max = this.clp_max;
    cloned.offset_odd = this.offset_odd;
    cloned.offset_even = this.offset_even;
    cloned.Max_lines = this.Max_lines;
    cloned.Max_lines_in = this.Max_lines_in;

    return((Object)cloned);
  }
  */
};

/*
The struct vsrc_params contains the line-frequent needed by the task TVSRC.
These paramters are updated by the task TGENParams (line-frequent)
*/
class vsrc_params {
public:
  int delta;                /*  6u    [0-63]            */
  bool reset;                /*  1u    [TRUE, FALSE]     */
  bool line_out;                /*  1u    [TRUE, FALSE]     */
  bool put_data;                /*  1u    [TRUE, FALSE]     */
  int LineType;        /*  line_type 1u    [ORIG, DELAY]     */

  /*
  public Object clone() {
    blackbox(SystemCSim)
    #ifdef DEBUG_PIP
      cout << "In vsrc_params:: clone()" << endl;
    #endif

    vsrc_params cloned = new vsrc_params();
    cloned.delta = this.delta;
    cloned.reset = this.reset;
    cloned.line_out = this.line_out;
    cloned.put_data = this.put_data;
    cloned.LineType = this.LineType;
    return((Object)cloned);
  }
  */
};

/*
The struct vsrc_control is a sub-set of vcontrol_params.
These parameters are used by the tasks TVSRC and TGENParams
and remain the same during the complete field
*/
class vsrc_control{
public:
  int Phase_shift;                 /*  3u    [0-7]             */
  int Phase_and;                        /*  6u    [1,3,7,15,31,63]  */
  int phases_int[64][7];
                                        /* 10s    [-1024-1023]      */
  int Samples_per_line;                /* 11u    [0-2000]          */
  int Mem_start;                        /* tbd,depends on cache impl*/
  short clp_min;                        /*  8u    [0-255]           */
  short clp_max;                        /*  8u    [0-255]           */
  int taps;                        /*  3u    [1-7]             */
  int line_memories;                /*  3u    [1-6]             */
  int Mode;                        /*  1u   [0,1]              */
  int Max_lines;                        /* 10u    [0-1023]          */
  int Max_lines_in;                /* 10u    [0-1023]          */

  vsrc_control(bool alloc) {
    //if (alloc) phases_int = new int[64][7];
  }


  /*
  public Object  clone() {
    int i,j;
    vsrc_control cloned;
    if (this.phases_int == null) {
      cloned = new vsrc_control(false);
    }
    else {
      cloned = new vsrc_control(true);
    }
    blackbox(SystemCSim)
    #ifdef DEBUG_PIP
      cout << "In vsrc_control:: clone()" << endl;
    #endif

    cloned.Phase_shift = this.Phase_shift;
    cloned.Phase_and = this.Phase_and;
    if (this.phases_int != null) {
       for (i=0;i<64;i++)
         for (j=0;j<7;j++)
            cloned.phases_int[i][j] = this.phases_int[i][j];
       blackbox(SystemCSim)
       #ifdef DEBUG_PIP
         cout << "phases_int: 0,0-6="
         << cloned->phases_int[0][0] << " "
         << cloned->phases_int[0][1] << " "
         << cloned->phases_int[0][2] << " "
         << cloned->phases_int[0][3] << " "
         << cloned->phases_int[0][4] << " "
         << cloned->phases_int[0][5] << " "
         << cloned->phases_int[0][6]
         << endl;
       #endif



    }
    cloned.Samples_per_line = this.Samples_per_line;
    cloned.Mem_start = this.Mem_start;
    cloned.clp_min = this.clp_min;
    cloned.clp_max = this.clp_max;
    cloned.taps = this.taps;
    cloned.line_memories = this.line_memories;
    cloned.Mode = this.Mode;

    cloned.Max_lines = this.Max_lines;
    cloned.Max_lines_in = this.Max_lines_in;

    return((Object)cloned);
  }
  */
};



//////////////////////////////////////////////////////////////////////////
//// vfconstans
/**
 * The header file for defining various constants
 *
 * @author   Yoshi Watanabe
 */
//////////////////////////////////////////////////////////////////////////


class vfconsts{
public:
  const static int _DEFAULT_MM = 5;
  const static int _DEFAULT_CHAN = 5;

  const static double PI = 3.141592654;
  const static double TWOPI = 6.283185307;

  /* Y-range {0.5.10} */
  const static int Y_MIN = 0;
  const static int Y_MAX = 255;

  const static int YRGB_MIN = 1;
  const static int YRGB_MAX = 2047;
  const static int YRGB_OFFSET = 512;
  const static int UV_MIN = 0;
  const static int UV_MAX = 255;

  /* line lengths */
  /* NOTE: SHOULD DEPEND ON RECEIVED TV STANDARD */
  const static int MSAL = 1920; /* Maximum Samples per Active-video Line: changed HD     */


  const static int NSAL = 1920; /* Nominal Samples per Active-video Line: changed HD     */

  /* field lenghts */
  /* NOTE: SHOULD DEPEND ON RECEIVED TV STANDARD */
  const static int NLFR = 1152; /* Nominal Lines per FRame                             */
  const static int NLAF = 288;

  const static int NSAF = NSAL*NLAF; /* Nominal Samples per Active Field; NSAL*NLAF         */
  const static int NSAFR = 2*NSAL*NLAF; /* Nominal Samples per Active Frame; NSAL*NLAFR         */

  /* field numbering */
  const static int MAX_FLD = 127; /* Maximum field number; 2^7-1 (char) */

  /* for simulation */
  const static int L_Proc_init = 576; /* nr lines of initial frame to be resized */
  const static int pixels_out_init = 144; /* start value for nr pixels out */
  const static int lines_out_init = 64; /* start value for nr output lines per field */


  const static int base_posx_init = 0;
  const static int base_posy_init = 0;
  const static int base_width_init = 704;  // frame size
  const static int base_height_init = 480; // frame size

  const static int ip_posx_init = 16; // field position
  const static int ip_posy_init = 4;  // in lines of field (not frame)
  const static int ip_width_init = pixels_out_init;  // width of frame
  const static int ip_height_init = 2*lines_out_init; // height of frame
};


// vfinterface.mmm classes
class active_line{
public:
  // static final int MSAL = 1920; // HWT: C compiler doesn't like assignment
  short c[vfconsts::MSAL];// hack by TCM

  active_line(bool alloc) {
    // if (alloc) c = new short[vfconsts.MSAL];
    //if (alloc) c = new short[vfconsts.MSAL]; // HWT 9/26/02. for SystemC generation
  }

};



class header {
public:
  int Id;            /* component_type: defines the component type        */
  short NrOfSamples; /* Number of Samples per line */
  bool StartOfField; /* TRUE: line is first of field        */
  bool StartOfSequence; /* TRUE: line is the first of a new sequence */
  int FieldType;       /* field_type: indentifies the field type: either even or odd */

  void copyInto(header & H) {
    H.Id = Id;
    H.NrOfSamples = NrOfSamples;
    H.StartOfField = StartOfField;
    H.StartOfSequence = StartOfSequence;
    H.FieldType = FieldType;
  }

  /*
  Object clone() {
    header hd = new header();
//    header hd = (header)super.clone();
    blackbox(SystemCSim)
    #ifdef DEBUG_PIP
      cout << "header: clone\n";
    #endif


      hd.Id = this.Id;
      hd.NrOfSamples = this.NrOfSamples;
      hd.StartOfField = this.StartOfField;
      hd.StartOfSequence = this.StartOfSequence;
      hd.FieldType = this.FieldType;
    return (Object)hd;


  }
  */
};


//////////////////////////////////////////////////
//// THSRC.mmm
/**
 *
 * @author Howard Wong-Toi
 */
//////////////////////////////////////////////////

/*
  THSRC process
  Same filter used for Y, U, and V
*/

class THSRC {
  /*
  port yapiininterface  Yi_Info;
  port yapiininterface  Yi_Data;
  port yapiininterface  hparams;
  port yapioutinterface Yo_Info;
  port yapioutinterface Yo_Data;
  */

public:
  header      Yi__Info;
  active_line Yi__Data;
  header      Yo__Info;
  active_line Yo__Data;


  // move declarations here so data can be accessed and modified in SRC_Y
  hcontrol_params hpar_;
  int zoom_tmp;
  int dzoom_tmp;
  bool StartOfField;


  THSRC(): Yi__Data(1), Yo__Data(1) {
    //super(n);
    //Yo__Info = new header();
    //Yi__Data = new active_line(1);
    //Yo__Data = new active_line(1);
  }

  void execute() {
    header *dummy = NULL; //= new header();
    hcontrol_params *dummyhpars = NULL; // = new hcontrol_params();
    StartOfField = false;
    zoom_tmp = hvsrc_dataconsts::SHIFT_C;
    dzoom_tmp = 0;

  /*
    while (true) {
      Yi_Info.read(dummy,1); Yi__Info = dummy;


      #ifdef DEBUG_PIP
        cout << basename() << ": Completed read of header (NrOfSamples=" <<
        (int)Yi__Info->NrOfSamples << ")" <<  endl;
      #endif



      Yi_Data.read(Yi__Data->c, (int)Yi__Info->NrOfSamples);

      #ifdef DEBUG_PIP
        cout << basename() << ": Completed read of data ("
           << Yi__Info->NrOfSamples << " samples)" << endl;
      #endif


      if (Yi__Info->StartOfField) {
        hparams.read(dummyhpars,1); hpar_ = dummyhpars;
        StartOfField = true;

        #ifdef DEBUG_PIP
          cout << "THSRC: Completed read of hparams" << endl;
        #endif

      }
  */

    // initialize values for proper execution
    hpar_.Samples_per_line = 960;
    hpar_.Samples_out = 480;
    hpar_.DDzoom = 1;
    hpar_.Dzoom = 1;
    hpar_.Zoom = 1;
    hpar_.Loop_Length = 100;
      SRC_Y();

  /*

      //      dummyo[0] = Yo__Info; Yo_Info.write(dummyo, 1);
      Yo_Info.write(Yo__Info);

      #ifdef DEBUG_PIP
        cout << "THSRC: completed writing header (NrOfSamples=" <<
        Yo__Info->NrOfSamples << ")" << endl;
      #endif
      // cout << name() << ": to write "
      //    << Yo__Info->NrOfSamples << " samples" << endl;


      Yo_Data.write(Yo__Data->c,Yo__Info->NrOfSamples);

      #ifdef DEBUG_PIP
        cout << name() << ": completed writing "
             << Yo__Info->NrOfSamples << " samples" << endl;
      #endif

    }
  */
  } // end of execute


  /************************************************************/
  /*   Yi, in, op : BITSIO                                    */
  /*   out         : BITSIO + 5                               */
  /*   sample_nr   : 12 bits                                  */
  /*   mode        : 10 bits                                  */
  /*                                                          */
  /************************************************************/
  void SRC_Y() {
        /* constants used:
                PIP,NORMAL,FIR,VAR_ZOOM,
                SHIFT_A,SHIFT_B,SHIFT_OFFST,
                ODDTAP,MUX,TAPS6,TAPS3,RNDFIRDIR,GRP
        */
    // "import" constants -- HWT
    int NORMAL = hvsrc_dataconsts::NORMAL;
    int SHIFT_A = hvsrc_dataconsts::SHIFT_A;
    int SHIFT_B = hvsrc_dataconsts::SHIFT_B;
    int SHIFT_OFFST = hvsrc_dataconsts::SHIFT_OFFST;
    int MSAL = vfconsts::MSAL;


    short *in, *op;
    // int start_delta;
    int offset_tmp;
    int ddzoom_tmp;
    int count;        // 12 bits
    int mode = hpar_.Mode; // 10 bits
    int samples_out; // 12 bits
    // int quant_filt = hpar_.quant_filt;
    int samples; // 12 bits
    int pos; // 12 bits
    int delta; // 8 bits
    int rcompr; // 8 bits
    int sample_nr; // 12 bits
    int out[MSAL+12]; // BITSIO + 6
    int in_tmp; // 11 bits

    int pixels_in, pixels_out;
    int poss;
    bool read_data, data_valid;  // HWT: were int
    // int dv_tmp;
    int addbegin, addend;

    // FILE *fp_coef;
    // fp_coef = fopen("coef.dat","a");


      #ifdef DEBUG_PIP
        cout << "\tIn SRY_Y" << endl;
      #endif




    Yi__Info.copyInto(Yo__Info);
    in = Yi__Data.c; // BITSIO bits
    op = Yo__Data.c; // BITSIO bits

    // initialise variables and use old results
    ddzoom_tmp = hpar_.DDzoom;
    if ((hpar_.Start & 0x8000) != 0) {

      // cout << "STARTING HIDDEN MODE!!!!" << endl;

        }
    if (((hpar_.Start & 0x8000) != 0) && (StartOfField == false)) {
      // dzoom_tmp -= ddzoom_tmp;
      // zoom_tmp += dzoom_tmp;
      dzoom_tmp = dzoom_tmp - ddzoom_tmp;
      zoom_tmp = zoom_tmp + dzoom_tmp;
    }
    else {
      zoom_tmp = hpar_.Zoom;
      dzoom_tmp = hpar_.Dzoom;
    }
    StartOfField = false;
    sample_nr = 0;
    offset_tmp = hpar_.Start & 0x00007FFF;
    addbegin = hpar_.addbegin;
    addend = hpar_.addend;
    pixels_in = hpar_.Samples_per_line;
    pixels_out = hpar_.Samples_out;
    for (samples=0; samples < vfconsts::MSAL; samples++) out[samples] = 0;
    if ((mode & NORMAL) != 0) // NORMAL MODE
      {
        for (samples=hpar_.Samples_per_line; samples >= 0; samples--) {
          in[samples+10] = in[samples];
        }
        for (samples=0; samples < 10; samples++) in[samples] = 128;
      }
    else
      {
        for (samples=hpar_.Samples_per_line; samples >= 0; samples--) {
          in[samples+4] = in[samples];
        }
        for (samples=0; samples < 4; samples++) in[samples] = 128;
      }

    pos = 0;


    #ifdef DEBUG_PIP
      cout << "THSRC: Loop_length=" << hpar_->Loop_Length << endl;
    #endif


    for (count=0; count < hpar_.Loop_Length-1; count++) {
      // HERE STARTS hsrc_calc_delta_pu
      delta = ((offset_tmp >> hpar_.Phase_shift) & hpar_.Phase_and);
      poss = offset_tmp >> SHIFT_OFFST;                // 12 bits

      if (addbegin != 0) {
        data_valid = true;
        read_data = false;
        addbegin--;
      }
      else {
        if ((mode & NORMAL) != 0) // NORMAL MODE
          {
            if (poss != 0) {
              offset_tmp = offset_tmp - (1 << SHIFT_OFFST);
              read_data = true;
            }
            else {
              offset_tmp = offset_tmp;
              read_data = false;
            }
            if (poss > 1) data_valid = false;
            else data_valid = true;
          }
        else // TRANSPOSED MODE  (NOT NORMAL)
          {
            read_data = true;
            data_valid = false;
            if (poss != 0) offset_tmp = offset_tmp - (1 << SHIFT_OFFST);
            if (poss == 1) data_valid = true;
          }

        if (poss > 1)
          {
            // dzoom_tmp = dzoom_tmp;
            // zoom_tmp = zoom_tmp;
          }
        else
          {
            if ((hpar_.Start & 0x8000) == 0)
              {
                // dzoom_tmp -= ddzoom_tmp;
                // zoom_tmp += dzoom_tmp;
                dzoom_tmp = dzoom_tmp - ddzoom_tmp;
                zoom_tmp = zoom_tmp + dzoom_tmp;
              }
            offset_tmp += (zoom_tmp >> SHIFT_A);
          }

        if ((hpar_.Start & 0x8000) != 0 && (pos >= pixels_in)) data_valid = true;

        if (sample_nr >= pixels_out) {
          data_valid = false;
          read_data = true;
        }
        if (pos >= pixels_in) {
          read_data = false;
        }
        if ((data_valid == false) && (read_data == false) && (addend != 0)) {
          addend--;
          data_valid = true;
        }
      }

      // HERE ENDS hsrc_calc_delta_pu

      rcompr = ((zoom_tmp >> SHIFT_B) & 255);
      if ((mode & NORMAL) == NORMAL) {
        if (read_data == true) pos++;
        out[sample_nr] =
          (hpar_.phases_int[delta][5] * in[pos  ]) +
          (hpar_.phases_int[delta][4] * in[pos+1]) +
          (hpar_.phases_int[delta][3] * in[pos+2]) +
          (hpar_.phases_int[delta][2] * in[pos+3]) +
          (hpar_.phases_int[delta][1] * in[pos+4]) +
          (hpar_.phases_int[delta][0] * in[pos+5]);

        if (data_valid == true) sample_nr++;
      }
      else {
        if (data_valid == true) sample_nr++;
        if (read_data == true) {
          in_tmp = ((in[pos] * rcompr) >> 7);

          out[sample_nr  ] += in_tmp * hpar_.phases_int[delta][5];
          out[sample_nr+1] += in_tmp * hpar_.phases_int[delta][4];
          out[sample_nr+2] += in_tmp * hpar_.phases_int[delta][3];
          out[sample_nr+3] += in_tmp * hpar_.phases_int[delta][2];
          out[sample_nr+4] += in_tmp * hpar_.phases_int[delta][1];
          out[sample_nr+5] += in_tmp * hpar_.phases_int[delta][0];
          pos++;
        }
      }
      // checking some variables
      if (zoom_tmp < 0)
        {

          //cout << "ERROR: zoom < 0: exiting !!!" << endl;
            exit(1);


        }
    }
    samples_out = sample_nr;
    // cout << "Samples out" << samples_out << endl;

    // clip and round data correctly
    Yo__Info.NrOfSamples = (short) samples_out;
    op[0] = 0;


    for (sample_nr=0;sample_nr<samples_out;sample_nr++)
      {
        op[sample_nr+1] = CLIP((short)((out[sample_nr] + (1 << (hpar_.quant_filt-1))) >> hpar_.quant_filt), (short)hpar_.clp_min, (short)hpar_.clp_max);
      }
    // fclose(fp_coef);
  } // end SRC_Y

 private:
  short CLIP(short a, short b, short c) {
    if (a<b)
      return b;
    else {
      if (a>c)
        return c;
      else
        return a;
    }
  }

};

int main(void) {
  THSRC temp;
  temp.execute();

  return 0;
}











