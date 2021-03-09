package com.teamayka.vansaleandmgmt.utils

import com.zebra.sdk.printer.PrinterStatus

object ZErr {
    fun getMessage(currentStatus: PrinterStatus): String {
        when {
            currentStatus.isHeadCold -> {
                return "Printer head is cold"
            }
            currentStatus.isHeadOpen -> {
                return "Printer is open"
            }
            currentStatus.isHeadTooHot -> {
                return "Printer head got over heat"
            }
            currentStatus.isPaperOut -> {
                return "No paper to print"
            }
            currentStatus.isRibbonOut -> {
                return "Ribbon out"
            }
            currentStatus.isReceiveBufferFull -> {
                return "Memory full"
            }
            currentStatus.isPartialFormatInProgress -> {
                return "Printer in progress"
            }
        }
        return "Error to print"
    }
}