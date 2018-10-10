package android.os;

import android.graphics.Rect;


public class DisplayManager {
    /**
     * Format and its value
     * It must be defined same as hi_unf_display.h
     * */
    public final static int ENC_FMT_1080P_60 = 0;         /*1080p60hz*/
    public final static int ENC_FMT_1080P_50 = 1;         /*1080p50hz*/
    public final static int ENC_FMT_1080P_30 = 2;         /*1080p30hz*/
    public final static int ENC_FMT_1080P_25 = 3;         /*1080p25hz*/
    public final static int ENC_FMT_1080P_24 = 4;         /*1080p24hz*/
    public final static int ENC_FMT_1080i_60 = 5;         /*1080i60hz*/
    public final static int ENC_FMT_1080i_50 = 6;         /*1080i60hz*/
    public final static int ENC_FMT_720P_60 = 7;          /*720p60hz*/
    public final static int ENC_FMT_720P_50 = 8;          /*720p50hz*/
    public final static int ENC_FMT_576P_50 = 9;          /*576p50hz*/
    public final static int ENC_FMT_480P_60 = 10;         /*480p60hz*/
    public final static int ENC_FMT_PAL = 11;             /*BDGHIPAL*/
    public final static int ENC_FMT_PAL_N = 12;           /*(N)PAL*/
    public final static int ENC_FMT_PAL_Nc = 13;          /*(Nc)PAL*/
    public final static int ENC_FMT_NTSC = 14;            /*(M)NTSC*/
    public final static int ENC_FMT_NTSC_J = 15;          /*NTSC-J*/
    public final static int ENC_FMT_NTSC_PAL_M = 16;      /*(M)PAL*/
    public final static int ENC_FMT_SECAM_SIN = 17;
    public final static int ENC_FMT_SECAM_COS = 18;
    public final static int ENC_FMT_861D_640X480_60 = 19;
    public final static int ENC_FMT_VESA_800X600_60 = 20;
    public final static int ENC_FMT_VESA_1024X768_60 = 21;
    public final static int ENC_FMT_VESA_1280X720_60 = 22;
    public final static int ENC_FMT_VESA_1280X800_60 = 23;
    public final static int ENC_FMT_VESA_1280X1024_60 = 24;
    public final static int ENC_FMT_VESA_1366X768_60 = 25;
    public final static int ENC_FMT_VESA_1440X900_60 = 26;
    public final static int ENC_FMT_VESA_1440X900_60_RB = 27;
    public final static int ENC_FMT_VESA_1600X1200_60 = 28;
    public final static int ENC_FMT_VESA_1920X1080_60 = 29;
    public final static int ENC_FMT_VESA_1920X1200_60 = 30;
    public final static int ENC_FMT_VESA_2048X1152_60 = 31;
    public final static int ENC_FMT_VESA_CUSTOMER_DEFINE =32;

    public final static int HI_DISP_MODE_NORMAL = 0;
    public final static int HI_DISP_MODE_SIDE_BY_SIDE = 1;
    public final static int HI_DISP_MODE_TOP_BOTTOM = 2;
    
	public Rect getOutRange() {
		// TODO Auto-generated method stub
		return null;
	}
	
    public int getFmt()
    {
        return -1;
    }
}
