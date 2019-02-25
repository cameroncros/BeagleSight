package com.cross.beaglesightlibs;

import android.database.Cursor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;

import static com.cross.beaglesightlibs.TargetManager.MIGRATION_1_2;
import static junit.framework.TestCase.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MigrationTargetManager {
    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper;

    public MigrationTargetManager() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                TargetManager.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate1To2() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        db.execSQL("INSERT INTO Target ('id', 'name', 'builtin', 'locationId', 'targetId', 'latitude', 'longitude', 'latlng_accuracy', 'altitude', 'altitude_accuracy', 'location_description') " +
                   "VALUES ('id', 'name', 1, 'locationId', 'targetId', 12, 34, 5.6, 7, 8.9, 'location_description')");

        db.execSQL("INSERT INTO LocationDescription ('locationId', 'targetId', 'latitude', 'longitude', 'latlng_accuracy', 'altitude', 'altitude_accuracy', 'location_description') " +
                   "VALUES ('locationId', 'targetId', 12, 34, 5.6, 7, 8.9, 'location_description')");


        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        Cursor cursor = db.query("SELECT lockStatus FROM Target");
        while (cursor.moveToNext())
        {
            assertEquals("WEAK", cursor.getString(0));
        }

        Cursor cursor2 = db.query("SELECT lockStatus FROM LocationDescription");
        while (cursor2.moveToNext())
        {
            assertEquals("WEAK", cursor2.getString(0));
        }
    }
}