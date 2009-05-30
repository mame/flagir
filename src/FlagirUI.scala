/*
 * Flagir - image retrieval for national flag
 *
 *   * FlagirUI.scala: interface for drawing
 */

import java.awt.{ Graphics, Image, Color, Font }

class UI(canvas: Canvas) {
    import Config.{ width, height, dot, palette, brushes }

    // UI states
    private var curr_colors: (Int, Int) = _   // selected colors
    private var curr_left_button: Boolean = _ // whether left button is pressed
    private var curr_brush: Int = _	      // selected brush

    // graphic-related objects
    private var image: Image = _	// image for double-buffering
    private var g: Graphics = _		// graphics of image
    private var repaint: () => Unit = _ // applet repaint
    private val colors =		// palette colors
	palette map { c => new Color(c >> 16, (c >> 8) & 0xff, c & 0xff) }

    // initialize
    def init(repaint0: () => Unit, createImage: (Int, Int) => Image) {
	curr_colors = (0, 1)
	curr_left_button = true
	curr_brush = 2
	repaint = repaint0

	// initialize applet
	image = createImage((width + 11) * dot, (height + 7) * dot)
	g = image.getGraphics()
	g.setFont(new Font("Serif", Font.PLAIN, 12))

	// clear canvas
	canvas.clear(curr_colors._2)
    }

    // paint screen
    def paint() = {
	var shift_x = 0
	var shift_y = 0
	def rect(x0: Int, y0: Int, c: Color, w0: Int, h0: Int) {
	    val x = (x0 + 1) * dot + shift_x
	    val y = (y0 + 1) * dot + shift_y
	    val w = w0 * dot
	    val h = h0 * dot
	    g.setColor(Color.lightGray)
	    g.drawRect(x, y, w, h)
	    g.setColor(c)
	    g.fillRect(x + 1, y + 1, w - 1, h - 1)
	}
	def paint_canvas {
	    shift_x = 0; shift_y = 0

	    // canvas
	    for (y <- 0 until height)
		for (x <- 0 until width)
		    rect(x, y, colors(canvas.canvas(y)(x)), 1, 1)

	    // border
	    g.setColor(Color.black)
	    g.drawRect(dot, dot, width * dot, height * dot)
	}
	def paint_palette {
	    shift_x = 0; shift_y = (height + 1) * dot

	    // palette colors
	    for (i <- 0 until ((colors.length + 1) / 2)) {
		rect(4 + i * 2, 0, colors(i * 2    ), 2, 2)
		rect(4 + i * 2, 2, colors(i * 2 + 1), 2, 2)
	    }

	    // current colors
	    rect(0, 0, Color.white, 4, 4)
	    shift_x += dot / 2; shift_y += dot / 2
	    rect(1, 1, colors(curr_colors._2), 2, 2)
	    rect(0, 0, colors(curr_colors._1), 2, 2)
	}
	def paint_brush {
	    shift_x = (width + 1) * dot; shift_y = 0

	    // mark of current brush
	    val mx = dot + shift_x
	    val my = (curr_brush * 8 + 4) * dot + shift_y
	    val xs = Array(mx, mx      , mx + dot - 2)
	    val ys = Array(my, my + dot, my + dot / 2)
	    g.setColor(Color.black)
	    g.fillPolygon(xs, ys, 3)

	    // brushes
	    for (i <- 0 until 3) {
		val c = if (curr_brush == i) colors(0) else Color.lightGray
		rect(1, i * 8, Color.white, 7, 7)
		for (y <- 0 until 7) {
		    for (x <- 0 until 7) {
			if (((brushes(i) >> (y * 8 + x)) & 1) == 1)
			    rect(x + 1, i * 8 + y, c, 1, 1)
		    }
		}
	    }
	}
	def paint_button {
	    shift_x = (width + 1) * dot; shift_y = (height + 1) * dot

	    // border
	    g.setColor(Color.lightGray)
	    g.drawRect(2 * dot + shift_x, dot + shift_y, 7 * dot, 4 * dot)

	    // "CLEAR" text
	    val text = "CLEAR"
	    g.setColor(Color.black)
	    val f = g.getFontMetrics()
	    val x = shift_x + 5 * dot + dot / 2 - f.stringWidth(text) / 2
	    val y = shift_y + 3 * dot - f.getHeight() / 2 + f.getAscent()
	    g.drawString(text, x, y)
	}

	// clear background
	g.setColor(Color.white)
	g.fillRect(0, 0, (image getWidth null) - 1, (image getHeight null) - 1)

	// paint objects
	paint_canvas
	paint_palette
	paint_brush
	paint_button

	image
    }

    // handling mouse dragged event
    def mouse_dragged(x0: Int, y0: Int) {
	val x = x0 / dot - 1
	val y = y0 / dot - 1
	// canvas
	if (in(x, 0, width) && in(y, 0, height)) {
	    val (l, r) = curr_colors
	    val color = if (curr_left_button) l else r
	    canvas.paint(x, y, curr_brush, color)
	    repaint()
	}
    }

    // handling mouse pressed event
    def mouse_pressed(x0: Int, y0: Int, left: Boolean) {
	val x = x0 / dot - 1
	val y = y0 / dot - 1
	curr_left_button = left

	// palette
	if (in(x, 4, colors.length) && in(y, height + 1, 4)) {
	    val n = (x - 4) / 2 * 2 + (y - (height + 1)) / 2
	    val (l, r) = curr_colors
	    curr_colors = if (curr_left_button) (n, r) else (l, n)
	    repaint()
	}

	// brush
	if (in(x, width + 2, 7) && in(y, 0, 24) && y % 8 < 7) {
	    curr_brush = y / 8
	    repaint()
	}

	// clear button
	if (in(x, width + 2, 7) && in(y, height + 1, 4)) {
	    canvas.clear(curr_colors._2)
	    repaint()
	}
    }

    // helper method
    private def in(x:Int, x0: Int, w: Int) = { x0 <= x && x < x0 + w }
}
