package core.game.ui;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class Configuration {
    public static final int TIME_LIMIT;
    public static boolean GUI;
    public static final int MAX_STEP;
    public static final String PLAYER_IDs;
    public static final int HOST_ID;
    public static final String GUI_TYPE;
    public static int STEP_INTER;   //每一步停留的时间

    public Configuration() {
    }

    static {
        Properties pps = new Properties();
        // 使用 InputStreamReader 指定编码，主动防御乱码问题
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream("file.properties"), "UTF-8")) {
            pps.load(isr);
        } catch (IOException var2) {
            var2.printStackTrace();
            System.exit(1); // 找不到配置直接退出，避免后续抛出空指针
        }

        TIME_LIMIT = Integer.parseInt(pps.getProperty("TimeLimit"));
        GUI = Boolean.parseBoolean(pps.getProperty("GUI"));
        MAX_STEP = Integer.parseInt(pps.getProperty("MaxStep"));
        STEP_INTER = Integer.parseInt(pps.getProperty("Step_Inter"));
        PLAYER_IDs = pps.getProperty("Player_Ids");
        HOST_ID = Integer.parseInt(pps.getProperty("Host"));
        GUI_TYPE = pps.getProperty("GuiType");
    }
}
