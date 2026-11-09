package com.umnik.os

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Общие способы открывать приложения, сайты и поиск. */
object Launch {

    fun safe(ctx: Context, intent: Intent, message: String = "Не удалось открыть") {
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
        }
    }

    /** Открывает Vexo, при желании сразу на нужной странице. */
    fun vexo(ctx: Context, url: String? = null) {
        val intent = Intent(ctx, VexoActivity::class.java)
        if (url != null) intent.data = Uri.parse(url)
        safe(ctx, intent)
    }

    fun search(ctx: Context, query: String) {
        vexo(ctx, "https://www.google.com/search?q=" + Uri.encode(query))
    }

    /** Адрес сайта открываем как есть, всё остальное ищем в Google. */
    fun toUrl(input: String): String {
        val text = input.trim()
        if (text.startsWith("http://") || text.startsWith("https://")) return text
        val looksLikeSite = !text.contains(' ') && text.contains('.')
        return if (looksLikeSite) "https://$text" else "https://www.google.com/search?q=" + Uri.encode(text)
    }

    fun installed(ctx: Context, pkg: String): Boolean =
        ctx.packageManager.getLaunchIntentForPackage(pkg) != null

    fun openPackage(ctx: Context, pkg: String): Boolean {
        val intent = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        safe(ctx, intent)
        return true
    }

    /** Ищет среди установленных приложений то, в названии которого есть query, и открывает его. */
    fun openAppByName(ctx: Context, query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return false
        val pm = ctx.packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val match = pm.queryIntentActivities(main, 0)
            .firstOrNull { it.loadLabel(pm).toString().lowercase().contains(q) }
            ?: return false
        val launch = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(match.activityInfo.packageName, match.activityInfo.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safe(ctx, launch)
        return true
    }
}
