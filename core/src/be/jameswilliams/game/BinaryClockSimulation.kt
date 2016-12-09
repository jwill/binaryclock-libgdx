package be.jameswilliams.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.utils.viewport.FillViewport
import java.util.*

class BinaryClockSimulation : ApplicationAdapter() {
    private val TAG: String = BinaryClockSimulation::class.java.simpleName

    val BLOCK_SIZE = 20f
    val VERTICAL_GAP = 10f
    val HORIZONTAL_GAP = 30f

    val CIRCLE_RADIUS = 8f
    val COLON_VERTICAL_SPACING = 50f

    val LINE_SPACING = 50f

    val WORLD_WIDTH = 640f
    val WORLD_HEIGHT = 480f

    val camera = OrthographicCamera()

    var timeSinceLastUpdate = 0f

    lateinit var batch: SpriteBatch
    lateinit var shapeRenderer: ShapeRenderer
    lateinit var font: BitmapFont

    lateinit var viewport: FillViewport


    override fun create() {
        batch = SpriteBatch()

        // Use Hiero the Bitmap Font Tool
        font = BitmapFont(Gdx.files.internal("myfont.fnt"))

        shapeRenderer = ShapeRenderer()

        viewport = FillViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)
    }

    fun drawLine(renderer: ShapeRenderer, digit: Char, xPos: Float, yPos: Float, length: Int = 4) {
        val number = Integer.valueOf("" + digit)
        var numberInBinary = Integer.toBinaryString(number)

        /**
         * Java defaults to giving you the shortest value so if you convert 3 to binary,
         * it returns 11. To make our clock not flicker, we have a set number of spaces.
         * If the length is less than the specified length, the following pads the front
         * with zeros to make it long enough. So the 11 will become 0011.
         */
        while (numberInBinary.length < length) {
            numberInBinary = "0" + numberInBinary
        }

        /**
         * We need to render our numbers from the bottom up.
         * For 8, 1000 in binary, which is stored as ['1','0','0','0']. We want to color it like this.
         * GREEN    1
         * RED      0
         * RED      0
         * RED      0
         *
         * So reverse the order to ['0', '0', '0', '1'] and render it.
         */

        numberInBinary = numberInBinary.reversed()

        for (i in 0..numberInBinary.length - 1) {
            if (numberInBinary[i] == '0') {
                renderer.color = Color.RED
            } else {
                renderer.color = Color.GREEN
            }

            renderer.rect(xPos, yPos + (i * (BLOCK_SIZE + VERTICAL_GAP)), BLOCK_SIZE, BLOCK_SIZE)
        }
    }

    fun drawColon(renderer: ShapeRenderer, xPos: Float, yPos: Float) {
        renderer.color = Color.GRAY
        renderer.circle(xPos, yPos, CIRCLE_RADIUS)
        renderer.circle(xPos, yPos + COLON_VERTICAL_SPACING, CIRCLE_RADIUS)
    }

    fun formatTime(): Map<String, String> {
        val now = GregorianCalendar()
        now.get(Calendar.HOUR)

        val hour = now.get(Calendar.HOUR_OF_DAY).toString()
        val minute = now.get(Calendar.MINUTE).toString()
        val seconds = now.get(Calendar.SECOND).toString()

        val components = mutableMapOf<String, String>(
                "hour" to hour,
                "minute" to minute,
                "seconds" to seconds)
        // Add leading zero
        for (i in components.keys) {
            if (components.get(i)?.length == 1)
                components.put(i, "0" + components[i])
        }
        return components
    }


    fun drawClock(hour: String, minute: String, seconds: String, xPos: Float, yPos: Float) {
        var currentTotal = xPos

        with(shapeRenderer) {
            // 0,0 is lower left
            drawLine(this, hour[0], xPos, yPos, 2)
            currentTotal += HORIZONTAL_GAP
            drawLine(this, hour[1], currentTotal, yPos)

            currentTotal += LINE_SPACING
            drawLine(this, minute[0], currentTotal, yPos, 3)

            currentTotal += HORIZONTAL_GAP
            drawLine(this, minute[1], currentTotal, yPos)

            currentTotal += LINE_SPACING
            drawLine(this, seconds[0], currentTotal, yPos, 3)

            currentTotal += HORIZONTAL_GAP
            drawLine(this, seconds[1], currentTotal, yPos)

            drawColon(this, xPos + 65f, yPos + 30f)
            drawColon(this, xPos + 145f, yPos + 30f)
        }
    }

    override fun render() {
        viewport.apply(true)

        batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        timeSinceLastUpdate += Gdx.graphics.deltaTime

        /**
         * Given that a change to the clock will only happen once a second
         * there's no reason to render most of the time. If the time since the last update
         * is greater than 0.4 secs, it will update. This is to make sure there aren't any
         * weird hiccups with time.
         */

        if (timeSinceLastUpdate != 0f && timeSinceLastUpdate < 0.4f)
            return

        val time = formatTime()
        // !! means trust me, this won't throw an exception
        val hour = time["hour"]!!
        val minute = time["minute"]!!
        val seconds = time["seconds"]!!

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        font.draw(batch, "${hour}:${minute}:${seconds}", WORLD_WIDTH / 2f - 100f, WORLD_HEIGHT / 2f + 150f)
        batch.end()

        shapeRenderer.setAutoShapeType(true)
        shapeRenderer.begin(ShapeType.Filled)
        drawClock(hour, minute, seconds, WORLD_WIDTH / 2f - 150f, WORLD_HEIGHT / 2f - 100f)
        shapeRenderer.end()

        timeSinceLastUpdate = 0f
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
    }
}