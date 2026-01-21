package com.example.kidtrack.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.kidtrack.data.model.Reminder;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderDao_Impl implements ReminderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Reminder> __insertionAdapterOfReminder;

  private final SharedSQLiteStatement __preparedStmtOfDeleteReminderById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRemindersByProfile;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRemindersByActivity;

  public ReminderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminder = new EntityInsertionAdapter<Reminder>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reminders` (`id`,`name`,`timeMinutes`,`frequency`,`associatedActivityId`,`profileId`,`daysBefore`,`eventDateTimestamp`,`snoozeEnabled`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Reminder entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getTimeMinutes());
        statement.bindString(4, entity.getFrequency());
        statement.bindLong(5, entity.getAssociatedActivityId());
        statement.bindLong(6, entity.getProfileId());
        statement.bindLong(7, entity.getDaysBefore());
        statement.bindLong(8, entity.getEventDateTimestamp());
        final int _tmp = entity.getSnoozeEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__preparedStmtOfDeleteReminderById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reminders WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteRemindersByProfile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reminders WHERE profileId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteRemindersByActivity = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reminders WHERE associatedActivityId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertReminder(final Reminder reminder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReminder.insert(reminder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteReminderById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteReminderById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteReminderById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRemindersByProfile(final long profileId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRemindersByProfile.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, profileId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteRemindersByProfile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRemindersByActivity(final long activityId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRemindersByActivity.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, activityId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteRemindersByActivity.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getReminderById(final long id, final Continuation<? super Reminder> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Reminder>() {
      @Override
      @Nullable
      public Reminder call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfAssociatedActivityId = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedActivityId");
          final int _cursorIndexOfProfileId = CursorUtil.getColumnIndexOrThrow(_cursor, "profileId");
          final int _cursorIndexOfDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "daysBefore");
          final int _cursorIndexOfEventDateTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "eventDateTimestamp");
          final int _cursorIndexOfSnoozeEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "snoozeEnabled");
          final Reminder _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final long _tmpAssociatedActivityId;
            _tmpAssociatedActivityId = _cursor.getLong(_cursorIndexOfAssociatedActivityId);
            final long _tmpProfileId;
            _tmpProfileId = _cursor.getLong(_cursorIndexOfProfileId);
            final int _tmpDaysBefore;
            _tmpDaysBefore = _cursor.getInt(_cursorIndexOfDaysBefore);
            final long _tmpEventDateTimestamp;
            _tmpEventDateTimestamp = _cursor.getLong(_cursorIndexOfEventDateTimestamp);
            final boolean _tmpSnoozeEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSnoozeEnabled);
            _tmpSnoozeEnabled = _tmp != 0;
            _result = new Reminder(_tmpId,_tmpName,_tmpTimeMinutes,_tmpFrequency,_tmpAssociatedActivityId,_tmpProfileId,_tmpDaysBefore,_tmpEventDateTimestamp,_tmpSnoozeEnabled);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllReminders(final Continuation<? super List<Reminder>> $completion) {
    final String _sql = "SELECT * FROM reminders ORDER BY timeMinutes ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Reminder>>() {
      @Override
      @NonNull
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfAssociatedActivityId = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedActivityId");
          final int _cursorIndexOfProfileId = CursorUtil.getColumnIndexOrThrow(_cursor, "profileId");
          final int _cursorIndexOfDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "daysBefore");
          final int _cursorIndexOfEventDateTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "eventDateTimestamp");
          final int _cursorIndexOfSnoozeEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "snoozeEnabled");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final long _tmpAssociatedActivityId;
            _tmpAssociatedActivityId = _cursor.getLong(_cursorIndexOfAssociatedActivityId);
            final long _tmpProfileId;
            _tmpProfileId = _cursor.getLong(_cursorIndexOfProfileId);
            final int _tmpDaysBefore;
            _tmpDaysBefore = _cursor.getInt(_cursorIndexOfDaysBefore);
            final long _tmpEventDateTimestamp;
            _tmpEventDateTimestamp = _cursor.getLong(_cursorIndexOfEventDateTimestamp);
            final boolean _tmpSnoozeEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSnoozeEnabled);
            _tmpSnoozeEnabled = _tmp != 0;
            _item = new Reminder(_tmpId,_tmpName,_tmpTimeMinutes,_tmpFrequency,_tmpAssociatedActivityId,_tmpProfileId,_tmpDaysBefore,_tmpEventDateTimestamp,_tmpSnoozeEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRemindersByProfile(final long profileId,
      final Continuation<? super List<Reminder>> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE profileId = ? ORDER BY timeMinutes ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, profileId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Reminder>>() {
      @Override
      @NonNull
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfAssociatedActivityId = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedActivityId");
          final int _cursorIndexOfProfileId = CursorUtil.getColumnIndexOrThrow(_cursor, "profileId");
          final int _cursorIndexOfDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "daysBefore");
          final int _cursorIndexOfEventDateTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "eventDateTimestamp");
          final int _cursorIndexOfSnoozeEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "snoozeEnabled");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final long _tmpAssociatedActivityId;
            _tmpAssociatedActivityId = _cursor.getLong(_cursorIndexOfAssociatedActivityId);
            final long _tmpProfileId;
            _tmpProfileId = _cursor.getLong(_cursorIndexOfProfileId);
            final int _tmpDaysBefore;
            _tmpDaysBefore = _cursor.getInt(_cursorIndexOfDaysBefore);
            final long _tmpEventDateTimestamp;
            _tmpEventDateTimestamp = _cursor.getLong(_cursorIndexOfEventDateTimestamp);
            final boolean _tmpSnoozeEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSnoozeEnabled);
            _tmpSnoozeEnabled = _tmp != 0;
            _item = new Reminder(_tmpId,_tmpName,_tmpTimeMinutes,_tmpFrequency,_tmpAssociatedActivityId,_tmpProfileId,_tmpDaysBefore,_tmpEventDateTimestamp,_tmpSnoozeEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRemindersByActivity(final long activityId,
      final Continuation<? super List<Reminder>> $completion) {
    final String _sql = "SELECT * FROM reminders WHERE associatedActivityId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, activityId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Reminder>>() {
      @Override
      @NonNull
      public List<Reminder> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinutes");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfAssociatedActivityId = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedActivityId");
          final int _cursorIndexOfProfileId = CursorUtil.getColumnIndexOrThrow(_cursor, "profileId");
          final int _cursorIndexOfDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "daysBefore");
          final int _cursorIndexOfEventDateTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "eventDateTimestamp");
          final int _cursorIndexOfSnoozeEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "snoozeEnabled");
          final List<Reminder> _result = new ArrayList<Reminder>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Reminder _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpTimeMinutes;
            _tmpTimeMinutes = _cursor.getInt(_cursorIndexOfTimeMinutes);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final long _tmpAssociatedActivityId;
            _tmpAssociatedActivityId = _cursor.getLong(_cursorIndexOfAssociatedActivityId);
            final long _tmpProfileId;
            _tmpProfileId = _cursor.getLong(_cursorIndexOfProfileId);
            final int _tmpDaysBefore;
            _tmpDaysBefore = _cursor.getInt(_cursorIndexOfDaysBefore);
            final long _tmpEventDateTimestamp;
            _tmpEventDateTimestamp = _cursor.getLong(_cursorIndexOfEventDateTimestamp);
            final boolean _tmpSnoozeEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSnoozeEnabled);
            _tmpSnoozeEnabled = _tmp != 0;
            _item = new Reminder(_tmpId,_tmpName,_tmpTimeMinutes,_tmpFrequency,_tmpAssociatedActivityId,_tmpProfileId,_tmpDaysBefore,_tmpEventDateTimestamp,_tmpSnoozeEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
