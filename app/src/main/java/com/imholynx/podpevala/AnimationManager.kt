package com.imholynx.podpevala


class AnimationManager {

    var startTime: Long

    constructor(duration: Int) {
        this.ANIMATION_DURATION = duration
        startTime = System.currentTimeMillis()
    }

    var ANIMATION_DURATION = 5000 //in ms
    val DEFAULT_POSITION = 0f
    val MAX_POSITION = 1f

    val SPEED = (MAX_POSITION - DEFAULT_POSITION) / ANIMATION_DURATION

    var curPosition = 0f

    fun getPosition(newPosition: Float): Float {
        if (newPosition > curPosition) {
            startTime = System.currentTimeMillis()
            curPosition = newPosition
        } else {
            if (curPosition > 0) {
                curPosition = curPosition - (System.currentTimeMillis() - startTime) * SPEED
            } else {
                curPosition = DEFAULT_POSITION
            }
        }
        return curPosition
    }
}
