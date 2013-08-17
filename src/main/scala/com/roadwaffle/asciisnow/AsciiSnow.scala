package com.roadwaffle.asciisnow

import com.googlecode.lanterna._
import com.googlecode.lanterna.terminal._
import com.googlecode.lanterna.terminal.text._
import java.nio.charset.Charset
import scala.util.Random
import java.lang.Math
import scala.util.control.Breaks._

object AsciiSnow {

	val terminal = new UnixTerminal(System.in, System.out, Charset.forName("UTF8"))
	//val terminal = TerminalFacade.createTerminal(System.in, System.out, Charset.forName("UTF8"))

	// Extra VT100 escape codes...
	val ESCAPE: Char = 0x1b;
	val HIDE_CURSOR = ESCAPE+"[?25l"
	def hideCursor = { print(HIDE_CURSOR) }
	val SHOW_CURSOR = ESCAPE+"[?25h"
	def showCursor = { print(SHOW_CURSOR) }

	val REFRESH = 30;

	val cols = terminal.getTerminalSize.getColumns
	val rows = terminal.getTerminalSize.getRows
	val rand = new Random
	val numflakes: Int = cols
	val flakes = for (i <- (0 to numflakes).toList) yield new Flake().init

	def main(args: Array[String]) {
		try {
			println(HIDE_CURSOR);
			terminal.enterPrivateMode
			terminal.applyForegroundColor(Terminal.Color.WHITE)
			terminal.applyBackgroundColor(Terminal.Color.BLACK)
			terminal.clearScreen
			for(y <- 0 to rows) for(x <- 0 to cols) { terminal.moveCursor(x, y); terminal.putCharacter(' ') }

			breakable {
				while(true) {
					flakes.foreach(_.update)
					terminal.flush
					terminal.readInput match {
						case null => 
						case _ => break
					}
			  		Thread.sleep(REFRESH)
				}
			}
		} finally {
			terminal.exitPrivateMode
			println(SHOW_CURSOR);
		}
	}

	class Flake() {
		var x: Float = 0f
		var y: Float = 0f
		var dx: Float = 0f
		var dy: Float = 1f
		var flakeChar: Char = '*'

		def update() {
			clear
			move
			draw
		}

		def clear() {
			terminal.moveCursor(Math.round(x), Math.round(y))
			terminal.putCharacter(' ')
		}

		def move() {
			x += dx
			y += dy
			if(x > cols) x = 0
			if(x < 0) x = cols
			if(y > rows) init
		}

		def draw() {
			if(y >= 0) {
			terminal.moveCursor(Math.round(x), Math.round(y))
			terminal.putCharacter(flakeChar)
			}
		}

		def init(): Flake = {
			x = rand.nextInt(cols)
			//y = rand.nextFloat * -20f
			y = 0
			dx = (rand.nextFloat * 0.75f) - 0.25f
			dy = (rand.nextFloat * 0.5f) + 0.3f
			flakeChar = if(rand.nextBoolean) '.' else '*'
			if(flakeChar == '*')
				dy += 0.3f
			this
		}
	}

}
