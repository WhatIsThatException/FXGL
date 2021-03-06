/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2016 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.almasb.fxgl.entity.animation

import com.almasb.fxgl.app.FXGL
import com.almasb.fxgl.event.FXGLEvent
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventHandler

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class RotationAnimation(private val animationBuilder: AnimationBuilder,
                        val startAngle: Double, val endAngle: Double) {

    companion object {
        private val log = FXGL.getLogger(RotationAnimation::class.java)
    }

    private val timeline: Timeline
    private var state = AnimationState.INIT

    init {
        timeline = Timeline()
        timeline.delay = animationBuilder.delay
        timeline.cycleCount = animationBuilder.times

        val subscriberPause = FXGL.getEventBus().addEventHandler(FXGLEvent.PAUSE, { e ->
            if (state == AnimationState.PLAYING)
                pause()
        })

        val subscriberResume = FXGL.getEventBus().addEventHandler(FXGLEvent.RESUME, { e ->
            if (state == AnimationState.PAUSED)
                play()
        })

        val value = SimpleDoubleProperty(startAngle)

        val frame = KeyFrame(animationBuilder.duration, KeyValue(value, endAngle))
        timeline.keyFrames.add(frame)

        animationBuilder.entities.map { it.rotationComponent }.forEach {
            it.valueProperty().bind(value)
        }

        timeline.onFinished = EventHandler {
            state = AnimationState.FINISHED

            subscriberPause.unsubscribe()
            subscriberResume.unsubscribe()

            animationBuilder.entities.map { it.rotationComponent }.forEach {
                it.valueProperty().unbind()
            }

            timeline.keyFrames.clear()
        }
    }

    fun play() {
        if (state == AnimationState.FINISHED) {
            log.warning("Attempted to play finished animation")
            return
        }

        timeline.play()
        state = AnimationState.PLAYING
    }

    fun pause() {
        if (state == AnimationState.FINISHED || state == AnimationState.INIT) {
            log.warning("Attempted to pause finished or initializing animation")
            return
        }

        timeline.pause()
        state = AnimationState.PAUSED
    }

    fun finish() {
        if (state == AnimationState.FINISHED) {
            log.warning("Attempted to finish already finished animation")
            return
        }

        timeline.stop()
        state = AnimationState.FINISHED
    }
}