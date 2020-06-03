package qz.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qz.common.Constants;
import qz.installer.Installer;
import qz.installer.certificate.CertificateManager;
import qz.installer.certificate.KeyPairWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import static qz.utils.FileUtilities.*;
import static qz.utils.FileUtilities.TEMP_DIR;

public class MiscUtilities {
    private static final Logger log = LoggerFactory.getLogger(MiscUtilities.class);
    private static Properties props = null;
    public static String DMS_TOKEN_ID = "1234567abcd";

    public static Properties getProps() {
        if (props == null)
            props = loadProperties();
        return props;
    }

    public static void forceLoadProps() {
        props = loadProperties();
    }

    private static Properties loadProperties() {
        Path[] locations = {SystemUtilities.detectAppPath(), SHARED_DIR, USER_DIR};

        Properties props = null;
        for(Path location : locations) {
            if (location == null) continue;
            try {
                props = new Properties();
                props.load(new FileInputStream(new File(location.toFile(), Constants.PROPS_FILE + ".properties")));
                // We've loaded without Exception, return
                log.info("Found {}/{}.properties", location, Constants.PROPS_FILE);
                return props;
            } catch(Exception ignore) {
                log.warn("Properties couldn't be loaded at {}, trying fallback...", location, ignore);
            }
        }
        return null;
    }

    public static void writeDmsProperty(String keyProp, String value, Boolean forceNewValue) {
        Path[] locations = {SystemUtilities.detectAppPath(), SHARED_DIR, USER_DIR};

        Properties props = null;
        for(Path location : locations) {
            if (location == null) continue;
            try {
                props = new Properties();
                props.load(new FileInputStream(new File(location.toFile(), Constants.PROPS_FILE + ".properties")));
                // We've loaded without Exception, return
                // own DMS properties
                log.info("Here we go");
                if (!forceNewValue)
                    props.putIfAbsent(keyProp, value);
                else
                    props.setProperty(keyProp, value);
                saveProperties(props);
            } catch(Exception ignore) {
                log.warn("Properties couldn't be loaded at {}, trying fallback...", location, ignore);
            }
        }
    }

    private static File getWritableLocation(String ... subDirs) throws IOException {
        // Get an array of preferred directories
        ArrayList<Path> locs = new ArrayList<>();

        if (subDirs.length == 0) {
            // Assume root directory is next to jar (e.g. qz-tray.properties)
            Path appPath = SystemUtilities.detectAppPath();
            // Handle null path, such as running from IDE
            if(appPath != null) {
                locs.add(appPath);
            }
            // Fallback on a directory we can normally write to
            locs.add(SHARED_DIR);
            locs.add(USER_DIR);
            // Last, fallback on a directory we won't ever see again :/
            locs.add(TEMP_DIR);
        } else {
            // Assume non-root directories are for ssl (e.g. certs, keystores)
            locs.add(Paths.get(SHARED_DIR.toString(), subDirs));
            // Fallback on a directory we can normally write to
            locs.add(Paths.get(USER_DIR.toString(), subDirs));
            // Last, fallback on a directory we won't ever see again :/
            locs.add(Paths.get(TEMP_DIR.toString(), subDirs));
        }

        // Find a suitable write location
        File path = null;
        for(Path loc : locs) {
            if (loc == null) continue;
            boolean isPreferred = locs.indexOf(loc) == 0;
            path = loc.toFile();
            path.mkdirs();
            if (path.canWrite()) {
                log.debug("Writing to {}", loc);
                if(!isPreferred) {
                    log.warn("Warning, {} isn't the preferred write location, but we'll use it anyway", loc);
                }
                return path;
            } else {
                log.debug("Can't write to {}, trying the next...", loc);
            }
        }
        throw new IOException("Can't find a suitable write location.  SSL will fail.");
    }

    private static void saveProperties(Properties properties) throws IOException {
        File propsFile = new File(getWritableLocation(), Constants.PROPS_FILE + ".properties");
        properties.store(new FileOutputStream(propsFile), null);
        FileUtilities.inheritParentPermissions(propsFile.toPath());
        log.info("Successfully created SSL properties file: {}", propsFile);
    }
}
