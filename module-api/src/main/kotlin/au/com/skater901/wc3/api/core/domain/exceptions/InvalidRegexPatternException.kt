package au.com.skater901.wc3.api.core.domain.exceptions

/**
 * Indicates that the provided regular expression was not valid.
 */
public class InvalidRegexPatternException(message: String) : IllegalArgumentException(message)