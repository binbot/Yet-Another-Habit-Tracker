package com.zavedahmad.yaHabit.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v6 -> v7: routines gain first-class steps.
 *
 * Creates [com.zavedahmad.yaHabit.database.entities.RoutineStepEntity] and
 * [com.zavedahmad.yaHabit.database.entities.RoutineStepCompletionEntity], then seeds one step per
 * existing habit-linked RoutineItem, copying the habit's name/description once (no live link).
 *
 * Past routine history is not attributable to steps, so no step completions are backfilled;
 * habit completions are untouched. The legacy RoutineItem table is intentionally left in place so
 * the current routine screens keep working until the step UI lands.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `RoutineStep` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`routineId` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, " +
                "`position` INTEGER NOT NULL, FOREIGN KEY(`routineId`) REFERENCES `RoutineTable`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_RoutineStep_routineId` ON `RoutineStep` (`routineId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `RoutineStepCompletion` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`stepId` INTEGER NOT NULL, `completionDate` INTEGER NOT NULL, `isDone` INTEGER NOT NULL, " +
                "`isSkip` INTEGER NOT NULL, FOREIGN KEY(`stepId`) REFERENCES `RoutineStep`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_RoutineStepCompletion_stepId_completionDate` " +
                "ON `RoutineStepCompletion` (`stepId`, `completionDate`)"
        )

        db.execSQL(
            "INSERT INTO `RoutineStep` (`routineId`, `name`, `description`, `position`) " +
                "SELECT `ri`.`routineId`, `h`.`name`, `h`.`description`, `ri`.`index` " +
                "FROM `RoutineItem` AS `ri` JOIN `HabitTable` AS `h` ON `h`.`id` = `ri`.`habitId`"
        )
    }
}
