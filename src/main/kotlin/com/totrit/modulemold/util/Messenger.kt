package com.totrit.modulemold.util

import com.intellij.openapi.ui.Messages

class Messenger {
    fun displayInfoMessage(info: String, isError: Boolean) {
        Messages.showMessageDialog(info, if (isError) "Error" else "Info", if (isError) Messages.getErrorIcon() else Messages.getInformationIcon())
    }
}