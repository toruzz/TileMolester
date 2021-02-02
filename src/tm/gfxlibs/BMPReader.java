package tm.gfxlibs;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

// This class provides a public static method that takes an InputStream
// to a Windows .BMP file and converts it into an ImageProducer via
// a MemoryImageSource.
// You can fetch a .BMP through a URL with the following code:
// URL url = new URL( <wherever your URL is> )
// Image img = createImage(BMPReader.getBMPImage(url.openStream()));

public class BMPReader extends Object
{
// Constants indicating how the data is stored
    public static final int BI_RGB = 0;
    public static final int BI_RLE8 = 1;
    public static final int BI_RLE4 = 2;

    public static ImageProducer getBMPImage(InputStream stream)
    throws IOException
    {
// The DataInputStream allows you to read in 16 and 32 bit numbers
        DataInputStream in = new DataInputStream(stream);

// Verify that the header starts with 'BM'

        if (in.read() != 'B') {
            throw new IOException("Not a .BMP file");
        }
        if (in.read() != 'M') {
            throw new IOException("Not a .BMP file");
        }

// Get the total file size
        int fileSize = intelInt(in.readInt());

// Skip the 2 16-bit reserved words
        in.readUnsignedShort();
        in.readUnsignedShort();

        int bitmapOffset = intelInt(in.readInt());

        int bitmapInfoSize = intelInt(in.readInt());

        int width = intelInt(in.readInt());
        int height = intelInt(in.readInt());

// Skip the 16-bit bitplane size
        in.readUnsignedShort();

        int bitCount = intelShort(in.readUnsignedShort());

        int compressionType = intelInt(in.readInt());

        int imageSize = intelInt(in.readInt());

// Skip pixels per meter
        in.readInt();
        in.readInt();

        int colorsUsed = intelInt(in.readInt());
        int colorsImportant = intelInt(in.readInt());
        if (colorsUsed == 0) colorsUsed = 1 << bitCount;

        int colorTable[] = new int[colorsUsed];

// Read the bitmap's color table
        for (int i=0; i < colorsUsed; i++) {
            colorTable[i] = (intelInt(in.readInt()) & 0xffffff) + 0xff000000;
        }

// Create space for the pixels
        int pixels[] = new int[width * height];

// Read the pixels from the stream based on the compression type
        if (compressionType == BI_RGB) {
            if (bitCount == 24) {
                readRGB24(width, height, pixels, in);
            } else {
                readRGB(width, height, colorTable, bitCount,
                    pixels, in);
            }
        } else if (compressionType == BI_RLE8) {
            readRLE(width, height, colorTable, bitCount,
                pixels, in, imageSize, 8);
        } else if (compressionType == BI_RLE4) {
            readRLE(width, height, colorTable, bitCount,
                pixels, in, imageSize, 4);
        }

// Create a memory image source from the pixels
        return new MemoryImageSource(width, height, pixels, 0,
            width);
    }

// Reads in pixels in 24-bit format. There is no color table, and the
// pixels are stored in 3-byte pairs. Oddly, all windows bitmaps are
// stored upside-down - the bottom line is stored first.

    protected static void readRGB24(int width, int height, int pixels[],
        DataInputStream in)
    throws IOException
    {

// Start storing at the bottom of the array
        for (int h = height-1; h >= 0; h--) {
         int pos = h * width;
            for (int w = 0; w < width; w++) {

// Read in the red, green, and blue components
          int red = in.read();
          int green = in.read();
          int blue = in.read();

// Turn the red, green, and blue values into an RGB color with
// an alpha value of 255 (fully opaque)
                pixels[pos++] = 0xff000000 + (red << 16) +
               (green << 8) + blue;
            }
        }
    }

// readRGB reads in pixels values that are stored uncompressed.
// The bits represent indices into the color table.

    protected static void readRGB(int width, int height, int colorTable[],
        int bitCount, int pixels[], DataInputStream in)
    throws IOException
    {

// How many pixels can be stored in a byte?
        int pixelsPerByte = 8 / bitCount;

// A bit mask containing the number of bits in a pixel
        int bitMask = (1 << bitCount) - 1;

// The shift values that will move each pixel to the far right
        int bitShifts[] = new int[pixelsPerByte];

        for (int i=0; i < pixelsPerByte; i++) {
            bitShifts[i] = 8 - ((i+1) * bitCount);
        }

        int whichBit = 0;

// Read in the first byte
        int currByte = in.read();

// Start at the bottom of the pixel array and work up
        for (int h=height-1; h >= 0; h--) {
            int pos = h * width;
            for (int w=0; w < width; w++) {

// Get the next pixel from the current byte
                pixels[pos] = colorTable[
                    (currByte >> bitShifts[whichBit]) &
                    bitMask];
          pos++;
                whichBit++;

// If the current bit position is past the number of pixels in
// a byte, we advance to the next byte
                if (whichBit >= pixelsPerByte) {
                    whichBit = 0;
                    currByte = in.read();
                }
            }
        }
    }


// readRLE reads run-length encoded data in either RLE4 or RLE8 format.

    protected static void readRLE(int width, int height, int colorTable[],
        int bitCount, int pixels[], DataInputStream in,
        int imageSize, int pixelSize)
    throws IOException
    {
        int x = 0;
        int y = height-1;

// You already know how many bytes are in the image, so only go
// through that many.

        for (int i=0; i < imageSize; i++) {

// RLE encoding is defined by two bytes
            int byte1 = in.read();
            int byte2 = in.read();
            i += 2;

// If byte 0 == 0, this is an escape code
            if (byte1 == 0) {

// If escaped, byte 2 == 0 means you are at end of line
                if (byte2 == 0) {
                    x = 0;
                    y--;

// If escaped, byte 2 == 1 means end of bitmap
                } else if (byte2 == 1) {
                    return;

// if escaped, byte 2 == 2 adjusts the current x and y by
// an offset stored in the next two words
                } else if (byte2 == 2) {
                    int xoff = (char) intelShort(
                        in.readUnsignedShort());
                    i+= 2;
                    int yoff = (char) intelShort(
                        in.readUnsignedShort());
                    i+= 2;
                    x += xoff;
                    y -= yoff;

// If escaped, any other value for byte 2 is the number of bytes
// that you should read as pixel values (these pixels are not
// run-length encoded)
                } else {
                    int whichBit = 0;

// Read in the next byte
                    int currByte = in.read();

                    i++;
                    for (int j=0; j < byte2; j++) {

                        if (pixelSize == 4) {
// The pixels are 4-bits, so half the time you shift the current byte
// to the right as the pixel value
                            if (whichBit == 0) {
                                pixels[y*width+x] = colorTable[(currByte >> 4)
                                    & 0xf];
                            } else {

// The rest of the time, you mask out the upper 4 bits, save the pixel
// value, then read in the next byte

                                pixels[y*width+x] = colorTable[currByte & 0xf];
                                currByte = in.read();
                                i++;
                            }
                        } else {
                            pixels[y*width+x] = colorTable[currByte];
                            currByte = in.read();
                            i++;
                        }
                        x++;
                        if (x >= width) {
                            x = 0;
                            y--;
                        }
                    }
// The pixels must be word-aligned, so if you read an uneven number of
// bytes, read and ignore a byte to get aligned again.
                    if ((byte2 & 1) == 1) {
                        in.read();
                        i++;
                    }
                }


// If the first byte was not 0, it is the number of pixels that
// are encoded by byte 2
            } else {
                for (int j=0; j < byte1; j++) {

                   if (pixelSize == 4) {
// If j is odd, use the upper 4 bits
                       if ((j & 1) == 0) {
                           pixels[y*width+x] = colorTable[(byte2 >> 4) & 0xf];
                       } else {
                           pixels[y*width+x+1] = colorTable[byte2 & 0xf];
                       }
                   } else {
                       pixels[y*width+x+1] = colorTable[byte2];
                   }
                   x++;
                   if (x >= width) {
                       x = 0;
                       y--;
                   }
                }
            }
        }
    }
// intelShort converts a 16-bit number stored in intel byte order into
// the local host format

    protected static int intelShort(int i)
    {
        return ((i >> 8) & 0xff) + ((i << 8) & 0xff00);
    }

// intelInt converts a 32-bit number stored in intel byte order into
// the local host format

    protected static int intelInt(int i)
    {
        return ((i & 0xff) << 24) + ((i & 0xff00) << 8) +
            ((i & 0xff0000) >> 8) + ((i >> 24) & 0xff);
    }
}