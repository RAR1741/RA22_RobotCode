package config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import frc.robot.config.Config;
import frc.robot.config.Configurable;

public class ConfigTest {
    /**
     * Tests whether {@link Config} can load a file
     */
    @Test
    public void configLoadTest() {
        Config config = new Config();
        boolean success = config.loadFromFile(getTestFileUrl());
        assertTrue(success);
    }

    /**
     * Tests whether {@link Config} values are parsed and returned as the correct
     * types
     */
    @Test
    public void configTypeTest() {
        Config config = new Config();
        config.loadFromFile(getTestFileUrl());
        assertEquals("string", config.getSetting("string_value", "wrong string"));
        assertEquals(5.5, config.getSetting("double_value", 1.0), 0.0001);
        assertTrue(config.getSetting("boolean_value", false));
        assertFalse(config.getSetting("second_boolean", true));
        assertEquals("wrong string", config.getSetting("bad_string", "wrong string"));
    }

    /**
     * Tests whether comments are ignored in {@link Config}
     */
    @Test
    public void configCommentTest() {
        Config config = new Config();
        config.loadFromFile(getTestFileUrl());
        assertEquals(20.0, config.getSetting("comment_value", 30.0), 0.0001);
    }

    /**
     * Tests whether default values are properly returned for nonexistent
     * {@link Config} values
     */
    @Test
    public void configDefaultValueTest() {
        Config config = new Config();
        config.loadFromFile(getTestFileUrl());
        assertEquals("default value", config.getSetting("nonexistent_value", "default value"));
    }

    /**
     * Tests that {@link Configurable Configurables} are called after
     * {@link Config#reloadConfig()}
     */
    @Test
    public void configurableTest() {
        Config config = new Config();
        boolean[] success = { false };
        Configurable configurable = (inConfig) -> {
            assertEquals(5.5, inConfig.getSetting("double_value", 1.0), 0.0001);
            success[0] = true;
        };
        config.addConfigurable(configurable);
        config.loadFromFile(getTestFileUrl());
        assertFalse(success[0]);
        config.reloadConfig();
        assertTrue(success[0]);
        // remove configurable
        success[0] = false;
        config.removeConfigurable(configurable);
        config.reloadConfig();
        assertFalse(success[0]);
        // use removeAllConfigurables instead
        config.addConfigurable(configurable);
        config.reloadConfig();
        assertTrue(success[0]);
        config.removeAllConfigurables();
        success[0] = false;
        config.reloadConfig();
        assertFalse(success[0]);
    }

    @Test
    public void noConfigTest() {
        Config config = new Config();
        assertFalse(config.loadFromFile("file that i hope never exists"));
    }

    public String getTestFileUrl() {
        try {
            return getClass().getResource("test_config.txt").toURI().getPath();
        } catch (Exception exception) {
            return "";
        }
    }
}
