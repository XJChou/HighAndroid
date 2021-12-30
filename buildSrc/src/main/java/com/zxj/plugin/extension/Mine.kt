package com.zxj.plugin.extension

/**
 * 需要open，gradle需要集成包装
 * * An exception occurred applying plugin request [id: 'com.zxj.plugin.mine']
 * * > Failed to apply plugin 'com.zxj.plugin.mine'.
 * * > Could not create an instance of type com.zxj.plugin.extension.Mine.
 * * > Class Mine is final.
 */
open class Mine {
    var name = "default"
}