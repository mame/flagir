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
	0x000000, 0xffffff,
	0x565656, 0xaaaaaa,
	0xff0000, 0x800000,
	0xff8080, 0xff8000,
	0xffff80, 0xffff00,
	0x80ff00, 0x808000,
	0x00ff00, 0x008000,
	0x80ff80, 0x00ff80,
	0x80ffff, 0x00ffff,
	0x0080ff, 0x008080,
	0x0000ff, 0x000080,
	0x8080ff, 0x8000ff,
	0xff80ff, 0xff00ff,
	0xff0080, 0x800080
    )

    // shapes of brush
    val brushes = Array(0x8000000L, 0x81c080000L, 0x1c3e7f7f7f3e1cL)
}
