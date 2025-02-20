package ai.deepgram.sdk.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling environment variables and configuration.
 */
public class EnvConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        } catch (Exception e) {
            logger.warn("Failed to load .env file: {}", e.getMessage());
        }
    }

    /**
     * Gets an environment variable value.
     * First checks for a system environment variable, then falls back to .env file.
     *
     * @param key The environment variable key
     * @return The value, or null if not found
     */
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null && dotenv != null) {
            value = dotenv.get(key);
        }
        return value;
    }

    /**
     * Gets an environment variable value with a default fallback.
     *
     * @param key The environment variable key
     * @param defaultValue The default value if not found
     * @return The environment variable value, or the default if not found
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets the Deepgram API key from environment variables.
     * Checks DEEPGRAM_API_KEY in both system environment and .env file.
     *
     * @return The API key
     * @throws IllegalStateException if API key is not found
     */
    public static String getDeepgramApiKey() {
        String apiKey = get("DEEPGRAM_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "DEEPGRAM_API_KEY not found in environment variables or .env file"
            );
        }
        return apiKey;
    }
} 