/*
 * Flagir - image retrieval for national flag
 *
 *   * FlagirEngine.scala: retrieval engine
 */

import java.io.InputStream
import scala.concurrent.ops

class Engine(canvas: Canvas) {
    import Config.{ div, quant, top }
    private val zones = (div - 1) * (div - 1)

    // feature = path of flag image, entry title of wikipedia and histogram
    private type Feature = (String, String, Array[Double])

    private var features: Array[Feature] = _
    private var scores: Array[Double] = _

    // function that calls javascript function
    private var call_js: (String, Array[AnyRef]) => Unit = _

    // initialize engine
    def init(
	getResourceAsStream: String => InputStream,
	call_js0: (String, Array[AnyRef]) => Unit
    ) {
	// setup feature data
	features = load_data(getResourceAsStream("/features.dat"))
	scores = features map { _ => 0.0 }

	call_js = call_js0

	// spawn notification thread
	ops.spawn { update_loop }
    }

    // wait new histogram and notify result to browser
    private def update_loop {
	while(true) {
	    update_scores
	    notify_browser
	    Thread.sleep(10)
	}
    }

    // wait new histogram and recalculate scores
    val histogram = new Array[Double](zones * 3 * quant)
    private def update_scores {
	// receive histogram from Canvas
	canvas.update_new_histogram(histogram)

	// normalize
	for (n <- 0 until (zones * 3 * quant) by 3) {
	    var sum = 0.0
	    for (m <- 0 until quant) sum += histogram(n + m)
	    for (m <- 0 until quant) histogram(n + m) /= sum
	}

	// calculate scores of every feature
	for (n <- 0 until features.length)
	    scores(n) = score(histogram, features(n)._3)

	// find first top features
	sort_top
    }

    // notify new result to browser
    private val args: Array[AnyRef] = new Array(3)
    private def notify_browser {
	call_js("notify_start", null)
	for (n <- 0 until top) {
	    val score = (zones - scores(n)) * 100.0 / zones
	    args(0) = double2Double(score)
	    args(1) = features(n)._1
	    args(2) = features(n)._2
	    call_js("notify_entry", args)
	}
	call_js("notify_end", null)
    }

    // calculate Bhattacharyya distance of two histograms
    private val all_zone  = 0 until zones
    private val all_color = 0 until (3 * quant)
    private def score(curr_hist: Array[Double], data_hist: Array[Double]) = {
	var sum = 0.0
	for (n <- all_zone) {
	    var sum2 = 0.0
	    for (m <- all_color) {
		val i = n * 3 * quant + m
		val v = curr_hist(i) * data_hist(i)
		sum2 += Math.sqrt(v)
	    }
	    sum += Math.sqrt(3.0 - sum2)
	}
	sum
    }

    // find first n entries by selection algorithm based on quick sort
    private def sort_top {
	def partition(l: Int, r: Int) = {
	    def swap(i: Int, j: Int) = {
		val f = features(i)
		features(i) = features(j)
		features(j) = f
		val s = scores(i)
		scores(i) = scores(j)
		scores(j) = s
	    }
	    val pivot = scores(r)
	    var s = l
	    for (i <- l until r if scores(i) < pivot) { swap(i, s); s += 1 }
	    swap(r, s)
	    s
	}
	def main(l: Int, r: Int) {
	    if (l >= r) return
	    var i = partition(l, r)
	    main(l, i -1)
	    if (i < top) main(i + 1, r)
	}
	main(0, features.length - 1)
    }

    // read data and build features table
    private def load_data(is: InputStream): Array[Feature] = {
	def read_bytes(n: Int) = {
	    val bytes = new Array[Byte](n)
	    var i = 0
	    while (0 <= i && i < n) i += is.read(bytes, i, n - i)
	    bytes
	}
	def read_string: String = {
	    val len = is.read()
	    if (len == -1) return null
	    new String(read_bytes(len))
	}
	def read_array(n: Int) = {
	    val bytes = read_bytes(n * 2)
	    val ary = for (i <- 0 until n) yield {
		val v1 = bytes(i * 2    ) & 0xff
		val v2 = bytes(i * 2 + 1) & 0xff
		(v1 | (v2 << 8)).asInstanceOf[Double] / 65535.0
	    }
	    ary.toArray
	}
	def read_all: List[Feature] = {
	    val file = read_string
	    val title = read_string
	    val feature = read_array(zones * 3 * quant)
	    if (file != null) (file, title, feature)::read_all
	    else List()
	}
	read_all.toArray
    }
}
