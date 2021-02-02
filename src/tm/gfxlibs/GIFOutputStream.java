/*
   Class: org.shetline.io.GIFOutputStream

   Copyright (c) 2000, 2001 by Kerry Shetline, kerry@shetline.com.

   This code is free for public use in any non-commercial application. All
   other uses are restricted without prior consent of the author, Kerry
   Shetline. The author assumes no liability for the suitability of this
   code in any application.

   Note: This code does *NOT* implement LZW compression. While the output
   of the compression routine is compatible with LZW, only a simple run-
   length compression is performed. The degree of compression as compared
   to LZW is not as high, but execution time is faster, and LZW licensing
   issues are avoided. Depending on image size and image complexity, the
   differences in compression may or may not have practical significance
   for particular applications.

   Date           Comments
   -----------    --------
   2000 SEP 30    First released version.
   2001 MAR 18    Replaced byte-by-byte specification of 256-color table with
                  a short code segment to generate the same table.
   2001 JUN 10    Added DITHERED_216_COLORS option.
   2001 AUG 21    Fixed a bug where single-color images would produce an
                  invalid GIF stream when using ORIGINAL_COLOR mode. GIF color
                  tables need to have at least two entries, so if the image only
                  has one color, an unused entry of either black or white is
                  added to the table to make it a valid length.
*/

package tm.gfxlibs;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;
import java.util.Enumeration;

public class GIFOutputStream extends FilterOutputStream
{
   public static final int ORIGINAL_COLOR = 0;
   public static final int BLACK_AND_WHITE = 1;
   public static final int GRAYSCALE_16 = 2;
   public static final int GRAYSCALE_256 = 3;
   public static final int STANDARD_16_COLORS = 4;
   public static final int STANDARD_256_COLORS = 5;
   public static final int DITHERED_216_COLORS = 6;

   public static final int NO_ERROR = 0;
   public static final int IMAGE_LOAD_FAILED = 1;
   public static final int TOO_MANY_COLORS = 2;
   public static final int INVALID_COLOR_MODE = 3;

   protected static final int BLACK_INDEX = 0;
   protected static final int WHITE_INDEX = 1;

   protected static final int[] standard16 =
   {
      0x000000,
      0xFF0000, 0x00FF00, 0x0000FF,
      0x00FFFF, 0xFF00FF, 0xFFFF00,
      0x800000, 0x008000, 0x000080,
      0x008080, 0x800080, 0x808000,
      0x808080, 0xC0C0C0,
      0xFFFFFF
   };

   protected static final int[] standard256 = new int[256];

   protected static int ditherPattern[][] = {{  8, 184, 248, 216},
                                             {120,  56, 152,  88},
                                             { 40, 232,  24, 200},
                                             {168, 104, 136,  72}};

   protected int     errorStatus = NO_ERROR;

   static
   {
      // Set up a standard 256-color table.

      int   n, j, r, g, b;

      standard256[0] = 0x000000;

      n = 40;

      // 0x33 multiples, starting at index 41 (black stored at index 40 gets replaced with 0xEE0000
      for (r = 0; r < 6; ++r)
         for (g = 0; g < 6; ++g)
            for (b = 0; b < 6; ++b)
               standard256[n++] = 0x330000 * r | 0x003300 * g | 0x000033 * b;

      n = 1;

      for (j = 0; j < 10; ++j) {
         // Shades of gray w/o 0x33 multiples, starting at index 1
         standard256[j +  1] = 0x111111 * n;
         // Shades of blue w/o 0x33 multiples, starting at index 11
         standard256[j + 11] = 0x000011 * n;
         // Shades of green w/o 0x33 multiples, starting at index 21
         standard256[j + 21] = 0x001100 * n;
         // Shades of red w/o 0x33 multiples, starting at index 31
         standard256[j + 31] = 0x110000 * n;

         ++n;

         if (n % 3 == 0)
            ++n;
      }
   }

   public static int writeGIF(OutputStream out, Image image) throws IOException
   {
      return writeGIF(out, image, ORIGINAL_COLOR, null);
   }

   public static int writeGIF(OutputStream out, Image image, int colorMode) throws IOException
   {
      return writeGIF(out, image, colorMode, null);
   }

   public static int writeGIF(OutputStream out, Image image, int colorMode, Color transparentColor) throws IOException
   {
      GIFOutputStream   gifOut = new GIFOutputStream(out);

      gifOut.write(image, colorMode, transparentColor);

       int res = gifOut.getErrorStatus();

       gifOut.close();

       return res;
   }

   public GIFOutputStream(OutputStream out)
   {
      super(out);
   }

   public int getErrorStatus() { return errorStatus; }

   public void write(Image image) throws IOException
   {
      write(image, ORIGINAL_COLOR, null);
   }

   public void write(Image image, int colorMode) throws IOException
   {
      write(image, colorMode, null);
   }

   public void write(Image image, Color transparentColor) throws IOException
   {
      write(image, ORIGINAL_COLOR, transparentColor);
   }

   public void write(Image image, int colorMode, Color transparentColor) throws IOException
   {
      errorStatus = NO_ERROR;

      if (image == null)
         return;

      PixelGrabber   pg = new PixelGrabber(image, 0, 0, -1, -1, true);

      try {
         pg.grabPixels();
      } catch (InterruptedException e) {
         errorStatus = IMAGE_LOAD_FAILED;
         return;
      }

      if ((pg.status() & ImageObserver.ABORT) != 0) {
         errorStatus = IMAGE_LOAD_FAILED;
         return;
      }

      int[]    pixels = (int[]) pg.getPixels();
      int      width = pg.getWidth();
      int      height = pg.getHeight();
      int      colorCount = 0;
      int[]    colorTable = null;
      byte[]   bytePixels = null;

////// do color reduction
/*      int[][] pixxies = new int[height][width];
      for (int i=0; i<height; i++) {
          for (int j=0; j<width; j++) {
              pixxies[i][j] = pixels[(i * width) + j];
          }
      }
*/
//////

      switch (colorMode) {
         case ORIGINAL_COLOR:
            Hashtable   colorSet = getColorSet(pixels);
            colorCount = colorSet.size();
            if (colorCount > 256) {
               errorStatus = TOO_MANY_COLORS;
               return;
            }
            colorTable = createColorTable(colorSet, colorCount);
            bytePixels = createBytePixels(pixels, colorSet);
            break;

         case BLACK_AND_WHITE:
            colorCount = 2;
            colorTable = createBWTable();
            bytePixels = createBWBytePixels(pixels);
            break;

         case GRAYSCALE_16:
            colorCount = 16;
            colorTable = create16GrayTable();
            bytePixels = create16GrayBytePixels(pixels);
            break;

         case GRAYSCALE_256:
            colorCount = 256;
            colorTable = create256GrayTable();
            bytePixels = create256GrayBytePixels(pixels);
            break;

         case STANDARD_16_COLORS:
            colorCount = 16;
            colorTable = createStd16ColorTable();
            bytePixels = createStd16ColorBytePixels(pixels);
            break;

         case STANDARD_256_COLORS:
            colorCount = 256;
            colorTable = createStd256ColorTable();
            bytePixels = createStd256ColorBytePixels(pixels, width, false);
            break;

         case DITHERED_216_COLORS:
            colorCount = 216;
            colorTable = createStd216ColorTable();
            bytePixels = createStd256ColorBytePixels(pixels, width, true);
            break;

         default:
            errorStatus = INVALID_COLOR_MODE;
            return;
      }

      pixels = null;

      int   cc1 = colorCount - 1;
      int   bitsPerPixel = 0;

      while (cc1 != 0) {
         ++bitsPerPixel;
         cc1 >>= 1;
      }

      writeGIFHeader(width, height, bitsPerPixel);

      writeColorTable(colorTable, bitsPerPixel);

      if (transparentColor != null)
         writeGraphicControlExtension(transparentColor, colorTable);

      writeImageDescriptor(width, height);

      writeCompressedImageData(bytePixels, bitsPerPixel);

      write(0x00); // Terminate picture data.

      write(0x3B); // GIF file terminator.
   }

   protected Hashtable getColorSet(int[] pixels)
   {
      Hashtable   colorSet = new Hashtable();
      boolean[]   checked = new boolean[pixels.length];
      int         needsChecking = pixels.length;
      int         color;
      int         colorIndex = 0;
      Integer     key;

      for (int j = 0; j < pixels.length && needsChecking > 0; ++j) {
         if (!checked[j]) {
            color = pixels[j] & 0x00FFFFFF;
            checked[j] = true;
            --needsChecking;

            key = new Integer(color);
            colorSet.put(key, new Integer(colorIndex));
            if (++colorIndex > 256)
               break;

            for (int j2 = j + 1; j2 < pixels.length; ++j2) {
               if ((pixels[j2] & 0x00FFFFFF) == color) {
                  checked[j2] = true;
                  --needsChecking;
               }
            }
         }
      }

      if (colorIndex == 1) {
         if (colorSet.get(new Integer(0)) == null)
            colorSet.put(new Integer(0), new Integer(1));
         else
            colorSet.put(new Integer(0xFFFFFF), new Integer(1));
      }

      return colorSet;
   }

   protected int[] createColorTable(Hashtable colorSet, int colorCount)
   {
      int[]    colorTable = new int[colorCount];
      Integer  key;

      for (Enumeration e = colorSet.keys(); e.hasMoreElements(); ) {
         key = (Integer) e.nextElement();
         colorTable[((Integer) colorSet.get(key)).intValue()] = key.intValue();
      }

      return colorTable;
   }

   protected byte[] createBytePixels(int[] pixels, Hashtable colorSet)
   {
      byte[]   bytePixels = new byte[pixels.length];
      Integer  key;
      int      colorIndex;

      for (int j = 0; j < pixels.length; ++j) {
         key = new Integer(pixels[j] & 0x00FFFFFF);
         colorIndex = ((Integer) colorSet.get(key)).intValue();
         bytePixels[j] = (byte) colorIndex;
      }

      return bytePixels;
   }

   protected int[] createBWTable()
   {
      int[]    colorTable = new int[2];

      colorTable[BLACK_INDEX] = 0x000000;
      colorTable[WHITE_INDEX] = 0xFFFFFF;

      return colorTable;
   }

   protected byte[] createBWBytePixels(int[] pixels)
   {
      byte[]   bytePixels = new byte[pixels.length];

      for (int j = 0; j < pixels.length; ++j) {
         if (grayscaleValue(pixels[j]) < 0x80)
            bytePixels[j] = (byte) BLACK_INDEX;
         else
            bytePixels[j] = (byte) WHITE_INDEX;
      }

      return bytePixels;
   }

   protected int[] create16GrayTable()
   {
      int[]    colorTable = new int[16];

      for (int j = 0; j < 16; ++j)
         colorTable[j] = 0x111111 * j;

      return colorTable;
   }

   protected byte[] create16GrayBytePixels(int[] pixels)
   {
      byte[]   bytePixels = new byte[pixels.length];

      for (int j = 0; j < pixels.length; ++j) {
         bytePixels[j] = (byte) (grayscaleValue(pixels[j]) / 16);
      }

      return bytePixels;
   }

   protected int[] create256GrayTable()
   {
      int[]    colorTable = new int[256];

      for (int j = 0; j < 256; ++j)
         colorTable[j] = 0x010101 * j;

      return colorTable;
   }

   protected byte[] create256GrayBytePixels(int[] pixels)
   {
      byte[]   bytePixels = new byte[pixels.length];

      for (int j = 0; j < pixels.length; ++j) {
         bytePixels[j] = (byte) grayscaleValue(pixels[j]);
      }

      return bytePixels;
   }

   protected int[] createStd16ColorTable()
   {
      int[]    colorTable = new int[16];

      System.arraycopy(standard16, 0, colorTable, 0, 16);

      return colorTable;
   }

   protected byte[] createStd16ColorBytePixels(int[] pixels)
   {
      byte[]   bytePixels = new byte[pixels.length];
      int      color;
      int      minError = 0;
      int      error;
      int      minIndex;

      for (int j = 0; j < pixels.length; ++j) {
         color = pixels[j] & 0xFFFFFF;
         minIndex = -1;

         for (int k = 0; k < 16; ++k) {
            error = colorMatchError(color, standard16[k]);
            if (error < minError || minIndex < 0) {
               minError = error;
               minIndex = k;
            }
         }

         bytePixels[j] = (byte) minIndex;
      }

      return bytePixels;
   }

   protected int[] createStd256ColorTable()
   {
      int[]    colorTable = new int[256];

      System.arraycopy(standard256, 0, colorTable, 0, 256);

      return colorTable;
   }

   protected int[] createStd216ColorTable()
   {
      int[]    colorTable = new int[216];

      colorTable[0] = 0x000000;

      System.arraycopy(standard256, 41, colorTable, 1, 215);

      return colorTable;
   }

   protected byte[] createStd256ColorBytePixels(int[] pixels, int width, boolean dither)
   {
      byte[]   bytePixels = new byte[pixels.length];
      int      color;
      int      minError = 0;
      int      error;
      int      minIndex;
      int      sampleIndex;
      int      r, g, b;
      int      r2, g2, b2;
      int      x, y;
      int      threshold;

      for (int j = 0; j < pixels.length; ++j) {
         color = pixels[j] & 0xFFFFFF;
         minIndex = -1;

         r = (color & 0xFF0000) >> 16;
         g = (color & 0x00FF00) >> 8;
         b =  color & 0x0000FF;

         r2 = r / 0x33;
         g2 = g / 0x33;
         b2 = b / 0x33;

         if (dither) {
            x = j % width;
            y = j / width;
            threshold = ditherPattern[x % 4][y % 4] / 5;

            if (r2 < 5 && r % 0x33 >= threshold)
               ++r2;

            if (g2 < 5 && g % 0x33 >= threshold)
               ++g2;

            if (b2 < 5 && b % 0x33 >= threshold)
               ++b2;

            bytePixels[j] = (byte) (r2 * 36 + g2 * 6 + b2);
         }
         else {
            // Try to match color to a 0x33-multiple color.

            for (int r0 = r2; r0 <= r2 + 1 && r0 < 6; ++r0) {
               for (int g0 = g2; g0 <= g2 + 1 && g0 < 6; ++g0) {
                  for (int b0 = b2; b0 <= b2 + 1 && b0 < 6; ++b0) {
                     sampleIndex = 40 + r0 * 36 + g0 * 6 + b0;
                     if (sampleIndex == 40)
                        sampleIndex = 0;

                     error = colorMatchError(color, standard256[sampleIndex]);
                     if (error < minError || minIndex < 0) {
                        minError = error;
                        minIndex = sampleIndex;
                     }
                  }
               }
            }

            int   shadeBase;
            int   shadeIndex;

            // Try to match color to a 0x11-multiple pure primary shade.

            if (r > g && r > b) {
               shadeBase = 30;
               shadeIndex = (r + 8) / 0x11;
            }
            else if (g > r && g > b) {
               shadeBase = 20;
               shadeIndex = (g + 8) / 0x11;
            }
            else {
               shadeBase = 10;
               shadeIndex = (b + 8) / 0x11;
            }

            if (shadeIndex > 0) {
               shadeIndex -= (shadeIndex / 3);
               sampleIndex = shadeBase + shadeIndex;
               error = colorMatchError(color, standard256[sampleIndex]);
               if (error < minError || minIndex < 0) {
                  minError = error;
                  minIndex = sampleIndex;
               }
            }

            // Try to match color to a 0x11-multiple gray.

            shadeIndex = (grayscaleValue(color) + 8) / 0x11;
            if (shadeIndex > 0) {
               shadeIndex -= (shadeIndex / 3);
               sampleIndex = shadeIndex;
               error = colorMatchError(color, standard256[sampleIndex]);
               if (error < minError || minIndex < 0) {
                  minError = error;
                  minIndex = sampleIndex;
               }
            }

            bytePixels[j] = (byte) minIndex;
         }
      }

      return bytePixels;
   }

   protected int grayscaleValue(int color)
   {
      int   r = (color & 0xFF0000) >> 16;
      int   g = (color & 0x00FF00) >> 8;
      int   b =  color & 0x0000FF;

      return (r * 30 + g * 59 + b * 11) / 100;
   }

   protected int colorMatchError(int color1, int color2)
   {
      int   r1 = (color1 & 0xFF0000) >> 16;
      int   g1 = (color1 & 0x00FF00) >> 8;
      int   b1 =  color1 & 0x0000FF;
      int   r2 = (color2 & 0xFF0000) >> 16;
      int   g2 = (color2 & 0x00FF00) >> 8;
      int   b2 =  color2 & 0x0000FF;
      int   dr = (r2 - r1) * 30;
      int   dg = (g2 - g1) * 59;
      int   db = (b2 - b1) * 11;

      return (dr * dr + dg * dg + db * db) / 100;
   }

   protected void writeGIFHeader(int width, int height, int bitsPerPixel) throws IOException
   {
      write((int) 'G');
      write((int) 'I');
      write((int) 'F');
      write((int) '8');
      write((int) '9');
      write((int) 'a');

      writeGIFWord(width);
      writeGIFWord(height);

      int   packedBits = 0x80; // Yes, there is a global color table, not ordered.

      packedBits |= ((bitsPerPixel - 1) << 4) | (bitsPerPixel - 1);

      write(packedBits);

      write(0); // Background color index -- not used.

      write(0); // Aspect ratio index -- not specified.
   }

   protected void writeColorTable(int[] colorTable, int bitsPerPixel) throws IOException
   {
      int   colorCount = 1 << bitsPerPixel;

      for (int j = 0; j < colorCount; ++j) {
         if (j < colorTable.length)
            writeGIFColor(colorTable[j]);
         else
            writeGIFColor(0);
      }
   }

   protected void writeGraphicControlExtension(Color transparentColor,
      int[] colorTable) throws IOException
   {
      for (int j = 0; j < colorTable.length; ++j) {
         if (colorTable[j] == (transparentColor.getRGB() & 0xFFFFFF)) {
            write(0x21); // Extension identifier.
            write(0xF9); // Graphic Control Extension identifier.
            write(0x04); // Block size, always 4.
            write(0x01); // Sets transparent color bit. Other bits in this
                         //   packed field should be zero.
            write(0x00); // Two bytes of delay time -- not used.
            write(0x00);
            write(j);    // Index of transparent color.
            write(0x00); // Block terminator.
         }
      }
   }

   protected void writeImageDescriptor(int width, int height) throws IOException
   {
      write(0x2C); // Image descriptor identifier;

      writeGIFWord(0); // left postion;
      writeGIFWord(0); // top postion;
      writeGIFWord(width);
      writeGIFWord(height);

      write(0); // No local color table, not interlaced.
   }

   protected void writeGIFWord(short word) throws IOException
   {
      writeGIFWord((int) word);
   }

   protected void writeGIFWord(int word) throws IOException
   {
      write(word & 0xFF);
      write((word & 0xFF00) >> 8);
   }

   protected void writeGIFColor(Color color) throws IOException
   {
      writeGIFColor(color.getRGB());
   }

   protected void writeGIFColor(int color) throws IOException
   {
      write((color & 0xFF0000) >> 16);
      write((color & 0xFF00) >> 8);
      write(color & 0xFF);
   }


   /********************************************************************\
   |                                                                    |
   |  The following code is based on C code for GIF compression         |
   |  obtained from http://www.boutell.com                              |
   |                                                                    |
   |  Based on GIFENCOD by David Rowley <mgardi@watdscu.waterloo.edu>   |
   |  Modified by Marcel Wijkstra <wijkstra@fwi.uva.nl>                 |
   |  One version, Copyright (C) 1989 by Jef Poskanzer.                 |
   |  Heavily modified by Mouse, 1998-02-12.                            |
   |                                                                    |
   |  And now, modified and rendered in Java by Kerry Shetline, 2000,   |
   |  kerry@shetline.com                                                |
   |                                                                    |
   \********************************************************************/

   protected int        rl_pixel;
   protected int        rl_basecode;
   protected int        rl_count;
   protected int        rl_table_pixel;
   protected int        rl_table_max;
   protected boolean    just_cleared;
   protected int        out_bits;
   protected int        out_bits_init;
   protected int        out_count;
   protected int        out_bump;
   protected int        out_bump_init;
   protected int        out_clear;
   protected int        out_clear_init;
   protected int        max_ocodes;
   protected int        code_clear;
   protected int        code_eof;
   protected int        obuf;
   protected int        obits;
   protected byte[]     oblock = new byte[256];
   protected int        oblen;

   protected final static int GIFBITS = 12;

   protected void writeCompressedImageData(byte[] bytePixels, int bitsPerPixel)
      throws IOException
   {
      int   init_bits = bitsPerPixel;

      if (init_bits < 2)
         init_bits = 2;

      write(init_bits);

      int      c;

      obuf = 0;
      obits = 0;
      oblen = 0;
      code_clear = 1 << init_bits;
      code_eof = code_clear + 1;
      rl_basecode = code_eof + 1;
      out_bump_init = (1 << init_bits) - 1;
      /* for images with a lot of runs, making out_clear_init larger will
         give better compression. */
      out_clear_init = (init_bits <= 2) ? 9 : (out_bump_init - 1);
      out_bits_init = init_bits + 1;
      max_ocodes = (1 << GIFBITS) - ((1 << (out_bits_init - 1)) + 3);
      did_clear();
      output(code_clear);
      rl_count = 0;

      for (int j = 0; j < bytePixels.length; ++j) {
         c = (int) bytePixels[j];
         if (c < 0)
            c += 256;

         if ((rl_count > 0) && (c != rl_pixel))
            rl_flush();

         if (rl_pixel == c) {
            rl_count++;
         }
         else {
            rl_pixel = c;
            rl_count = 1;
         }
      }

      if (rl_count > 0)
         rl_flush();

      output(code_eof);
      output_flush();
   }


   protected void write_block() throws IOException
   {
      write(oblen);
      write(oblock, 0, oblen);
      oblen = 0;
   }

   protected void block_out(int c) throws IOException
   {
      oblock[oblen++] = (byte) c;
      if (oblen >= 255)
         write_block();
   }

   protected void block_flush() throws IOException
   {
      if (oblen > 0)
         write_block();
   }

   protected void output(int val) throws IOException
   {
      obuf |= val << obits;
      obits += out_bits;
      while (obits >= 8) {
         block_out(obuf & 0xFF);
         obuf >>= 8;
         obits -= 8;
      }
   }

   protected void output_flush() throws IOException
   {
      if (obits > 0)
         block_out(obuf);
      block_flush();
   }

   protected void did_clear() throws IOException
   {
      out_bits = out_bits_init;
      out_bump = out_bump_init;
      out_clear = out_clear_init;
      out_count = 0;
      rl_table_max = 0;
      just_cleared = true;
   }

   protected void output_plain(int c) throws IOException
   {
      just_cleared = false;
      output(c);
      out_count++;
      if (out_count >= out_bump) {
         out_bits++;
         out_bump += 1 << (out_bits - 1);
      }
      if (out_count >= out_clear) {
         output(code_clear);
         did_clear();
      }
   }

   protected int isqrt(int x)
   {
      int   r;
      int   v;

      if (x < 2)
         return x;

      for (v = x, r = 1; v != 0; v >>= 2, r <<= 1);

      while (true) {
         v = ((x / r) + r) / 2;
         if ((v == r) || (v == r + 1))
            return r;
         r = v;
      }
   }

   protected int compute_triangle_count(int count, int nrepcodes)
   {
      int   perrep;
      int   cost;

      cost = 0;
      perrep = (nrepcodes * (nrepcodes +1 )) / 2;
      while (count >= perrep) {
         cost += nrepcodes;
         count -= perrep;
      }
      if (count > 0) {
         int      n = isqrt(count);
         while ((n * (n + 1)) >= 2 * count)
            n--;
         while ((n * (n + 1)) < 2 * count)
            n++;
         cost += n;
      }

      return cost;
   }

   protected void max_out_clear()
   {
      out_clear = max_ocodes;
   }

   protected void reset_out_clear() throws IOException
   {
      out_clear = out_clear_init;
      if (out_count >= out_clear) {
         output(code_clear);
         did_clear();
      }
   }

   protected void rl_flush_fromclear(int count) throws IOException
   {
      int   n;

      max_out_clear();
      rl_table_pixel = rl_pixel;
      n = 1;
      while (count > 0) {
         if (n == 1) {
            rl_table_max = 1;
            output_plain(rl_pixel);
            count--;
         }
         else if (count >= n) {
            rl_table_max = n;
            output_plain(rl_basecode + n - 2);
            count -= n;
         }
         else if (count == 1) {
            rl_table_max++;
            output_plain(rl_pixel);
            count = 0;
         }
         else {
            rl_table_max++;
            output_plain(rl_basecode + count - 2);
            count = 0;
         }

         if (out_count == 0)
            n = 1;
         else
            n++;
      }

      reset_out_clear();
   }

   protected void rl_flush_clearorrep(int count) throws IOException
   {
      int   withclr;

      withclr = 1 + compute_triangle_count(count, max_ocodes);
      if (withclr < count) {
         output(code_clear);
         did_clear();
         rl_flush_fromclear(count);
      }
      else {
         for (; count > 0; count--)
            output_plain(rl_pixel);
      }
   }

   protected void rl_flush_withtable(int count) throws IOException
   {
      int   repmax;
      int   repleft;
      int   leftover;

      repmax = count / rl_table_max;
      leftover = count % rl_table_max;
      repleft = (leftover != 0 ? 1 : 0);
      if (out_count + repmax + repleft > max_ocodes) {
         repmax = max_ocodes - out_count;
         leftover = count - (repmax * rl_table_max);
         repleft = 1 + compute_triangle_count(leftover, max_ocodes);
      }

      if (1 + compute_triangle_count(count,max_ocodes) < repmax + repleft) {
         output(code_clear);
         did_clear();
         rl_flush_fromclear(count);
         return;
      }

      max_out_clear();
      for (; repmax > 0; repmax--)
         output_plain(rl_basecode + rl_table_max - 2);
      if (leftover != 0) {
         if (just_cleared) {
            rl_flush_fromclear(leftover);
         }
         else if (leftover == 1) {
            output_plain(rl_pixel);
         }
         else {
            output_plain(rl_basecode + leftover - 2);
         }
      }
      reset_out_clear();
   }

   protected void rl_flush() throws IOException
   {
      int   table_reps;
      int   table_extra;

      if (rl_count == 1) {
         output_plain(rl_pixel);
         rl_count = 0;
         return;
      }
      if (just_cleared) {
         rl_flush_fromclear(rl_count);
      }
      else if ((rl_table_max < 2) || (rl_table_pixel != rl_pixel)) {
         rl_flush_clearorrep(rl_count);
      }
      else {
         rl_flush_withtable(rl_count);
      }

      rl_count = 0;
   }

   /******** END OF IMPORTED GIF COMPRESSION CODE ********/
}
