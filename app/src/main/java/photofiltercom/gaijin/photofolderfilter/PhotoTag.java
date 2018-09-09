package photofiltercom.gaijin.photofolderfilter;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Kachulyak Ivan.
 *
 * This class need for calculation tag for folder.
 *
 * In future this tag will be used to a specific folder.
 * The behavior of folders and files will be subject to the tag.
 */
public class PhotoTag {

    private static List<String> tagParameters = Arrays.asList("Year", "Month", "Week", "Day", "hh", "mm", "ss", "Text", "Number");
    private String TOKEN = "↕";
    private String GROUP_TOKEN = "→";
    private String regEx = "";

    public PhotoTag() {
    }

    public String transformTag(String tag, String pathFolder) {
        String newTag = "";
        String numberRegEx = "";
        String[] parseTag = tag.split(TOKEN);

        for (String gat : parseTag) {
            Log.d("logM", "Part of tag : " + gat);
        }
        for (int i = 0; i < parseTag.length; i++) {
            String[] groupTag = null;
            switch (parseTag[i]) {
                case "Year":
                    newTag += transformCurrentTime("yyyy");
                    break;
                case "Month":
                    newTag += transformCurrentTime("MM");
                    break;
                case "Week":
                    newTag += getCurrentWeek();
                    break;
                case "Day":
                    newTag += transformCurrentTime("dd");
                    break;
                case "hh":
                    newTag += transformCurrentTime("hh");
                    break;
                case "mm":
                    newTag += transformCurrentTime("mm");
                    break;
                case "ss":
                    newTag += transformCurrentTime("ss");
                    break;
            }
            if (parseTag[i].contains("Text")) {
                groupTag = parseTag[i].split(GROUP_TOKEN);
                newTag += whereInText(groupTag[1], i, parseTag.length);
            } else if (parseTag[i].contains("Number")) {
                numberRegEx = whereInText("%s", i, parseTag.length);
                newTag += numberRegEx;
            } else if (parseTag[i].contains("FolderName")) {
                File folder = new File(pathFolder);
                String folderName = folder.getName();
                newTag += whereInText(folderName, i, parseTag.length);
            }
        }

        // On this part need create function wen will count photos it contains same PhotoTag type
        // and return next number for calculation
        String[] partOfRegEx = newTag.split(numberRegEx);

        for (String gat : partOfRegEx) {
            Log.d("logM", "Part of RegEx : " + gat);
        }

        regEx = String.format("[a-zA-Z0-1_]{%d}", partOfRegEx[0].length());

        Log.d("logM", "Mathcher of someText_One_Folder_201808 : " + "someText_One_Folder_201808".matches(regEx));

        int number = 10;

        newTag = newTag.replace("__", "_");
        return newTag;
    }

    private String whereInText(String string, int i, int length) {
        if (i == 0)
            return String.format("%s_", string);
        else if (i == length - 1) {
            return String.format("_%s", string);
        } else {
            return String.format("_%s_", string);
        }
    }

    private String transformCurrentTime(String format) {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    private String getCurrentWeek() {
        Calendar now = Calendar.getInstance();
        return String.valueOf(now.get(Calendar.WEEK_OF_YEAR));
    }

}
