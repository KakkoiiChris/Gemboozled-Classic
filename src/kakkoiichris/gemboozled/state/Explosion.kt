package kakkoiichris.gemboozled.state

import kakkoiichris.gemboozled.Resources
import kakkoiichris.hypergame.input.Input
import kakkoiichris.hypergame.media.*
import kakkoiichris.hypergame.state.StateManager
import kakkoiichris.hypergame.util.Time
import kakkoiichris.hypergame.util.math.Box
import kakkoiichris.hypergame.view.View

const val EXPLODE_SIZE = 51.0

class Explosion(x: Double, y: Double) : Box(x, y, EXPLODE_SIZE, EXPLODE_SIZE), Renderable {
    private val animation = Resources.explodeAnimation.copy()
    
    var removed = false; private set
    
    override fun update(view: View, manager: StateManager, time: Time, input: Input) {
        animation.update(time)
        
        if (animation.elapsed) {
            removed = true
        }
    }
    
    override fun render(view: View, renderer: Renderer) {
        renderer.drawAnimation(animation, this)
    }
}