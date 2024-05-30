package com.gotcha.narandee

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.gotcha.narandee.src.login.LoginActivity

/**
 * Implementation of App Widget functionality.
 */
class NarandeeAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val pendingIntent = Intent(context, LoginActivity::class.java)
        .let { intent ->
            intent.putExtra("from_widget", true)
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    val widgetText = context.getString(R.string.app_name)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.nanrandee_app_widget).apply {
//        setTextViewText(R.id.widget_iv, widgetText)
        setOnClickPendingIntent(R.id.widget_iv, pendingIntent)
    }


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}