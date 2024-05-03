package tm.tilecodecs;

public class _6BPPLinearTileCodec extends TileCodec {

/**
* Constructor.
**/

    public _6BPPLinearTileCodec() {
        super("LN99", 6, "6bpp linear, reverse-order");
    }

/**
*
* Decodes a tile.
*
**/

    public int[] decode(byte[] bits, int ofs, int stride) {
        int pos=0;
        int b1, b2, b3, b4, b5, b6;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
            // do one row
			/*	BPP3 Linear:
			 * 	byte 1: 0001 1122
				byte 2: 2333 4445
				byte 3: 5566 6777
			 */
			/*	BPP6 Linear:
			 * 	byte 1: 0000 0011
				byte 2: 1111 2222
				byte 3: 2233 3333
				byte 4: 4444 4455
				byte 5: 5555 6666
				byte 6: 6677 7777
			 */
            b6 = bits[ofs++] & 0xFF; // byte 1: 0000 0011
            b5 = bits[ofs++] & 0xFF; // byte 2: 1111 2222
            b4 = bits[ofs++] & 0xFF; // byte 3: 2233 3333
            b3 = bits[ofs++] & 0xFF; // byte 4: 4444 4455
            b2 = bits[ofs++] & 0xFF; // byte 5: 5555 6666
            b1 = bits[ofs++] & 0xFF; // byte 6: 6677 7777
            pixels[pos++] = (b1 >> 2) & 63;
            pixels[pos++] = ((b1 & 3) << 4) | ((b2 >> 4) & 15);
            pixels[pos++] = ((b2 & 15) << 2) | ((b3 >> 6) & 3);
            pixels[pos++] = b3 & 63;
            pixels[pos++] = (b4 >> 2) & 63;
            pixels[pos++] = ((b4 & 3) << 4) | ((b5 >> 4) & 15);
            pixels[pos++] = ((b5 & 15) << 2) | ((b6 >> 6) & 3);
            pixels[pos++] = b6 & 63;
            ofs += stride;
        }
        return pixels;
    }

/**
*
* Encodes a tile.
*
**/

    public void encode(int[] pixels, byte[] bits, int ofs, int stride) {
        int pos = 0;
        int b1, b2, b3, b4, b5, b6;
        stride *= bytesPerRow;
        for (int i=0; i<8; i++) {
			/*	BPP3 Linear:
			 * 	byte 1: 0001 1122
				byte 2: 2333 4445
				byte 3: 5566 6777
			 */
			/*	BPP6 Linear:
			 * 	byte 1: 0000 0011
				byte 2: 1111 2222
				byte 3: 2233 3333
				byte 4: 4444 4455
				byte 5: 5555 6666
				byte 6: 6677 7777
			 */

            // do one row
            b1 = (pixels[pos++] & 63) << 2;
            b1 |= (pixels[pos] & 48) >> 4;
            
            b2 = (pixels[pos++] & 15) << 4;
            b2 |= (pixels[pos] & 60) >> 2;
            
            b3 = (pixels[pos++] & 3) << 6;
            b3 |= (pixels[pos++] & 63);
            
			
            b4 = (pixels[pos++] & 63) << 2;
            b4 |= (pixels[pos] & 48) >> 4;
            
            b5 = (pixels[pos++] & 15) << 4;
            b5 |= (pixels[pos] & 60) >> 2;
            
            b6 = (pixels[pos++] & 3) << 6;
            b6 |= (pixels[pos++] & 63);
            
			
            bits[ofs++] = (byte)b6; // byte 1: 0000 0011
            bits[ofs++] = (byte)b5; // byte 2: 1111 2222
            bits[ofs++] = (byte)b4; // byte 3: 2233 3333
            bits[ofs++] = (byte)b3; // byte 4: 4444 4455
            bits[ofs++] = (byte)b2; // byte 5: 5555 6666
            bits[ofs++] = (byte)b1; // byte 6: 6677 7777
            ofs += stride;
        }
    }

}