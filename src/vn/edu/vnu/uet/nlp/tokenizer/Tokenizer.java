package vn.edu.vnu.uet.nlp.tokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vn.edu.vnu.uet.nlp.utils.FileUtils;
import vn.edu.vnu.uet.nlp.utils.StringUtils;

/**
 * Tokenization process (pre-processing).
 * 
 * @author tuanphong94
 *
 */
public class Tokenizer {
	public static List<String> tokenize(String s) throws IOException {
		List<String> tokens = new ArrayList<>();

		String[] tempTokens = s.split("(\\s| )+");
		String trimed = s.trim();

		if (trimed.isEmpty() || trimed.equals(StringConst.SPACE) || s == null || tempTokens.length == 0) {
			return tokens;
		}

		for (String token : tempTokens) {

			if (token.equals(StringConst.SPACE) || token.isEmpty() || token == null) {
				continue;
			}

			if (token.length() == 1 || !StringUtils.hasPunctuation(token)) {
				tokens.add(token);
				continue;
			}

			if (token.endsWith(",")) {
				tokens.addAll(tokenize(token.substring(0, token.length() - 1)));
				tokens.add(",");
				continue;
			}

			if (AbbreviationException.getAbbreviation().contains(token)) {
				tokens.add(token);
				continue;
			}

			if (token.endsWith(".") && Character.isAlphabetic(token.charAt(token.length() - 2))) {
				if (token.length() == 2 && Character.isUpperCase(token.charAt(token.length() - 2))) {
					tokens.add(token);
					continue;
				}
				tokens.addAll(tokenize(token.substring(0, token.length() - 1)));
				tokens.add(".");
				continue;
			}

			if (AbbreviationException.getException().contains(token)) {
				tokens.add(token);
				continue;
			}

			boolean tokenContainsAbb = false;
			for (String e : AbbreviationException.getAbbreviation()) {
				int i = token.indexOf(e);
				if (i < 0)
					continue;

				// System.out.println(token + "\t\t" + i);
				tokenContainsAbb = true;
				tokens = recursive(tokens, token, i, i + e.length());
				break;
			}
			if (tokenContainsAbb)
				continue;

			boolean tokenContainsExp = false;
			for (String e : AbbreviationException.getException()) {
				int i = token.indexOf(e);
				if (i < 0)
					continue;

				// System.out.println(token + "\t\t" + i);
				tokenContainsExp = true;
				tokens = recursive(tokens, token, i, i + e.length());
				break;
			}
			if (tokenContainsExp)
				continue;

			List<String> regexes = Regex.getRegexList();

			boolean matching = false;
			for (String regex : regexes) {
				// System.out.println(regex);
				if (token.matches(regex)) {
					// System.out.println(token);
					// System.out.println("Regex: " + regex);

					tokens.add(token);
					matching = true;
					break;
				}
			}
			if (matching)
				continue;

			for (int i = 0; i < regexes.size(); i++) {
				Pattern pattern = Pattern.compile(regexes.get(i));
				Matcher matcher = pattern.matcher(token);

				if (matcher.find()) {
					// System.out.println(token);
					// System.out.println("Contain: " + pattern);

					if (i == Regex.getRegexIndex("url")) {
						String[] elements = token.split(Pattern.quote("."));
						boolean hasURL = true;
						for (String ele : elements) {
							if (ele.length() == 1 && Character.isUpperCase(ele.charAt(0))) {
								hasURL = false;
								break;
							}
							for (int j = 0; j < ele.length(); j++) {
								if (ele.charAt(j) >= 128) {
									hasURL = false;
									break;
								}
							}
						}
						if (hasURL) {
							// System.out.println("has url");
							tokens = recursive(tokens, token, matcher.start(), matcher.end());
						} else {
							// System.out.println("not have url");
							continue;
						}
					}

					else if (i == Regex.getRegexIndex("month")) {
						int start = matcher.start();

						boolean hasLetter = false;

						for (int j = 0; j < start; j++) {
							if (Character.isLetter(token.charAt(j))) {
								tokens = recursive(tokens, token, matcher.start(), matcher.end());
								hasLetter = true;
								break;
							}
						}

						if (!hasLetter) {
							tokens.add(token);
						}
					}

					// else if (i == Regex.getRegexIndex("number")) {
					// String[] replaceChar = { "-", "+" };
					// tokens = recursive(tokens, token, matcher.start(),
					// matcher.end(), replaceChar);
					// }
					else {
						tokens = recursive(tokens, token, matcher.start(), matcher.end());
					}

					matching = true;
					break;
				}
			}

			if (matching)
				continue;
			else
				tokens.add(token);
		}

		return tokens;
	}

	private static List<String> recursive(List<String> tokens, String token, int beginMatch, int endMatch)
			throws IOException {
		if (beginMatch > 0)
			tokens.addAll(tokenize(token.substring(0, beginMatch)));
		tokens.addAll(tokenize(token.substring(beginMatch, endMatch)));

		if (endMatch < token.length())
			tokens.addAll(tokenize(token.substring(endMatch)));

		return tokens;
	}

	public static List<String> joinSentences(List<String> tokens) {
		List<String> sentences = new ArrayList<>();

		List<String> sentence = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			String nextToken = null;
			if (i != tokens.size() - 1) {
				nextToken = tokens.get(i + 1);
			}
			String beforeToken = null;
			if (i > 0) {
				beforeToken = tokens.get(i - 1);
			}

			sentence.add(token);

			if (i == tokens.size() - 1) {
				sentences.add(FileUtils.joinSentence(sentence));
				return sentences;
			}

			if (i < tokens.size() - 2 && token.equals(StringConst.COLON)) {
				if (Character.isDigit(nextToken.charAt(0)) && tokens.get(i + 2).equals(StringConst.STOP)
						|| tokens.get(i + 2).equals(StringConst.COMMA)) {
					sentences.add(FileUtils.joinSentence(sentence));
					sentence.clear();
					continue;
				}
			}

			if (token.matches(Regex.EOS_PUNCTUATION)) {

				// If the current sentence is in the quote or in the brace
				if (StringUtils.isBrace(nextToken) || nextToken.isEmpty() || Character.isLowerCase(nextToken.charAt(0))
						|| nextToken.equals(StringConst.COMMA) || Character.isDigit(nextToken.charAt(0))) {
					continue;
				}

				// Sentence starts with its order number
				if (sentence.size() == 2 && token.equals(StringConst.STOP)) {
					if (Character.isDigit(beforeToken.charAt(0))) {
						continue;
					}
					if (Character.isLowerCase(beforeToken.charAt(0))) {
						continue;
					}
					if (Character.isUpperCase(beforeToken.charAt(0))) {
						if (beforeToken.length() == 1) {
							continue;
						}
					}
				}

				sentences.add(FileUtils.joinSentence(sentence));
				sentence.clear();
			}
		}

		return sentences;
	}
}
