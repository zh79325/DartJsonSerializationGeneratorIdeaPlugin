library potato_helper;

class PotatoDataField {
  /// PRIMARY KEY
  final bool primary;

  /// AUTO INCREASEMENT
  final bool ai;

  const PotatoDataField({this.primary, this.ai});
}

class PotatoDataParser{
  static DateTime parseDateTime(dynamic value) {
    if (value == null) {
      return null;
    }
    if (value is int) {
      return DateTime.fromMillisecondsSinceEpoch(value);
    }
    if (value is DateTime) {
      return value;
    }
    if (value is String) {
      return DateTime.parse(value);
    }
    return null;
  }

  static int parseIntValue(dynamic value) {
    if (value == null) {
      return null;
    }
    if (value is int) {
      return value;
    }
    if (value is String) {
      return int.parse(value);
    }
    return 0;
  }

  static double parseDoubleValue(dynamic value) {
    if (value == null) {
      return 0;
    }
    if (value is double) {
      return value;
    }
    if (value is String) {
      return double.parse(value);
    }
    return 0;
  }

  static bool parseBool(dynamic value) {
    if (value == null) {
      return false;
    }
    if (value is bool) {
      return value;
    }
    if (value is int) {
      return value != 0;
    }
    if (value is String) {
      return "true" == value;
    }
    return false;
  }
}

