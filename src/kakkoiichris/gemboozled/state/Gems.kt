package kakkoiichris.gemboozled.state

import kakkoiichris.gemboozled.Resources
import kakkoiichris.gemboozled.util.manhattanDistance
import kakkoiichris.hypergame.input.Input
import kakkoiichris.hypergame.media.Renderable
import kakkoiichris.hypergame.media.Renderer
import kakkoiichris.hypergame.media.Sprite
import kakkoiichris.hypergame.state.StateManager
import kakkoiichris.hypergame.util.Time
import kakkoiichris.hypergame.util.math.Box
import kakkoiichris.hypergame.view.View
import kotlin.random.Random

enum class GemColor {
    RED,
    YELLOW,
    GREEN,
    CYAN,
    BLUE,
    MAGENTA,
    WHITE,
    ALL;
    
    companion object {
        fun random() = values()[Random.nextInt(values().size - 1)]
    }
}

const val GEM_SIZE = 51.0

sealed class Gem(r: Int, c: Int, val color: GemColor) : Box(c * GEM_SIZE, r * GEM_SIZE, GEM_SIZE, GEM_SIZE), Renderable {
    companion object {
        fun random(r: Int = -1, c: Int = -1) = if (Random.nextDouble() < 0.85) {
            BasicGem(r, c)
        }
        else {
            val s = Random.nextDouble()
            
            when {
                s < 1.0 / 7.0 -> CrossGem(r, c)
                s < 2.0 / 7.0 -> ExplodeGem(r, c)
                s < 3.0 / 7.0 -> SoleGem(r, c)
                s < 4.0 / 7.0 -> ScatterGem(r, c)
                s < 5.0 / 7.0 -> WarpGem(r, c)
                s < 6.0 / 7.0 -> BonusGem(r, c)
                else          -> {
                    val t = Random.nextDouble()
                    
                    when {
                        t < 1.0 / 2.0 -> TenSecondGem(r, c)
                        t < 5.0 / 6.0 -> TwentySecondGem(r, c)
                        else          -> ThirtySecondGem(r, c)
                    }
                }
            }
        }
    }
    
    open val score = 10
    
    var removed = false
    
    fun moveTo(r: Int, c: Int) {
        x = c * GEM_SIZE
        y = r * GEM_SIZE
    }
    
    open fun allowMove(ra: Int, ca: Int, rb: Int, cb: Int) =
        manhattanDistance(ra, ca, rb, cb) == 1
    
    abstract fun getSprite(): Sprite
    
    open fun affectBoard(r: Int, c: Int, board: Board) = Unit
    
    override fun update(view: View, manager: StateManager, time: Time, input: Input) = Unit
    
    override fun render(view: View, renderer: Renderer) {
        renderer.drawImage(getSprite(), position)
    }
}

class BasicGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 0
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
}

class CrossGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 1
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
    
    override fun affectBoard(r: Int, c: Int, board: Board) {
        for (i in 0 until BOARD_SIZE) {
            board.removeGem(board[r, i])
            board.removeGem(board[i, c])
        }
    }
}

class ExplodeGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 2
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
    
    override fun affectBoard(r: Int, c: Int, board: Board) {
        for (or in -2..2) {
            for (oc in -2..2) {
                if (
                    manhattanDistance(0, 0, or, oc) <= 2
                    && r + or in 0 until BOARD_SIZE
                    && c + oc in 0 until BOARD_SIZE
                ) {
                    board.removeGem(board[r + or, c + oc])
                }
            }
        }
    }
}

class SoleGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 3
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
    
    override fun affectBoard(r: Int, c: Int, board: Board) =
        board.filter { it?.color == color }
            .forEach(board::removeGem)
}

class ScatterGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 4
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
    
    override fun affectBoard(r: Int, c: Int, board: Board) {
        repeat(10) {
            var rr: Int
            var cc: Int
            
            do {
                rr = Random.nextInt(BOARD_SIZE)
                cc = Random.nextInt(BOARD_SIZE)
            }
            while (board[rr, cc]?.removed == true)
            
            board.removeGem(board[rr, cc])
        }
    }
}

class WarpGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 5
    }
    
    override fun allowMove(ra: Int, ca: Int, rb: Int, cb: Int) = true
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
}

class BonusGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 6
    }
    
    override val score get() = 25
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
}

class TenSecondGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 7
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
    
    override fun affectBoard(r: Int, c: Int, board: Board) {
        board.gameTime += 10
    }
}

class TwentySecondGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 8
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
    
    override fun affectBoard(r: Int, c: Int, board: Board) {
        board.gameTime += 20
    }
}

class ThirtySecondGem(r: Int, c: Int, color: GemColor = GemColor.random()) : Gem(r, c, color) {
    companion object {
        private const val TYPE = 9
    }
    
    override fun getSprite() = Resources.gemSheet[color.ordinal, TYPE]
    
    override fun affectBoard(r: Int, c: Int, board: Board) {
        board.gameTime += 30
    }
}

class OmniGem(r: Int, c: Int) : Gem(r, c, GemColor.ALL) {
    private val animation = Resources.omniAnimation.copy()
    
    init {
        animation.running = true
    }
    
    override fun getSprite() = animation.frame
    
    override fun update(view: View, manager: StateManager, time: Time, input: Input) {
        animation.update(time)
    }
}