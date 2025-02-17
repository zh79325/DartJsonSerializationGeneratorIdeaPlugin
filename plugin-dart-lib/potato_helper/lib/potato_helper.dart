library potato_helper;

class PotatoDataField {
  /// PRIMARY KEY
  final bool primary;

  /// AUTO INCREASEMENT
  final bool ai;

  final bool skip;

  const PotatoDataField({this.primary, this.ai,this.skip});
}

class PotatoDataParser {
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

  static List<T> parseListValue<T, E>(dynamic value, T f(E e)) {
    if (value == null) {
      return null;
    }
    if (value is Set) {
      return Set.of(value).map((e) {
        return f(e);
      }).toList();
    }
    if (value is List) {
      return List.of(value).map((e) {
        return f(e);
      }).toList();
    }
    return null;
  }

  static Set<T> parseSetValue<T, E>(dynamic value, T f(E e)) {
    if (value == null) {
      return null;
    }
    if (value is List) {
      return List.of(value).map((e) {
        return f(e);
      }).toSet();
    }
    if (!value is Set) {
      return Set.of(value).map((e) {
        return f(e);
      }).toSet();
    }
    return null;
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
