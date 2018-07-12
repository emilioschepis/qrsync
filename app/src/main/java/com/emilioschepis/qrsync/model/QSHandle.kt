package com.emilioschepis.qrsync.model

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.emilioschepis.qrsync.R

sealed class QSHandle(@StringRes val name: Int,
                      @DrawableRes val icon: Int) {

    object Instagram : QSHandle(R.string.handle_instagram, R.drawable.ic_instagram)
    object Twitter : QSHandle(R.string.handle_twitter, R.drawable.ic_twitter)
    object GitHub : QSHandle(R.string.handle_github, R.drawable.ic_github_logo)
    object LinkedIn : QSHandle(R.string.handle_linkedin, R.drawable.ic_linkedin)
    object PlayStore : QSHandle(R.string.handle_playstore, R.drawable.ic_playstore)
}