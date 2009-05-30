/*
 * Flagir - image retrieval for national flag
 *
 *   * FlagirConfig.scala: configuration file
 */

object Config {
    // canvas size
    val (width, height) = (36, 24)

    // image retrieval parameters
    val div = 4   // number of image divison; image is divided into (div * div)
    val quant = 3 // length of Hist1D

    // scale factor of screen
    val dot = 8

    // number of shown result
    val top = 10

    // palette colors (0xRRGGBB)
    val palette = Array( 
	0x000000, 0xffffff, 0x464646, 0xdcdcdc, 0x787878, 0xbfbfbf, 0x99003a,
	0x9c5a3c, 0xed1c24, 0xffa3b1, 0xff7e00, 0xe5aa7a, 0xffc20e, 0xf5e49c,
	0xfff200, 0xfff9bd, 0xa8e61d, 0xd3f9bc, 0x22b14c, 0x9dbb61, 0x00b7ef,
	0x99d9ea, 0x4d6df3, 0x709ad1, 0x2f3699, 0x546d8e, 0x6f3198, 0xb5a5d5
    )

    // shapes of brush
    val brushes = Array(0x8000000L, 0x81c080000L, 0x1c3e7f7f7f3e1cL)
}
