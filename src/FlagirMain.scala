/*
 * Flagir - image retrieval for national flag
 *
 *   * FlagirMain.scala: applet instance
 */

import java.awt.Graphics
import java.awt.event.{ MouseListener, MouseMotionListener, MouseEvent }
import java.applet.Applet
import netscape.javascript.JSObject

class Main extends Applet with MouseListener with MouseMotionListener {
    val canvas = new Canvas
    val ui = new UI(canvas)
    val engine = new Engine(canvas)

    override def init() {
	canvas.init
	ui.init(repaint, createImage)
	engine.init(
	    getClass().getResourceAsStream,
	    JSObject.getWindow(this).call
	)

	addMouseListener(this)
	addMouseMotionListener(this)
    }

    override def paint(g: Graphics) {
	g.drawImage(ui.paint(), 0, 0, this)
    }

    override def update(g: Graphics) {
	paint(g)
    }

    def mouseEntered(e: MouseEvent) {}
    def mouseExited(e: MouseEvent) {}
    def mousePressed(e: MouseEvent) {
	val point = e.getPoint()
	ui.mouse_pressed(point.x, point.y, e.getButton() == MouseEvent.BUTTON1)
	mouseDragged(e)
    }
    def mouseReleased(e: MouseEvent) {}
    def mouseClicked(e: MouseEvent) {}

    def mouseDragged(e: MouseEvent) {
	val point = e.getPoint()
	ui.mouse_dragged(point.x, point.y)
    }
    def mouseMoved(e: MouseEvent) {}
}
