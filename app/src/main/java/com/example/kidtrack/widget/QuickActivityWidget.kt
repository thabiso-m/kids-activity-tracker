package com.example.kidtrack.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.kidtrack.MainActivity
import com.example.kidtrack.R

class QuickActivityWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val ACTION_ADD_HOMEWORK = "com.example.kidtrack.ADD_HOMEWORK"
        private const val ACTION_ADD_SPORTS = "com.example.kidtrack.ADD_SPORTS"
        private const val ACTION_ADD_CHORES = "com.example.kidtrack.ADD_CHORES"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_activity)

            // Open app intent
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_open_app, openAppPendingIntent)

            // Add Homework intent
            val homeworkIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_HOMEWORK
                putExtra("category", "Homework")
            }
            val homeworkPendingIntent = PendingIntent.getActivity(
                context, 1, homeworkIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_add_homework, homeworkPendingIntent)

            // Add Sports intent
            val sportsIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_SPORTS
                putExtra("category", "Sports")
            }
            val sportsPendingIntent = PendingIntent.getActivity(
                context, 2, sportsIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_add_sports, sportsPendingIntent)

            // Add Chores intent
            val choresIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_CHORES
                putExtra("category", "Chores")
            }
            val choresPendingIntent = PendingIntent.getActivity(
                context, 3, choresIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_add_chores, choresPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
