package kakkoiichris.gemboozled

import kakkoiichris.hypergame.media.Animation
import kakkoiichris.hypergame.media.SpriteSheet
import kakkoiichris.hypergame.util.filesystem.ResourceManager

object Resources {
    private val manager = ResourceManager("/resources")
    
    private val fonts = manager.getFolder("fnt")
    private val images = manager.getFolder("img")
    
    val font = fonts.getFont("charybdis")
    
    val icon = images.getSprite("Icon")
    
    private val gems = images.getSprite("gems")
    private val omni = images.getSprite("omni")
    private val select = images.getSprite("select")
    private val explode = images.getSprite("explode")
    
    val gemSheet = SpriteSheet(gems, 51, 51)
    val omniAnimation = Animation(SpriteSheet(omni, 51, 51).sprites, 0.05, Animation.Style.LOOP)
    
    private val selectSheet = SpriteSheet(select, 64, 64)
    
    val selectAnimation = Animation(selectSheet.sprites, 0.05, Animation.Style.LOOP)
    
    private val explodeSheet = SpriteSheet(explode, 64, 64)
    
    val explodeAnimation = Animation(explodeSheet.sprites, 0.025, Animation.Style.ONCE)
}