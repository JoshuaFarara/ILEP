package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small JSON reader and writer for the Code Submission API.
 *
 * JSON values map to Java like this:
 * <ul>
 * <li>object → {@code Map<String, Object>} (keeps key order)</li>
 * <li>array → {@code List<Object>}</li>
 * <li>string → {@code String}</li>
 * <li>whole number → {@code Long}; decimal number → {@code Double}</li>
 * <li>true / false → {@code Boolean}; null → {@code null}</li>
 * </ul>
 *
 * Why not a library? ILEP has no build tool yet, so every jar would have to be added by hand in
 * Eclipse on each computer. Only {@link CodeSubmissionClient} uses this class, so replacing it with
 * Jackson later (when ILEP moves to Gradle) changes one file.
 */
public final class JsonText {

	private JsonText() {
	}

	/** Thrown when text is not valid JSON. The message says where parsing stopped. */
	public static class JsonFormatException extends RuntimeException {
		public JsonFormatException(String message) {
			super(message);
		}
	}

	// ------------------------------------------------------------ writing
	/** Converts a Map, List, String, Number, Boolean or null into JSON text. */
	public static String write(Object value) {
		StringBuilder json = new StringBuilder();
		writeValue(value, json);
		return json.toString();
	}

	private static void writeValue(Object value, StringBuilder json) {
		if (value == null) {
			json.append("null");
		} else if (value instanceof String text) {
			writeString(text, json);
		} else if (value instanceof Boolean || value instanceof Integer || value instanceof Long) {
			json.append(value);
		} else if (value instanceof Number number) {
			double decimal = number.doubleValue();
			if (Double.isNaN(decimal) || Double.isInfinite(decimal)) {
				throw new IllegalArgumentException("JSON cannot represent " + decimal);
			}
			json.append(decimal);
		} else if (value instanceof Map<?, ?> map) {
			json.append('{');
			boolean first = true;
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (!first) {
					json.append(',');
				}
				first = false;
				writeString(String.valueOf(entry.getKey()), json);
				json.append(':');
				writeValue(entry.getValue(), json);
			}
			json.append('}');
		} else if (value instanceof List<?> list) {
			json.append('[');
			for (int index = 0; index < list.size(); index++) {
				if (index > 0) {
					json.append(',');
				}
				writeValue(list.get(index), json);
			}
			json.append(']');
		} else {
			throw new IllegalArgumentException("Cannot write " + value.getClass().getSimpleName() + " as JSON");
		}
	}

	private static void writeString(String text, StringBuilder json) {
		json.append('"');
		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			switch (character) {
				case '"' -> json.append("\\\"");
				case '\\' -> json.append("\\\\");
				case '\n' -> json.append("\\n");
				case '\r' -> json.append("\\r");
				case '\t' -> json.append("\\t");
				case '\b' -> json.append("\\b");
				case '\f' -> json.append("\\f");
				default -> {
					if (character < 0x20) {
						json.append(String.format("\\u%04x", (int) character));
					} else {
						json.append(character);
					}
				}
			}
		}
		json.append('"');
	}

	// ------------------------------------------------------------ reading
	/** Parses JSON text into Maps, Lists, Strings, Longs, Doubles, Booleans and nulls. */
	public static Object read(String json) {
		Reader reader = new Reader(json);
		reader.skipWhitespace();
		Object value = reader.readValue();
		reader.skipWhitespace();
		if (!reader.atEnd()) {
			throw reader.error("unexpected text after the JSON value");
		}
		return value;
	}

	/** Parses JSON text that must be an object. */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> readObject(String json) {
		Object value = read(json);
		if (!(value instanceof Map)) {
			throw new JsonFormatException("Expected a JSON object");
		}
		return (Map<String, Object>) value;
	}

	/** Parses JSON text that must be an array. */
	@SuppressWarnings("unchecked")
	public static List<Object> readArray(String json) {
		Object value = read(json);
		if (!(value instanceof List)) {
			throw new JsonFormatException("Expected a JSON array");
		}
		return (List<Object>) value;
	}

	private static final class Reader {
		private final String json;
		private int position;

		Reader(String json) {
			this.json = json == null ? "" : json;
		}

		boolean atEnd() {
			return position >= json.length();
		}

		JsonFormatException error(String problem) {
			return new JsonFormatException("Invalid JSON at character " + position + ": " + problem);
		}

		void skipWhitespace() {
			while (!atEnd() && Character.isWhitespace(json.charAt(position))) {
				position++;
			}
		}

		char peek() {
			if (atEnd()) {
				throw error("unexpected end of text");
			}
			return json.charAt(position);
		}

		void expect(char expected) {
			if (peek() != expected) {
				throw error("expected '" + expected + "'");
			}
			position++;
		}

		Object readValue() {
			char next = peek();
			return switch (next) {
				case '{' -> readObjectValue();
				case '[' -> readArrayValue();
				case '"' -> readString();
				case 't' -> readLiteral("true", Boolean.TRUE);
				case 'f' -> readLiteral("false", Boolean.FALSE);
				case 'n' -> readLiteral("null", null);
				default -> {
					if (next == '-' || Character.isDigit(next)) {
						yield readNumber();
					}
					throw error("unexpected character '" + next + "'");
				}
			};
		}

		Map<String, Object> readObjectValue() {
			expect('{');
			Map<String, Object> object = new LinkedHashMap<>();
			skipWhitespace();
			if (peek() == '}') {
				position++;
				return object;
			}
			while (true) {
				skipWhitespace();
				String key = readString();
				skipWhitespace();
				expect(':');
				skipWhitespace();
				object.put(key, readValue());
				skipWhitespace();
				if (peek() == ',') {
					position++;
				} else {
					expect('}');
					return object;
				}
			}
		}

		List<Object> readArrayValue() {
			expect('[');
			List<Object> array = new ArrayList<>();
			skipWhitespace();
			if (peek() == ']') {
				position++;
				return array;
			}
			while (true) {
				skipWhitespace();
				array.add(readValue());
				skipWhitespace();
				if (peek() == ',') {
					position++;
				} else {
					expect(']');
					return array;
				}
			}
		}

		String readString() {
			expect('"');
			StringBuilder text = new StringBuilder();
			while (true) {
				char character = peek();
				position++;
				if (character == '"') {
					return text.toString();
				}
				if (character == '\\') {
					char escaped = peek();
					position++;
					switch (escaped) {
						case '"' -> text.append('"');
						case '\\' -> text.append('\\');
						case '/' -> text.append('/');
						case 'n' -> text.append('\n');
						case 'r' -> text.append('\r');
						case 't' -> text.append('\t');
						case 'b' -> text.append('\b');
						case 'f' -> text.append('\f');
						case 'u' -> {
							if (position + 4 > json.length()) {
								throw error("incomplete \\u escape");
							}
							try {
								text.append((char) Integer.parseInt(json.substring(position, position + 4), 16));
							} catch (NumberFormatException exception) {
								throw error("invalid \\u escape");
							}
							position += 4;
						}
						default -> throw error("invalid escape '\\" + escaped + "'");
					}
				} else if (character < 0x20) {
					throw error("control character inside a string");
				} else {
					text.append(character);
				}
			}
		}

		Object readLiteral(String literal, Object value) {
			if (!json.startsWith(literal, position)) {
				throw error("expected " + literal);
			}
			position += literal.length();
			return value;
		}

		Object readNumber() {
			int start = position;
			if (peek() == '-') {
				position++;
			}
			boolean decimal = false;
			while (!atEnd()) {
				char character = json.charAt(position);
				if (Character.isDigit(character)) {
					position++;
				} else if (character == '.' || character == 'e' || character == 'E' || character == '+'
						|| character == '-') {
					decimal = true;
					position++;
				} else {
					break;
				}
			}
			String number = json.substring(start, position);
			try {
				return decimal ? (Object) Double.parseDouble(number) : (Object) Long.parseLong(number);
			} catch (NumberFormatException exception) {
				throw error("invalid number '" + number + "'");
			}
		}
	}
}
