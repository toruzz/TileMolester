package tm.osbaldeston.image;

/**
  * BMP - Wrapper Class  for the loading & saving of uncompressed BMP files.
  *
  * Known problems/issues:
  *     Only 24 bit BMP files are output, images get converted to 24-bit 
  *     which corresponds to Java's default colour model, output from 
  *     PixelGrabber in Java 1.1.x.
  *
  * @see PCBinaryInputStream, PCBinaryOutputStream
  *
  * @author Richard J.Osbaldeston
  * @version 1.1 02/08/98
  * @copyright Richard J.Osbaldeston (http://www.osbald.co.uk)
  */

import java.awt.Image;
import java.awt.image.*;
import java.awt.Toolkit;
import java.io.*;
import java.net.URL;
import tm.osbaldeston.io.*;

public class BMP {

    private java.awt.Image image;
    private BmpFileheader bmp_fileheader = new BmpFileheader();
    private BmpInfoHeader bmp_infoheader = new BmpInfoHeader();
    private BmpPalette bmp_palette;    
    private int width;
    private int height;

    /**
     * Creates an BMP by reading from the given File.
     *
     * @param f File to read BMP from
     */
    public BMP(File f) {
        try {
            PCBinaryInputStream file = new PCBinaryInputStream(f);
            read(file);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Creates an BMP by reading from the given File.
     *
     * @param url URL to read BMP from
     */
    public BMP(URL url) {
        try {
            PCBinaryInputStream file = new PCBinaryInputStream(url);
            read(file);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Creates an BMP from the given Image, (for writing).
     */
    public BMP(Image image) {
        this.image=image;
        width=image.getWidth(null);
        height=image.getHeight(null);
        bmp_infoheader.biWidth = (short)width;
        bmp_infoheader.biHeight = (short)height;
        bmp_infoheader.biBitCount = 24;
        bmp_infoheader.biClrUsed = 0;
    }

    /**
     * Returns the image loaded, last saved image or null if unused.
     *
     * @return the current image
     */
    public Image getImage() {
        return image;
    }

    /**
     * Saves the BMP in a given file.
     *
     * @param f File to write BMP
     */
    public void write(File f) {
        try {
            PCBinaryOutputStream file = new PCBinaryOutputStream(f);
            write(file);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Saves the BMP in a given file.
     *
     * @param url URL to write BMP
     */
    public void write(URL url) {
        try {
            PCBinaryOutputStream file = new PCBinaryOutputStream(url);
            write(file);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Saves the BMP in a given file.
     */
    void write(PCBinaryOutputStream file) {
        try {
            int rawData[] = new int[width*height];
            PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, rawData, 0, width);
            bmp_palette = new BmpPalette(grabber.getColorModel());
                          
            try {
                grabber.grabPixels();
            } catch (InterruptedException e) { System.err.println(e); }

            ColorModel model = grabber.getColorModel();

            bmp_fileheader.bfOffBits = bmp_fileheader.getSize() + 
                                       bmp_infoheader.getSize() +
                                       bmp_palette.getSize();
            int x=0,y=0,i=0,j=0,k=0;
            byte bitmap[];
            int w=width*3;              
            int h=height-1;

            // dword align width            
            if (w%4 != 0) 
                w += (4-(w%4));

            if (model instanceof IndexColorModel) {
                // not tested - (can't get IndexColorModel from PixelGrabber??!)
                bitmap = new byte[width*bmp_infoheader.biHeight];
                for (y=h;y>=0;y--) {
                    i=(h-y)*width;
                    j=y*width;                    
                    for (x=0;x<width;x++) {
                        bitmap[j++]=(byte)(rawData[i+x]);
                    }
                }
                bmp_fileheader.bfSize = bmp_fileheader.bfOffBits + 
                                        width*bmp_infoheader.biHeight;
            }
            else {
                bitmap = new byte[w*bmp_infoheader.biHeight];
                for (y=0;y<height;y++) {
                   i=(h-y)*width;                   
                   j=y*w;                    
                   for (x=0;x<width;x++) {
                       k=i+x;
                       bitmap[j++]=(byte)((model.getBlue(rawData[k]))&0xFF);
                       bitmap[j++]=(byte)((model.getGreen(rawData[k]))&0xFF);
                       bitmap[j++]=(byte)((model.getRed(rawData[k]))&0xFF);
                   }
                }
            }                                   
            rawData = null;
            // bmp_infoheader.biWidth=w;
            bmp_fileheader.bfSize = bmp_fileheader.bfOffBits + w*bmp_infoheader.biHeight;
                        
            bmp_fileheader.write(file);
            bmp_infoheader.write(file);
            bmp_palette.write(file);
            file.writeByteArray(bitmap);            
            bitmap = null;
            file.close();
        } catch (IOException e) { System.err.println(e); }
    }

    /**
     * Read a BMP from a given file.
     */
    void read(PCBinaryInputStream file) {
        int coloursUsed = 0;
        int scanlineSize = 0;
        int bitplaneSize = 0;
        byte [] rawData = null;

        try {
            bmp_fileheader.read(file);
            bmp_infoheader.read(file);

            if (bmp_infoheader.biClrUsed != 0)
                coloursUsed = bmp_infoheader.biClrUsed;
            else if (bmp_infoheader.biBitCount < 16)
                coloursUsed = 1 << bmp_infoheader.biBitCount;

            bmp_palette = new BmpPalette(coloursUsed);
            bmp_palette.read(file);

            long skip =  bmp_fileheader.bfOffBits - 
                        (bmp_fileheader.getSize()+
                         bmp_infoheader.getSize()+
                         bmp_palette.getSize());
                         
            if (skip > 0) {
                file.skip(skip);
            }

            scanlineSize = ((bmp_infoheader.biWidth*bmp_infoheader.biBitCount+31)/32)*4;

            if (bmp_infoheader.biSizeImage != 0)
                bitplaneSize = bmp_infoheader.biSizeImage;
            else
                bitplaneSize = scanlineSize * bmp_infoheader.biHeight;

            rawData = new byte[bitplaneSize];
            file.readByteArray(rawData);
            file.close();

        } catch (IOException e) {
            System.err.println(e);
        }

        if (rawData != null) {
            if (bmp_infoheader.biBitCount > 8) {
                image = unpack24(rawData, scanlineSize);
            }
            else {
                image = unpack08(rawData, scanlineSize);
            }
        }
        rawData = null;
    }

    Image unpack24(byte [] rawData, int scanlineSize) {
        int b=0, k=0, x=0, y=0;
        int [] data = new int[bmp_infoheader.biWidth * bmp_infoheader.biHeight];
        try {
            for (y=0; y < bmp_infoheader.biHeight; y++) {
              b=(bmp_infoheader.biHeight-1-y)*bmp_infoheader.biWidth;
              k=y*scanlineSize;
              for (x=0; x < bmp_infoheader.biWidth; x++) {
                data[x+b] = 0xFF000000 |
                         (((int)(rawData[k++])) & 0xFF) |
                         (((int)(rawData[k++])) & 0xFF) << 8 |
                         (((int)(rawData[k++])) & 0xFF) << 16;
              }
            }
        }catch (Exception e) {};
        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bmp_infoheader.biWidth, bmp_infoheader.biHeight, ColorModel.getRGBdefault(), data, 0, bmp_infoheader.biWidth));
    }

    Image unpack08(byte [] rawData, int scanlineSize) {
        int b=0, k=0, i=0, x=0, y=0;
        byte [] data = new byte[bmp_infoheader.biWidth * bmp_infoheader.biHeight];
        try {
        if (bmp_infoheader.biBitCount == 1) {            
            for (y=0; y < bmp_infoheader.biHeight; y++) {
              b=(bmp_infoheader.biHeight-1-y)*bmp_infoheader.biWidth;
              k=y*scanlineSize;
              for (x=0; x < (bmp_infoheader.biWidth-8); x+=8) {
                data[b+x+7] = (byte)((rawData[k]) & 1);
                data[b+x+6] = (byte)((rawData[k] >>> 1) & 1);
                data[b+x+5] = (byte)((rawData[k] >>> 2) & 1);
                data[b+x+4] = (byte)((rawData[k] >>> 3) & 1);
                data[b+x+3] = (byte)((rawData[k] >>> 4) & 1);
                data[b+x+2] = (byte)((rawData[k] >>> 5) & 1);
                data[b+x+1] = (byte)((rawData[k] >>> 6) & 1);
                data[b+x]   = (byte)((rawData[k] >>> 7) & 1);
                k++;
              }
              for (i=7; i>=0 ; i--) {
                 if ((i+x)< bmp_infoheader.biWidth) {
                     data[b+x+i] = (byte)((rawData[k] >>> (7-i)) & 1);
                 }
              }
            }
        } else if (bmp_infoheader.biBitCount == 4) {
            for (y=0; y < bmp_infoheader.biHeight; y++) {
              b=(bmp_infoheader.biHeight-1-y)*bmp_infoheader.biWidth;
              k=y*scanlineSize;
              for (x=0; x < (bmp_infoheader.biWidth-2); x+=2) {
                data[b+x]   = (byte)((rawData[k]>>4) & 0x0F);
                data[b+x+1] = (byte)((rawData[k] & 0x0F));
                k+=1;
              }
              for (i=1; i>=0 ; i--) {
                 if ((i+x)< bmp_infoheader.biWidth) {
                     data[b+x+i] = (byte)((rawData[k] >>> ((1-i)<<2)) & 0x0F);
                 }
              }              
            }
        } else {
            for (y=0; y < bmp_infoheader.biHeight; y++) {
              b=(bmp_infoheader.biHeight-1-y)*bmp_infoheader.biWidth;
              k=y*scanlineSize;
              for (x=0; x < bmp_infoheader.biWidth; x++) {
                data[x+b] = (byte)(rawData[k++] & 0xFF);
              }
            }
        }
        }catch (Exception e) {};

        ColorModel colourModel = new IndexColorModel(bmp_infoheader.biBitCount, bmp_palette.length, bmp_palette.r, bmp_palette.g, bmp_palette.b);
        return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bmp_infoheader.biWidth, bmp_infoheader.biHeight, colourModel, data, 0, bmp_infoheader.biWidth));
    }

    /**
    * .BMP InfoHeader
    */

    class BmpInfoHeader {
        int   biSize = 40;                  /* InfoHeader Offset*/
        int   biWidth;                      /* Width */
        int   biHeight;                     /* Height */
        short biPlanes = 1;                 /* BitPlanes on Target Device */
        short biBitCount;                   /* Bits per Pixel */
        int   biCompression;                /* Bitmap Compression */
        int   biSizeImage;                  /* Bitmap Image Size */
        int   biXPelsPerMeter;              /* Horiz Pixels Per Meter */
        int   biYPelsPerMeter;              /* Vert Pixels Per Meter */
        int   biClrUsed;                    /* Number of ColorMap Entries */
        int   biClrImportant;               /* Number of important colours */
        
        int getSize() {
           return biSize;
        }

        void read(PCBinaryInputStream file) {
            try {
                biSize = file.readInt();
                if (biSize == 12) {
                    biWidth = file.readShort();
                    biHeight = file.readShort();
                    biPlanes = file.readShort();
                    biBitCount = file.readShort();
                }
                else
                {
                    biWidth = file.readInt();
                    biHeight = file.readInt();
                    biPlanes = file.readShort();
                    biBitCount = file.readShort();
                    biCompression = file.readInt();
                    biSizeImage = file.readInt();
                    biXPelsPerMeter = file.readInt();
                    biYPelsPerMeter = file.readInt();
                    biClrUsed = file.readInt();
                    biClrImportant = file.readInt();
                }
            } catch (IOException e) { System.err.println(e); }

            if (biSizeImage == 0)
                biSizeImage = (((biWidth*biBitCount+31)>>5)<<2)*biHeight;

            if (biClrUsed == 0 && biBitCount < 16)
                biClrUsed = 1 << biBitCount;
        }

        void write(PCBinaryOutputStream file) {
            try {
                file.writeInt(biSize);
                if (biSize == 12) {
                    file.writeShort((short)biWidth);
                    file.writeShort((short)biHeight);
                    file.writeShort(biPlanes);
                    file.writeShort(biBitCount);
                }
                else {
                    file.writeInt(biWidth);
                    file.writeInt(biHeight);
                    file.writeShort(biPlanes);
                    file.writeShort(biBitCount);
                    file.writeInt(biCompression);
                    file.writeInt(biSizeImage);
                    file.writeInt(biXPelsPerMeter);
                    file.writeInt(biYPelsPerMeter);
                    file.writeInt(biClrUsed);
                    file.writeInt(biClrImportant);
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    /**
    * .BMP FileHeader
    */

    class BmpFileheader {
        byte    bfType[] = {'B','M'};       /* Type */
        int     bfSize;                     /* File Size */
        short   bfReserved1=0;              /* Reserved 1 */
        short   bfReserved2=0;              /* Reserved 2 */
        int     bfOffBits;                  /* Offset to Data */

        int getSize() {
           return 14;
        }

        void read(PCBinaryInputStream file) {
            try {
                bfType[0] = file.readByte();
                bfType[1] = file.readByte();
                if (bfType[0] != 'B' && bfType[1] != 'M')
                    throw new IOException("Invalid BMP 3.0 File.");
                bfSize = file.readInt();
                bfReserved1 = file.readShort();
                bfReserved2 = file.readShort();
                bfOffBits = file.readInt();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        void write(PCBinaryOutputStream file) {
            try {
            file.writeByte(bfType[0]);
            file.writeByte(bfType[1]);
            file.writeInt(bfSize);
            file.writeShort(bfReserved1);
            file.writeShort(bfReserved2);
            file.writeInt(bfOffBits);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    /**
    * .BMP Palette
    */

    class BmpPalette {
        int length;
        byte r[];
        byte g[];
        byte b[];

        public BmpPalette(int length)
        {
            this.length = length;
            r = new byte[length];
            g = new byte[length];
            b = new byte[length];
        }

        int getSize() {
           return length*4;
        }

        public BmpPalette(ColorModel colourModel) {
            if (colourModel instanceof IndexColorModel) {
                IndexColorModel indexColourModel = (IndexColorModel)colourModel;
                this.length = indexColourModel.getMapSize();
                r = new byte[length];
                indexColourModel.getReds(r);
                g = new byte[length];
                indexColourModel.getGreens(g);
                b = new byte[length];
                indexColourModel.getBlues(b);
                bmp_infoheader.biBitCount = (short)indexColourModel.getPixelSize();
                bmp_infoheader.biClrUsed = length;
            }
        }

        void read(PCBinaryInputStream file) {
            if (length > 0) {
                try {
                    byte reserved;
                    for (int i=0; i < length; i++) {
                        b[i] = file.readByte();     // blue
                        g[i] = file.readByte();     // green
                        r[i] = file.readByte();     // red
                        reserved = file.readByte(); // reserved
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }

        void write(PCBinaryOutputStream file) {
            if (length > 0) {
                try {
                    byte reserved = 0;
                    for (int i=0; i < length; i++) {
                        file.writeByte(b[i]);
                        file.writeByte(g[i]);
                        file.writeByte(r[i]);
                        file.writeByte(reserved);
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

}