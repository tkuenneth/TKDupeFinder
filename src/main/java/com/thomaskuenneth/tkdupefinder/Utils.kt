/*
 * Copyright 2021 Thomas Kuenneth
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thomaskuenneth.tkdupefinder

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun appIcon() = ImageIO.read(File("app_icon.png")).toBufferedImage()

fun Image.toBufferedImage(): BufferedImage {
    return if (this is BufferedImage) {
        this
    } else {
        BufferedImage(this.getWidth(null),
                this.getHeight(null),
                BufferedImage.TYPE_INT_ARGB).also {
            val graphics = it.createGraphics()
            graphics.drawImage(this, 0, 0, null)
            graphics.dispose()
        }
    }
}