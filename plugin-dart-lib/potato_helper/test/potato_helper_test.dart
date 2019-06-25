import 'package:flutter_test/flutter_test.dart';

import 'package:potato_helper/potato_helper.dart';

void main() {
  test('adds one to input values', () {
    expect(PotatoDataParser.parseBool(null), false);
    expect(PotatoDataParser.parseBool(0), false);
    expect(PotatoDataParser.parseBool(1), false);
  });
}
