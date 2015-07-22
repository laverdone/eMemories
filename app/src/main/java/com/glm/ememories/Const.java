package com.glm.ememories;

import android.graphics.Color;
import android.os.Environment;

public class Const {
	/**altezza di base da cui poi scalare per le altre risoluzioni*/
	public static final int DEFAULT_HEIGTH=1920;
	/**larghezza di base da cui poi scalare per le altre risoluzioni*/
	public static final int DEFAULT_WIDTH=1500;
	/**margine standard destro*/
	public static final int DEFAULT_MARGIN_SX=40;
	/**margine standard sinistro*/
	public static final int DEFAULT_MARGIN_RX=150;
	/**definisce il margine standard della prima riga*/
	public static final int DEFAULT_OFFSET_TOP=250;
	/**definisce l'offset standard tra le righe*/
	public static final int DEFAULT_OFFSET_ROW=75;
	/**definisce l'offset standard per il giorno*/
	public static final int DATE_DAY_OFFSET_Y=130;
	/**definisce l'offset standard per il giorno*/
	public static final int DATE_MONTH_OFFSET_Y=170;
	/**definisce il colore della data*/
	public static final int COLOR_DATE = Color.parseColor("#e78383");
    /**definisce il colore alternativo della data*/
    public static final int COLOR_DATE_ALTERNATE = Color.parseColor("#5CCCCC");
    /**definisce il colore della data*/
    public static final int COLOR_LINE = Color.parseColor("#000000") ;
	/**indica il margine sinistro del mese*/
	public static final int DATE_MONTH_OFFSET_X = 1250;
	/**indica il margine sinistro del mese*/
	public static final int DATE_DAY_OFFSET_X = 1225;

    /**Cartella di storage esterna*/
	public static final String EXTDIR = Environment.getExternalStorageDirectory().getPath() + "/";
    /**Cartella di storage interna*/
    public static final String INTERNALDIR = Environment.getDataDirectory() + "/data/";
	/**identifica il fattore di moltipricazione per la dimensione del finger*/
	public static final float STROKEFACTOR = 1800;
	/**Dimensioni Immagini */
	public static final int IMGWIDTH=1204;
	public static final int IMGHEIGHT=768;
	/**Sample Size Immagine*/
	public static final int SAMPLESIZEIMAGE = 4;
    /**Sample Size Immagine for page preview*/
    public static final int SAMPLESIZEDIARY = 1;
    /**sample per le immagini da lavorare*/
    public static final int WORKIZEIMAGE = 2;
    /**PagePreview extension*/
    public static final String PAGE_PREVIEW_EXT=".prw";
    /**Camera Image extension*/
    public static final String CAMERA_PREVIEW_EXT=".cix";
    /**Share Image extension*/
    public static final String CAMERA_SHARE_EXT = ".png";
    /**Definisce gli effetti disponibili*/
    public static final String[] PICTURE_FILTER= new String[]{"- NOTHING -",
            "HighlightEffect","InvertEffect","GreyscaleEffect",
            "GammaEffect","ColorFilterEffect","SepiaToningEffect",
            "DecreaseColorDepthEffect","ContrastEffect",
            "BrightnessEffect","GaussianBlurEffect",
            "SharpenEffect","MeanRemovalEffect","SmoothEffect",
            "EmbossEffect","EngraveEffect","BoostEffect",
            "RoundCornerEffect","WaterMarkEffect","TintEffect",
            "FleaEffect","BlackFilter","SnowEffect",
            "ShadingFilter","SaturationFilter","HueFilter","Reflection",
            "Old Paper","Frame","Seam","Clip Paper","Pellicle","Broker Glass"};

    public static final String CLOUD_COLLECTION="eMemories";

    public static final String CLOUD_APPCODE = "1234567890";

    public static final int colorTheme1 =  Color.parseColor("#009688");
    public static final int colorTheme2 =  Color.parseColor("#795548");
    public static final int colorTheme3 =  Color.parseColor("#795548");
    public static final int colorTheme4 =  Color.parseColor("#FFC107");
    public static final int colorTheme5 =  Color.parseColor("#9E9E9E");
    public static final int colorTheme6 =  Color.parseColor("#F44336");

}
