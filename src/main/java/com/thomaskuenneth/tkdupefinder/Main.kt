package com.thomaskuenneth.tkdupefinder

import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (thr: ClassNotFoundException) {
            System.err.println(thr.localizedMessage)
        } catch (thr: InstantiationException) {
            System.err.println(thr.localizedMessage)
        } catch (thr: IllegalAccessException) {
            System.err.println(thr.localizedMessage)
        } catch (thr: UnsupportedLookAndFeelException) {
            System.err.println(thr.localizedMessage)
        }
        TKDupeFinderGUI().isVisible = true
    }
}
