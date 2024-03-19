/* PcxReader: This is a class which provides a method for reading
 * PCX-Files.
 * The PCX-Format is a Image-File-Format which was developed by ZSoft.
 *
 * PcxReader, Version 1.00 [06/05/2000]
 * Copyright (c) 2000 by Matthias Burg
 * All rights reserved
 * eMail: Matthias@burgsoft.de
 * Internet: www.burgsoft.de
 *
 * The PcxReader is Freeware. You can use and copy it without any fee.
 * The author is not responsible for any damages which are caused by
 * this software.
 * You find further information about the Java-Technology on the Sun-
 * Website (Internet: www.sun.com).
 *
 * At the moment PcxReader supports the following File-Formats:
 *   - PCX Version 3.0 with 8 Bit (=256) Colors
 *   - PCX Version 3.0 with 24 Bit (=16.7 Mio) Colors
 *
 * The PcxReader needs an opened InputStream with the PCX-Data as
 * Argument. The return-value is the loaded Image.
 * You can use the PcxReader in a Java-Application as well as in a
 * Java-Applet
 *
 * If you have questions or tips for the PcxReader, please write an
 * eMail.
 */

package tm.gfxlibs;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;


public class PCXReader
{
  /* This is the main-class of the PcxReader. It reads the PCX-Data
   * from a stream and converts it into an Image-Class.
   */

  public static final int NORMAL = 1;
  public static final int RLE = 2;
  
  public static Image loadImage(InputStream in)
  {
    int pcxheight, pcxwidth;
    Image picture = null;

    //Header-Data
    int manufacturer;
    int version;
    int encoding;
    int bits_per_pixel;
    int xmin,ymin;
    int xmax,ymax;
    int hres;
    int vres;
    byte[] palette16 = new byte[48];
    int reserved;
    int color_planes;
    int bytes_per_line;
    int palette_type;
    byte[] filler = new byte[58];

    int imagebytes;
    try
    {
      /* In the beginning the Image-Data is read as in the PCX-
       * specification.
       */
      manufacturer = in.read();
      version = in.read();
      encoding = in.read();
      bits_per_pixel = in.read();

      xmin = in.read()+in.read()*256;   
      ymin = in.read()+in.read()*256;
      xmax = in.read()+in.read()*256;
      ymax = in.read()+in.read()*256;
      hres = in.read()+in.read()*256;
      vres = in.read()+in.read()*256;
      in.read(palette16);
      reserved = in.read();
      color_planes = in.read();
      bytes_per_line = in.read()+in.read()*256;
      palette_type = (short)(in.read()+in.read()*256);
      in.read(filler);


      pcxwidth = 1+xmax-xmin;
      pcxheight = 1+ymax-ymin;
      if(pcxwidth % 2 == 1)
      {
        /* The width of an PCX-Image must be even. That is why the
         * width is increased when it was odd before.
         */
        pcxwidth++;
      }


      if(bits_per_pixel == 8 && color_planes == 1)
      {
        /* If the PCX-file has 256 colors there is a color-palete
         * at the end of the file. This is 768b bytes long and
         * contains the red- green- and blue-values of the colors.
         */
        byte[] pal = new byte[768];
        int[] intPal = new int[768];

        imagebytes = (pcxwidth*pcxheight);
        int[] imageData = new int[imagebytes];
        readRLECompressedData(imagebytes, imageData, in);        

        if(in.available() > 769)
        {
          while(in.available() > 769)
          {
            in.read();
          }
        }
      
        if(in.available() != 769)
        {
          System.out.println("Error in the palette!");
        }
        if(in.read()!=12)
        {
          System.out.println("Error in the palette!");
        }

        in.read(pal);
        in.close();
        for(int y = 0; y <767;y++)
        {
          intPal[y] = (int)(pal[y]);
          if(intPal[y] < 0)
          {
            intPal[y] += 256;
          }
        }

        /* Now the PcxReader converts the imagedata into the format
         * of a MemoryImageSource.
         */

        int RGBImageData[] = new int[imagebytes];
        for(int i = 0; i < imagebytes; i++)
        {
          int paletteEntry = imageData[i];
          if(paletteEntry < 0) paletteEntry += 256;
          RGBImageData[i] = new Color(intPal[paletteEntry*3],
                                      intPal[paletteEntry*3+1],
                                      intPal[paletteEntry*3+2]).getRGB();
        }

        ImageProducer prod = new MemoryImageSource(pcxwidth, pcxheight, RGBImageData, 0, pcxwidth);
        picture = Toolkit.getDefaultToolkit().createImage(prod);

      }
      else if(bits_per_pixel == 8 && color_planes == 3)
      {
        /* If the picture has 24 bit colors, there are 3 times many         
         * bytes as many pixels.
         */

        imagebytes = (pcxwidth*pcxheight*3);
        int[] imageData = new int[imagebytes];
        readRLECompressedData(imagebytes, imageData, in);
        
        
        int RGBImageData[] = new int[imagebytes];
        for(int i = 0; i < pcxheight; i++)
        {
          for(int j = 0; j < pcxwidth; j++)
          {
            int red = imageData[i*3*pcxwidth+j];
            int green = imageData[((i*3)+1)*pcxwidth+j];
            int blue = imageData[((i*3)+2)*pcxwidth+j];
            RGBImageData[i*pcxwidth+j] = new Color(red,green,blue).getRGB();
          }
        }

        ImageProducer prod = new MemoryImageSource(pcxwidth, pcxheight, RGBImageData, 0, pcxwidth);
        picture = Toolkit.getDefaultToolkit().createImage(prod);
      
      }
    }
    catch (IOException e)
    {
      System.out.println("Error reading PCX-File!");
    }
    
    return picture;
  }

  private static void readRLECompressedData(int imagebytes, int[] imageData, InputStream in)
    throws IOException
  {
    /* This method reads the compressed data-stream and decompresses 
     * it in the memory.
     */

    int i;
    int mode=NORMAL,nbytes=0;
    int abyte = 0;

    for(i = 0; i<imagebytes;i++)
    {
      if (mode == NORMAL)
      {
        abyte = in.read();
        if(abyte > 191)
        {
          nbytes=abyte-192;
          abyte =(byte)(in.read());
          if (--nbytes > 0)
          {
            mode = RLE;
          }
        }
      }
      else if(--nbytes == 0)
      {
       mode = NORMAL;
      }
        
      imageData[i] = abyte;
      if(imageData[i] < 0) imageData[i] += 256;
    }    
  }
}


