package com.zavedahmad.yaHabit.database.constants


sealed class PreferenceKeys {
    abstract val key: String
    abstract val defaultValue: String
    abstract val validValues: List<String>

    object AmoledTheme : PreferenceKeys(){
        override val key = "AmoledTheme"
        override val defaultValue = "false"
        override val validValues = Values.entries.map { it.value }

        enum class Values(val value: String) {
            TRUE("true"),
            FALSE("false")
        }
    }

    object FirstDayOfWeek: PreferenceKeys() {
        override val key = "firstDayOfWeek"
        override val defaultValue = "1"
        override val validValues = Values.entries.map { it.value }

        enum class Values(val value: String) {
            MONDAY("1"),
            TUESDAY("2"),
            WEDNESDAY("3"),
            THURSDAY("4"),
            FRIDAY("5"),
            SATURDAY("6"),
            SUNDAY("7")
        }
    }

    object ShowArchive : PreferenceKeys() {
        override val key = "showArchive"
        override val defaultValue = "false"
        override val validValues = Values.entries.map { it.value }
        enum class Values(val value: String) {
            TRUE("true"),
            FALSE("false")
        }
    }

    object ShowActive : PreferenceKeys() {
        override val key = "showActive"
        override val defaultValue = "true"
        override val validValues = Values.entries.map { it.value }
        enum class Values(val value: String) {
            TRUE("true"),
            FALSE("false")
        }
    }

    object ThemeMode : PreferenceKeys() {
        override val key = "ThemeMode"
        override val defaultValue = "system"
        override val validValues = Values.entries.map { it.value }

        enum class Values(val value: String) {
            SYSTEM("system"),
            LIGHT("light"),
            DARK("dark")
        }
    }

    object NotificationsEnabled : PreferenceKeys() {
        override val key = "notificationsEnabled"
        override val defaultValue = "false"
        override val validValues = Values.entries.map { it.value }
        enum class Values(val value: String) {
            TRUE("true"),
            FALSE("false")
        }
    }

    object DefaultReminderHour : PreferenceKeys() {
        override val key = "defaultReminderHour"
        override val defaultValue = "9"
        override val validValues = (0..23).map { it.toString() }
    }

    object DefaultReminderMinute : PreferenceKeys() {
        override val key = "defaultReminderMinute"
        override val defaultValue = "0"
        override val validValues = (0..59).map { it.toString() }
    }
}