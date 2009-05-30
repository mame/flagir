/*
 * Flagir - image retrieval for national flag
 *
 *   * FlagirCanvas.scala: canvas manager
 */

class Canvas {
    import Config.{ height, width, div, quant, brushes, palette }

    // whether canvas is modified
    private var modified = true			 

    // current colors of each cell
    val canvas: Array[Array[Int]] =
	(0 until height) map {
	    (_: Int) => (0 until width) map { (_: Int) => -1 } toArray
	} toArray

    // current histogram
    private val histogram: Array[Int] =
	new Array((div - 1) * (div - 1) * 3 * quant)

    // quantization table
    private val quantize =
	palette map { c =>
	    Array(c >> 16, c >> 8, c) map { c => (c & 0xff) * quant / 256 }
	}

    // initialize
    def init {
	synchronized {
	    for (j <- 0 until height)
		for (i <- 0 until width)
		    canvas(j)(i) = -1
	    for (i <- 0 until (div - 1) * (div - 1) * 3 * quant)
		histogram(i) = 0
	}
    }

    // paint canvas all new_color
    def clear(new_color: Int) {
	modify {
	    for (y <- 0 until height)
		for (x <- 0 until width)
		    paint_cell(x, y, new_color)
	}
    }

    // paint a specified region with brush
    def paint(x0: Int, y0: Int, brush: Int, new_color: Int) {
	val b = brushes(brush)
	modify {
	    for (y <- (y0 - 3) to (y0 + 3) if 0 <= y && y < height)
		for (x <- (x0 - 3) to (x0 + 3) if 0 <= x && x < width) 
		    if (((b >> ((x - x0 + 3) * 8 + (y - y0 + 3))) & 1) == 1)
			paint_cell(x, y, new_color)
	}
    }

    // wait until canvas is modified and copy current histogram to dest_hist
    def update_new_histogram(dest_hist: Array[Double]) {
	synchronized {
	    while (!modified) {
		try { wait() } catch { case e:InterruptedException => () }
	    }
	    modified = false
	    for (i <- 0 until histogram.length)
		dest_hist(i) = histogram(i).asInstanceOf[Double]
	}
    }

    // execute proc with modification check
    private def modify(proc: => Unit) {
	synchronized {
	    proc
	    if (modified) notify()
	}
    }

    // paint a speficied cell
    private def paint_cell(x0: Int, y0: Int, new_color: Int) {
	def update_histogram(offset: Int, color: Int, n: Int, inc: Int) {
	    if (color == -1) return
	    val bin = quantize(color)(n)
	    if (bin > 0        ) histogram(offset + bin - 1) += inc
				 histogram(offset + bin    ) += inc * 3
	    if (bin < quant - 1) histogram(offset + bin + 1) += inc
	}
	val old_color = canvas(y0)(x0)
	if (old_color == new_color) return
	val x = x0 * div / width
	val y = y0 * div / height
	for (j <- (y - 1) to y if 0 <= j && j < div - 1) {
	    for (i <- (x - 1) to x if 0 <= i && i < div - 1) {
		for (n <- 0 until 3) {
		    val offset = ((j * (div - 1) + i) * 3 + n) * quant
		    update_histogram(offset, old_color, n, -1)
		    update_histogram(offset, new_color, n,  1)
		}
	    }
	}
	canvas(y0)(x0) = new_color
	modified = true
    }
}
