import 'package:flutter_test/flutter_test.dart';
import 'dart:convert';
import 'package:potato_helper/potato_helper.dart';

void main() {
  test('adds one to input values', () {
    String json2 = "[\"abc\",\"cdf\"]";
    var p = json.decode(json2);
    List<String> ll = PotatoDataParser.parseListValue(p, (i) {
      return i;
    });
    expect(PotatoDataParser.parseBool(null), false);
    expect(PotatoDataParser.parseBool(0), false);
    expect(PotatoDataParser.parseBool(1), false);
  });
}
