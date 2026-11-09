package com.umnik.os

/**
 * Приложение в каталоге Умник Store.
 * [unavailable] - если не null, приложение получить нельзя, а текст объясняет почему.
 */
class StoreApp(
    val name: String,
    val pkg: String,
    val category: String,
    val emoji: String,
    val desc: String,
    val unavailable: String? = null
)

object StoreCatalog {
    const val ALL = "Все"

    private const val CHAT = "Общение"
    private const val MEDIA = "Видео и музыка"
    private const val GAMES = "Игры"
    private const val MAPS = "Карты и такси"
    private const val SHOP = "Покупки и банки"
    private const val TOOLS = "Инструменты"

    val apps: List<StoreApp> = listOf(
        // Общение
        StoreApp("ВКонтакте", "com.vkontakte.android", CHAT, "🔵", "Соцсеть, сообщения и музыка"),
        StoreApp("MAX", "ru.oneme.app", CHAT, "💬", "Мессенджер от VK"),
        StoreApp("Telegram", "org.telegram.messenger", CHAT, "✈️", "Мессенджер"),
        StoreApp("WhatsApp", "com.whatsapp", CHAT, "🟢", "Сообщения и звонки"),
        StoreApp("Viber", "com.viber.voip", CHAT, "🟣", "Сообщения и звонки"),
        StoreApp("Одноклассники", "ru.ok.android", CHAT, "🟠", "Соцсеть"),
        StoreApp("Discord", "com.discord", CHAT, "🎧", "Голосовые чаты и сообщества"),
        StoreApp("Instagram", "com.instagram.android", CHAT, "📸", "Фото и видео"),
        StoreApp("TikTok", "com.zhiliaoapp.musically", CHAT, "🎵", "Короткие видео"),
        StoreApp("Facebook", "com.facebook.katana", CHAT, "👍", "Соцсеть"),
        StoreApp("X (Twitter)", "com.twitter.android", CHAT, "✖️", "Новости и микроблоги"),
        StoreApp("Snapchat", "com.snapchat.android", CHAT, "👻", "Фото и истории"),
        StoreApp("Reddit", "com.reddit.frontpage", CHAT, "🤖", "Сообщества по интересам"),
        StoreApp("Pinterest", "com.pinterest", CHAT, "📌", "Идеи и подборки картинок"),
        StoreApp("LinkedIn", "com.linkedin.android", CHAT, "💼", "Работа и контакты"),
        StoreApp("Skype", "com.skype.raider", CHAT, "☎️", "Видеозвонки"),
        StoreApp("Zoom", "us.zoom.videomeetings", CHAT, "🎥", "Видеоконференции"),
        StoreApp("iMessage", "com.apple.imessage", CHAT, "💙", "Сообщения Apple", "Только для устройств Apple"),
        StoreApp("FaceTime", "com.apple.facetime", CHAT, "📹", "Видеозвонки Apple", "Только для устройств Apple"),

        // Видео и музыка
        StoreApp("YouTube", "com.google.android.youtube", MEDIA, "▶️", "Видео"),
        StoreApp("VK Видео", "com.vk.vkvideo", MEDIA, "📺", "Видео и трансляции"),
        StoreApp("Rutube", "ru.rutube.app", MEDIA, "🎬", "Видео"),
        StoreApp("Кинопоиск", "ru.kinopoisk", MEDIA, "🍿", "Фильмы и сериалы"),
        StoreApp("Okko", "ru.more.play", MEDIA, "🎞", "Фильмы и сериалы"),
        StoreApp("Netflix", "com.netflix.mediaclient", MEDIA, "🔴", "Фильмы и сериалы"),
        StoreApp("Twitch", "tv.twitch.android.app", MEDIA, "🟪", "Трансляции игр"),
        StoreApp("Spotify", "com.spotify.music", MEDIA, "🎧", "Музыка и подкасты"),
        StoreApp("Яндекс Музыка", "ru.yandex.music", MEDIA, "🎶", "Музыка"),
        StoreApp("Apple Music", "com.apple.android.music", MEDIA, "🍎", "Музыка"),

        // Игры
        StoreApp("Roblox", "com.roblox.client", GAMES, "🧱", "Миллионы игр от игроков"),
        StoreApp("Minecraft", "com.mojang.minecraftpe", GAMES, "⛏", "Строй и выживай"),
        StoreApp("Brawl Stars", "com.supercell.brawlstars", GAMES, "⭐", "Командные бои"),
        StoreApp("Clash Royale", "com.supercell.clashroyale", GAMES, "👑", "Карточные бои"),
        StoreApp("Clash of Clans", "com.supercell.clashofclans", GAMES, "🏰", "Стратегия"),
        StoreApp("Genshin Impact", "com.miHoYo.GenshinImpact", GAMES, "🌌", "Приключения в открытом мире"),
        StoreApp("PUBG Mobile", "com.tencent.ig", GAMES, "🎯", "Королевская битва"),
        StoreApp("Standoff 2", "com.axlebolt.standoff2", GAMES, "🎮", "Шутер"),
        StoreApp("Free Fire", "com.dts.freefireth", GAMES, "🔥", "Королевская битва"),
        StoreApp("Among Us", "com.innersloth.spacemafia", GAMES, "🚀", "Найди предателя"),
        StoreApp("Subway Surfers", "com.kiloo.subwaysurf", GAMES, "🏃", "Бесконечный бег"),
        StoreApp("Fortnite", "com.epicgames.fortnite", GAMES, "🪂", "Королевская битва", "Пока недоступно"),

        // Карты и такси
        StoreApp("Яндекс Карты", "ru.yandex.yandexmaps", MAPS, "🗺", "Карты и навигатор"),
        StoreApp("2ГИС", "ru.dublgis.dgismobile", MAPS, "📍", "Карты и организации"),
        StoreApp("Google Карты", "com.google.android.apps.maps", MAPS, "🧭", "Карты и маршруты"),
        StoreApp("Яндекс Go", "ru.yandex.taxi", MAPS, "🚕", "Такси"),
        StoreApp("Uber", "com.ubercab", MAPS, "🚖", "Такси"),

        // Покупки и банки
        StoreApp("Ozon", "ru.ozon.app.android", SHOP, "🛒", "Маркетплейс"),
        StoreApp("Wildberries", "com.wildberries.ru", SHOP, "🛍", "Маркетплейс"),
        StoreApp("Авито", "com.avito.android", SHOP, "🏷", "Объявления"),
        StoreApp("Яндекс Маркет", "ru.beru.android", SHOP, "🟡", "Маркетплейс"),
        StoreApp("AliExpress", "com.alibaba.aliexpresshd", SHOP, "📦", "Товары из-за рубежа"),
        StoreApp("Сбербанк", "ru.sberbankmobile", SHOP, "🟩", "Банк"),
        StoreApp("Т-Банк", "com.idamob.tinkoff.android", SHOP, "🟨", "Банк"),
        StoreApp("Госуслуги", "ru.rostel", SHOP, "🏛", "Государственные услуги"),

        // Инструменты
        StoreApp("Chrome", "com.android.chrome", TOOLS, "🌐", "Браузер Google"),
        StoreApp("Яндекс Браузер", "com.yandex.browser", TOOLS, "🔎", "Браузер"),
        StoreApp("Яндекс", "ru.yandex.searchplugin", TOOLS, "🔍", "Поиск и Алиса"),
        StoreApp("Gmail", "com.google.android.gm", TOOLS, "✉️", "Почта Google"),
        StoreApp("Яндекс Почта", "ru.yandex.mail", TOOLS, "📧", "Почта"),
        StoreApp("Google Диск", "com.google.android.apps.docs", TOOLS, "🗄", "Облачное хранилище"),
        StoreApp("Google Переводчик", "com.google.android.apps.translate", TOOLS, "🈂️", "Перевод текста"),
        StoreApp("Duolingo", "com.duolingo", TOOLS, "🦉", "Учим языки")
    )

    val categories: List<String> = listOf(ALL) + apps.map { it.category }.distinct()
}
