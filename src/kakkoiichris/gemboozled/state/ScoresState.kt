package kakkoiichris.gemboozled.state

import kakkoiichris.gemboozled.S_GAME
import kakkoiichris.gemboozled.S_SCORES
import kakkoiichris.hypergame.input.Button
import kakkoiichris.hypergame.input.Input
import kakkoiichris.hypergame.media.Colors
import kakkoiichris.hypergame.media.Renderer
import kakkoiichris.hypergame.media.Sprite
import kakkoiichris.hypergame.media.filter.BlurFilter
import kakkoiichris.hypergame.state.State
import kakkoiichris.hypergame.state.StateManager
import kakkoiichris.hypergame.util.Time
import kakkoiichris.hypergame.view.View
import java.awt.Color

class ScoresState : State {
    private lateinit var background: Sprite
    
    private var hue = 0.0
    
    override val name get() = S_SCORES
    
    override fun swapTo(view: View, passed: List<Any>) {
        if (passed.isNotEmpty()) {
            passed.forEach { println(it) }
            
            background = passed[0] as Sprite
            
            background.filter(BlurFilter(BlurFilter.getKernel(5)))
        }
    }
    
    override fun swapFrom(view: View) = Unit
    
    override fun update(view: View, manager: StateManager, time: Time, input: Input) {
        if (input.buttonDown(Button.LEFT)) {
            view.manager.goto(S_GAME)
        }
        
        hue += time.delta * 0.0025
    }
    
    override fun render(view: View, renderer: Renderer) {
        renderer.drawImage(background, 0, 0)
        
        val (red, green, blue) = Colors.toRGB(hue.toFloat(), 1F, 1F)
        
        renderer.paint = Color(red, green, blue,127)
        
        renderer.fillRect((BORDER * 2).toInt(), (BORDER * 2).toInt(), (view.width - BORDER * 4).toInt(), (view.height - BORDER * 4).toInt())
    }
    
    override fun halt(view: View) = Unit
}