/*
 * Flagir - image retrieval for national flag
 *
 *   * FlagirBuilder.scala: feature data builder
 */

import java.io.File
import javax.imageio.ImageIO

object Builder {
    import Config.{ div, quant }
    def main(args: Array[String]) {
	for (path <- args) {
	    val start = System.currentTimeMillis()
	    val hist3d = calc(path)
	    val time = System.currentTimeMillis() - start
	    val ary = Array(path, hist3d deepMkString(","), time + "ms")
	    println(ary deepMkString(":"))
	}
    }

    type Hist1D = Array[Int]    // histogram for single channel
    type Hist2D = Array[Hist1D] // histogram for colored channels
    type Hist3D = Array[Hist2D] // histogram for zoned and colored channels

    // calculate histogram of image
    def calc(path: String) = {
	val image = ImageIO read(new File(path))
	val w = image.getWidth  / div
	val h = image.getHeight / div

	// helper functions
	def binop[A](f: (A, A) => A)(hl: Array[A], hr: Array[A]) =
	    for ((l, r) <- hl zip hr) yield f(l, r)
	def binop1d(f: (Int, Int) => Int) = binop(f) _
	def binop2d(f: (Int, Int) => Int) = binop(binop1d(f)) _
	def binop3d(f: (Int, Int) => Int) = binop(binop2d(f)) _
	def sub2d = binop2d { _ - _ }
	def sub3d = binop3d { _ - _ }
	def add3d = binop3d { _ + _ }
	def copy1d(h: Hist1D) = h map { x => x }
	def copy2d(h: Hist2D) = h map copy1d
	def copy3d(h: Hist3D) = h map copy2d

	// map and accumlate from 0 until n*div, pickup values at 0, div,
	// 2*div, ..., n*div, and returns diffs of every other pair.
	def each_zone[A](n: Int, acc: A, copy: A => A, sub: (A, A) => A,
			 step: (A, Int) => A) = {
	    val points: Array[A] = new Array(div + 1)
	    var a = acc
	    for (i <- 0 until (n * div)) {
		if (i % n == 0) points(i / n) = copy(a)
		    a = step(a, i)
	    }
	    points(div) = a
	    (0 until (div - 1)).toArray map {
		(n: Int) => sub(points(n + 2), points(n))
	    }
	}

	// update histogram at every pixel (core loop)
	val zero_to_two = 0 to 2 // avoid to create objects
	def each_pixel(x: Int, y: Int, hist2d: Hist2D) = {
	    val rgb = image getRGB(x, y)
	    for (n <- zero_to_two) {
		val bin = ((rgb >> (16 - n * 8)) & 0xff) * quant / 256
		if (bin > 0        ) hist2d(n)(bin - 1) += 1
				     hist2d(n)(bin    ) += 3
		if (bin < quant - 1) hist2d(n)(bin + 1) += 1
	    }
	    hist2d
	}

	val accum2d: Hist2D = new Array(     3, quant)
	val accum3d: Hist3D = new Array(div, 3, quant)

	val hist3ds = each_zone( // y
	    h, accum3d, copy3d, sub3d,
	    { (hist3d: Hist3D, y) =>
		val hist2ds = each_zone( // x
		    w, accum2d, copy2d, sub2d,
		    { (hist2d: Hist2D, x) => each_pixel(x, y, hist2d) })
		add3d(hist3d, hist2ds)
	    })

	hist3ds map { _ map { _ map {
	    hist1d =>
		val s = sum(hist1d)
		val h = hist1d map {
		    n => (n.asInstanceOf[Long] * 65535 / s).asInstanceOf[Int]
		}
		h(quant - 1) += 65535 - sum(h)
		h
	}}}
    }

    // helper functions
    def flatten[A](a: Array[Array[A]]) = { a reduceLeft { (z, x) => z ++ x } }
    def sum(a: Array[Int]) = { a reduceLeft { (z, x) => z + x } }
}
