# KidTrack - Version History

## Version 2.1.0 (Build 4) - 2026-01-17

### New Features
- ✨ **Day-Before Reminders**: Automatically schedule reminders 1 day before events
- ✨ **Predefined Tasks**: 26 pre-configured activity templates across 6 categories
  - School (5 tasks)
  - Activity (5 tasks)
  - Health (4 tasks)
  - Routine (5 tasks)
  - Social (3 tasks)
  - Chore (3 tasks)
- ✨ **Sound Alerts**: Notifications now play sound and vibrate
- ✨ **Snooze Functionality**: 10-minute snooze button in reminder notifications
- ✨ **Build Info**: Version and build information displayed in app

### Improvements
- 🔊 Enhanced notification channel with high priority
- 📳 Vibration pattern added to reminders
- 💬 Improved notification messages with activity details
- 🎨 Better UI for activity creation with predefined task selector

### Database Changes
- Upgraded database to version 4
- Added `daysBefore` field to reminders table
- Added `eventDateTimestamp` field to reminders table
- Safe migration with no data loss

### Bug Fixes
- 🐛 Fixed missing launcher icons
- 🔧 Enhanced error handling in reminder scheduling

---

## Version 2.0.0 (Build 3) - Previous Release

### Features
- Activity tracking with categories
- User profiles for children
- Reminder system
- Calendar view
- Reports and statistics
- Widget support
- File export functionality

---

## Future Roadmap

### Planned Features
- [ ] Custom snooze duration (5/15/30 minutes)
- [ ] Custom notification sounds
- [ ] Recurring activities
- [ ] Activity templates management
- [ ] Dark mode
- [ ] Backup and restore
- [ ] Cloud sync
- [ ] Multi-language support
- [ ] Activity completion tracking
- [ ] Reward system

---

## Version Numbering

**Format**: MAJOR.MINOR.PATCH (Build CODE)

- **MAJOR**: Incompatible API changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)
- **Build CODE**: Incremental build number

---

## Build Information

Current version info can be accessed via:
```kotlin
BuildInfo.versionName      // "2.1.0"
BuildInfo.versionCode      // 4
BuildInfo.fullVersionString // "KidTrack v2.1.0 (Build 4)"
BuildInfo.buildTime        // Build timestamp
BuildInfo.isDebug          // Debug/Release flag
```

---

## Release Notes Template

```
## Version X.Y.Z (Build N) - YYYY-MM-DD

### New Features
- Feature description

### Improvements
- Improvement description

### Bug Fixes
- Bug fix description

### Database Changes
- Schema change description

### Known Issues
- Issue description
```
