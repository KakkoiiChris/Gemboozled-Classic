package kakkoiichris.gemboozled.state

import kakkoiichris.gemboozled.*
import kakkoiichris.hypergame.input.Input
import kakkoiichris.hypergame.media.Renderer
import kakkoiichris.hypergame.media.filter.BlurFilter
import kakkoiichris.hypergame.media.toSprite
import kakkoiichris.hypergame.state.State
import kakkoiichris.hypergame.state.StateManager
import kakkoiichris.hypergame.util.Time
import kakkoiichris.hypergame.util.math.Box
import kakkoiichris.hypergame.util.math.Vector
import kakkoiichris.hypergame.view.View
import java.awt.Color
import java.awt.Font
import kotlin.math.max

const val BORDER = 20.0
const val DISPLAY = 120.0

class Game : State {
    private val boardBox = Box(BORDER, BORDER, WIDTH - (BORDER * 2), HEIGHT - (BORDER * 3) - DISPLAY)
    private val displayBox = Box(BORDER, HEIGHT - BORDER - DISPLAY, WIDTH - (BORDER * 2), DISPLAY)
    
    private val timeBox: Box
    private val scoreBox: Box
    private val comboBox: Box
    
    private val board = Board()
    
    private var hue = 0.0
    
    override val name get() = S_GAME
    
    init {
        val (a, b, c) = displayBox.divide(3, 1)
        
        timeBox = a
        scoreBox = b
        comboBox = c
    }
    
    override fun swapTo(view: View, passed: List<Any>) {
        board.reset()
    }
    
    override fun swapFrom(view: View) = Unit
    
    override fun update(view: View, manager: StateManager, time: Time, input: Input) {
        if (!board.chaining && board.gameTime <= 0) {
            val noBlur = view.getScreenshot().toSprite()
            
            val blur = noBlur.crop((BORDER * 2).toInt(), (BORDER * 2).toInt(), (WIDTH - (BORDER * 4)).toInt(), (HEIGHT - (BORDER * 4)).toInt())
            blur.filter(BlurFilter(BlurFilter.getKernel(5)))
            
            view.manager.goto(S_SCORES, noBlur, blur, board.score, board.totalTime, board.maxCombo)
            
            return
        }
    
        input.translate(Vector(BORDER, BORDER))
        
        board.update(view, manager, time, input)
    
        input.translate(-Vector(BORDER, BORDER))
        
        hue += time.delta * 0.001
    }
    
    override fun render(view: View, renderer: Renderer) {
        renderer.clearRect(0, 0, view.width, view.height)
        
        renderer.translate(BORDER, BORDER)
        board.render(view, renderer)
        renderer.translate(-BORDER, -BORDER)
        
        renderer.color = Color(Color.HSBtoRGB(hue.toFloat(), 1F, 1F))
        
        renderer.drawRect(boardBox)
        renderer.drawRect(displayBox)
        
        renderer.font = Font(Resources.font, Font.PLAIN, 32)
        renderer.color = Color.WHITE
        
        val minutes = max(board.gameTime / 60, 0.0).toInt()
        val seconds = max(board.gameTime % 60, 0.0).toInt()
        val microseconds = (max((board.gameTime % 60) % 1, 0.0) * 100).toInt()
        
        val timeString = String.format("Time: %d:%02d:%02d", minutes, seconds, microseconds)
        
        renderer.drawString(timeString, timeBox, 0.0, 0.5)
        
        renderer.drawString("Score: ${board.score}", scoreBox, 0.0, 0.5)
        
        renderer.drawString("Combo: ${board.combo} / ${board.maxCombo}", comboBox, 0.0, 0.5)
    }
    
    override fun halt(view: View) = Unit
}