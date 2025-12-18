package com.example.todolist.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.example.todolist.core.model.AppLanguage
import java.util.Locale

/**
 * Helper class để đổi ngôn ngữ toàn app
 */
object LocaleHelper {

    /**
     * Áp dụng locale dựa trên AppLanguage
     */
    fun applyLocale(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.VIETNAMESE -> Locale("vi")
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }
        return setLocale(context, locale)
    }

    /**
     * Set locale cho context
     */
    private fun setLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        
        return context.createConfigurationContext(config)
    }

    /**
     * Recreate activity để áp dụng locale mới
     */
    fun recreateActivity(activity: Activity) {
        activity.recreate()
    }

    /**
     * Lấy locale code từ AppLanguage
     */
    fun getLocaleCode(language: AppLanguage): String {
        return when (language) {
            AppLanguage.VIETNAMESE -> "vi"
            AppLanguage.ENGLISH -> "en"
        }
    }
}
