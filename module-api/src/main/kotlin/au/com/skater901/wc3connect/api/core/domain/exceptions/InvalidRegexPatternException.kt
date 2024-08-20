package au.com.skater901.wc3connect.api.core.domain.exceptions

/**
 * Indicates that the provided regular expression was not valid.
 */
public class InvalidRegexPatternException(message: String) : IllegalArgumentException(message)