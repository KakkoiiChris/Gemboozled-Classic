package kakkoiichris.gemboozled.state

import kakkoiichris.gemboozled.Resources
import kakkoiichris.hypergame.input.Button
import kakkoiichris.hypergame.input.Input
import kakkoiichris.hypergame.media.Renderable
import kakkoiichris.hypergame.media.Renderer
import kakkoiichris.hypergame.state.StateManager
import kakkoiichris.hypergame.util.Time
import kakkoiichris.hypergame.view.View
import java.awt.Color
import kotlin.math.max
import kotlin.math.pow

const val BOARD_SIZE = 8

class Board : Renderable {
    private val queue = Array<Gem>(BOARD_SIZE) { i -> BasicGem(-1, i) }
    
    private val board = Array<Gem?>(BOARD_SIZE * BOARD_SIZE) { i ->
        val r = i % BOARD_SIZE
        val c = i / BOARD_SIZE
        BasicGem(r, c)
    }
    
    private val scoredGems = mutableListOf<Gem>()
    
    var gameTime = 120.0
    var totalTime = 0.0; private set
    
    var score = 0; private set
    var combo = 0; private set
    var maxCombo = 0; private set
    
    private var updateTimer = 0.0
    private var timeTimer = 0.0
    
    private var swapping = false
    private var swapped = false
    var chaining = false; private set
    
    private var rowA = -1
    private var colA = -1
    private var rowB = -1
    private var colB = -1
    private var rowM = -1
    private var colM = -1
    
    private val explosions = mutableListOf<Explosion>()
    
    private var hue = 0F
    
    init {
        constrainBoard()
    }
    
    fun reset() {
        for (i in queue.indices) {
            queue[i] = BasicGem(-1, i)
        }
        
        for (i in board.indices) {
            val r = i % BOARD_SIZE
            val c = i / BOARD_SIZE
            board[i] = BasicGem(r, c)
        }
        
        constrainBoard()
        
        Resources.selectAnimation.reset()
        
        gameTime = 120.0
        
        totalTime = 0.0
        score = 0
        combo = 0
        maxCombo = 0
        
        updateTimer = 0.0
        timeTimer = 0.0
        
        swapping = false
        swapped = false
        chaining = false
        
        rowA = -1
        colA = -1
        rowB = -1
        colB = -1
        rowM = -1
        colM = -1
    }
    
    fun filter(predicate: (Gem?) -> Boolean) = board.filter(predicate)
    
    operator fun get(r: Int, c: Int) =
        if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE)
            board[r * BOARD_SIZE + c]
        else
            null
    
    operator fun set(r: Int, c: Int, gem: Gem?) {
        if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE) {
            gem?.moveTo(r, c)
            
            board[r * BOARD_SIZE + c] = gem
        }
    }
    
    private fun constrainBoard() {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val cc = get(r, c)?.color?.ordinal ?: -2
                val cu = if (r - 1 >= 0) get(r - 1, c)?.color?.ordinal ?: -1 else -1
                val cd = if (r + 1 < BOARD_SIZE) get(r + 1, c)?.color?.ordinal ?: -1 else -1
                val cl = if (c - 1 >= 0) get(r, c - 1)?.color?.ordinal ?: -1 else -1
                val cr = if (c + 1 < BOARD_SIZE) get(r, c + 1)?.color?.ordinal ?: -1 else -1
                
                if (cu == cc && cc == cd || cl == cc && cc == cr) {
                    do {
                        set(r, c, BasicGem(r, c))
                    }
                    while (get(r, c)?.color?.ordinal == cc)
                }
            }
        }
    }
    
    override fun update(view: View, manager: StateManager, time: Time, input: Input) {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val gem = get(r, c)
                
                if (gem != null && input.mouse in gem) {
                    rowM = r
                    colM = c
                    
                    if (input.buttonDown(Button.LEFT)) {
                        if (rowA + colA == -2) {
                            rowA = r
                            colA = c
                        }
                        else {
                            rowB = r
                            colB = c
                            
                            swapping = true
                        }
                        
                        updateTimer = 0.0
                    }
                }
            }
        }
        
        board.forEach { it?.update(view, manager, time, input) }
        
        Resources.selectAnimation.update(time)
        
        gameTime -= time.seconds
        totalTime += time.seconds
        
        hue += (time.delta * 0.001F).toFloat()
        
        val updateSpeed = view.frameRate / 5.0
        
        updateTimer += time.delta
        
        if (updateTimer >= updateSpeed) {
            updateTimer -= updateSpeed
            
            if (swapped) {
                if (!(validateGems(rowA, colA) || validateGems(rowB, colB))) {
                    swapGems()
                }
                
                swapping = false
                swapped = false
                rowA = -1
                colA = -1
                rowB = -1
                colB = -1
            }
            
            if (swapping) {
                swapped = true
                
                swapGems()
            }
            else if (!removeGems()) {
                if (!fallGems()) {
                    if (!matchGems()) {
                        chaining = false
                        
                        maxCombo = max(combo, maxCombo)
                        
                        combo = 0
                    }
                }
                else {
                    dropQueue()
                }
            }
            else {
                chaining = true
                
                combo++
                
                for (gem in scoredGems) {
                    score += gem.score * (2.0.pow(combo - 1)).toInt()
                }
                
                scoredGems.clear()
            }
            
            fixPositions()
        }
        
        explosions.forEach { it.update(view, manager, time, input) }
        
        explosions.filter(Explosion::removed).forEach { explosions.remove(it) }
    }
    
    private fun validateGems(r: Int, c: Int): Boolean {
        val cc = get(r, c)?.color?.ordinal ?: -2
        val cu = if (r - 1 >= 0) get(r - 1, c)?.color?.ordinal ?: -1 else -1
        val cd = if (r + 1 < BOARD_SIZE) get(r + 1, c)?.color?.ordinal ?: -1 else -1
        val cl = if (c - 1 >= 0) get(r, c - 1)?.color?.ordinal ?: -1 else -1
        val cr = if (c + 1 < BOARD_SIZE) get(r, c + 1)?.color?.ordinal ?: -1 else -1
        val cuu = if (r - 2 >= 0) get(r - 2, c)?.color?.ordinal ?: -1 else -1
        val cdd = if (r + 2 < BOARD_SIZE) get(r + 2, c)?.color?.ordinal ?: -1 else -1
        val cll = if (c - 2 >= 0) get(r, c - 2)?.color?.ordinal ?: -1 else -1
        val crr = if (c + 2 < BOARD_SIZE) get(r, c + 2)?.color?.ordinal ?: -1 else -1
        
        return when {
            cuu == cu && cu == cc                                -> true
            cu == cc && cc == cd                                 -> true
            cc == cd && cd == cdd                                -> true
            cll == cl && cl == cc                                -> true
            cl == cc && cc == cr                                 -> true
            cc == cr && cr == crr                                -> true
            cc == GemColor.ALL.ordinal && (cu == cd && cu != -1) -> true
            cc == GemColor.ALL.ordinal && (cl == cr && cl != -1) -> true
            else                                                 -> false
        }
    }
    
    private fun swapGems() {
        val gemA = get(rowA, colA)
        val gemB = get(rowB, colB)
        
        if (gemA != null && gemB != null) {
            if (gemA.allowMove(rowA, colA, rowB, colB) || gemB.allowMove(rowB, colB, rowA, colA)) {
                set(rowA, colA, gemB)
                set(rowB, colB, gemA)
            }
            else {
                swapping = false
                swapped = false
                rowA = -1
                colA = -1
                rowB = -1
                colB = -1
            }
        }
    }
    
    private fun fallGems(): Boolean {
        var falling = false
        
        for (r in BOARD_SIZE - 2 downTo 0) {
            for (c in 0 until BOARD_SIZE) {
                if (get(r + 1, c) == null) {
                    set(r + 1, c, get(r, c))
                    set(r, c, null)
                    
                    falling = true
                }
            }
        }
        
        for (c in 0 until BOARD_SIZE) {
            if (get(0, c) == null) {
                falling = true
            }
        }
        
        return falling
    }
    
    private fun matchGems(): Boolean {
        var matched = false
        
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val cc = get(r, c)?.color?.ordinal ?: -2
                
                val cu = if (r - 1 >= 0) get(r - 1, c)?.color?.ordinal ?: -1 else -1
                val cd = if (r + 1 < BOARD_SIZE) get(r + 1, c)?.color?.ordinal ?: -1 else -1
                val cl = if (c - 1 >= 0) get(r, c - 1)?.color?.ordinal ?: -1 else -1
                val cr = if (c + 1 < BOARD_SIZE) get(r, c + 1)?.color?.ordinal ?: -1 else -1
                
                if (cu == cc && cc == cd) {
                    matchVertical(r, c, cc)
                    matched = true
                }
                
                if (cl == cc && cc == cr) {
                    matchHorizontal(r, c, cc)
                    matched = true
                }
                
                if (cc == GemColor.ALL.ordinal && (cu == cd && cu != -1)) {
                    matchVertical(r, c, cu)
                    matched = true
                }
                
                if (cc == GemColor.ALL.ordinal && (cl == cr && cl != -1)) {
                    matchHorizontal(r, c, cl)
                    matched = true
                }
            }
        }
        
        return matched
    }
    
    private fun matchVertical(r: Int, c: Int, n: Int) {
        for (i in r downTo 0) {
            val gem = get(i, c)
            
            if (gem?.color?.ordinal == n) {
                removeGem(gem)
                
                gem.affectBoard(i, c, this)
            }
            else {
                break
            }
        }
        
        for (i in r + 1 until BOARD_SIZE) {
            val gem = get(i, c)
            
            if (gem?.color?.ordinal == n) {
                removeGem(gem)
                
                gem.affectBoard(i, c, this)
            }
            else {
                break
            }
        }
    }
    
    private fun matchHorizontal(r: Int, c: Int, n: Int) {
        for (i in c downTo 0) {
            val gem = get(r, i)
            
            if (gem?.color?.ordinal == n) {
                removeGem(gem)
                
                gem.affectBoard(r, i, this)
            }
            else {
                break
            }
        }
        
        for (i in c + 1 until BOARD_SIZE) {
            val gem = get(r, i)
            
            if (gem?.color?.ordinal == n) {
                removeGem(gem)
                
                gem.affectBoard(r, i, this)
            }
            else {
                break
            }
        }
    }
    
    fun removeGem(gem: Gem?) {
        gem?.removed = true
        
        explosions += Explosion(gem?.x ?: 0.0, gem?.y ?: 0.0)
    }
    
    private fun removeGems(): Boolean {
        var removed = false
        
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val gem = get(r, c)
                
                if (gem != null && gem.removed) {
                    scoredGems.add(gem)
                    
                    set(r, c, null)
                    
                    removed = true
                }
            }
        }
        
        return removed
    }
    
    private fun dropQueue() {
        for (i in queue.indices) {
            if (get(0, i) == null) {
                set(0, i, queue[i])
                
                queue[i] = Gem.random()
            }
        }
    }
    
    private fun fixPositions() {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                get(r, c)?.moveTo(r, c)
            }
        }
    }
    
    override fun render(view: View, renderer: Renderer) {
        renderer.color = Color(Color.HSBtoRGB(hue, 1F, 0.5F))
        
        val gem = get(rowM, colM)
        
        if (gem != null) {
            renderer.fillRect(gem)
        }
        
        board.forEach { it?.render(view, renderer) }
        
        if (rowA > -1 && colA > -1 && get(rowA, colA) != null) {
            renderer.drawAnimation(Resources.selectAnimation, get(rowA, colA)!!)
        }
        
        if (rowB > -1 && colB > -1 && get(rowB, colB) != null) {
            renderer.drawAnimation(Resources.selectAnimation, get(rowB, colB)!!)
        }
        
        explosions.forEach { it.render(view, renderer) }
    }
}