package kakkoiichris.gemboozled

import kakkoiichris.hypergame.view.Display
import kakkoiichris.gemboozled.state.*

const val WIDTH = ((BORDER * 2) + (GEM_SIZE * BOARD_SIZE)).toInt()
const val HEIGHT = (WIDTH + DISPLAY + BORDER).toInt()
const val TITLE = "Gem-Boozled!"

const val S_GAME = "game"
const val S_SCORES = "scores"

fun main() {
    val display = Display(WIDTH, HEIGHT, title = TITLE, icon = Resources.icon)
    
    with(display) {
        manager += Game()
        manager += ScoresState()
        
        open()
    }
}