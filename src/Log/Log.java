package Log;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    public Logger logger;
    FileHandler fh;

    public Log(String fname) throws SecurityException, IOException{

        File f = new File(fname);

        if(!f.exists()){
            f.createNewFile();
        }
        fh = new FileHandler(fname, true);
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(fh);
        SimpleFormatter format = new SimpleFormatter();
        fh.setFormatter(format);
    }
}
